package configgen.lans.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import configgen.Generator;
import configgen.Main;
import configgen.Utils;
import configgen.type.Config;
import configgen.type.Const;
import configgen.type.ENUM;
import configgen.type.Field;
import configgen.type.Struct;

public class CodeGen implements Generator {
	@Override
	public void gen() {
		Struct.getExports().forEach(s -> genStruct(s));
		ENUM.getExports().forEach(e -> genEnum(e));
		
		genConfig();

	}
	
	String readType(String type) {
		switch(type) {
			case "bool":
			 return "fs.GetBool()";
			case "int":
			 return "fs.GetInt()";
			case "long":
			 return "fs.GetLong()";
			case "float":
			  return "fs.GetFloat()";
			case "string": return "fs.GetString()";
			default: {
				Struct struct = Struct.get(type);
				if(struct.isDynamic())
					return String.format("(%s)cfg.CfgMgr.Create(fs.GetString(), fs)", type);
				else
					return "(" + type + ")cfg.CfgMgr.Create(\"" + type + "\", fs)";
			}
		}
	}
	
	void genEnum(ENUM e) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = e.getNamespace();
		ls.add("namespace " + namespace + "{");
		final String name = e.getName();
		ls.add(String.format("public sealed class %s {", name));
		for(Map.Entry<String, Integer> me : e.getCases().entrySet()) {
			ls.add(String.format("public const int %s = %d;", me.getKey(), me.getValue()));
		}
		ls.add("}");
		ls.add("}");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s/%s.cs", Main.codeDir, namespace.replace('.', '/'), name);
		Utils.save(outFile, code);
	}
	
	void genStructConsts(Struct struct, ArrayList<String> ls) {
		for(Const c : struct.getConsts()) {
			final String type = c.getType();
			final String value = c.getValue();
			final String cname = c.getName();
			if(Field.isRaw(type)) {
				ls.add(String.format("	public const %s %s = %s;",
						toJavaType(type), cname, toJavaValue(type, value)));
			} else {
				switch(type) {
					case "list:int" :
						ls.add(String.format("	public static readonly int[] %s = {%s};", cname, value));
						break;
					case "list:float" :
						ls.add(String.format("	public static readonly double[] %s = {%s};", cname, value));
						break;
					default:
						Utils.error("unknow const type:" + type);	
				}
			}
		}
	}
	
	void genStruct(Struct struct) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = struct.getNamespace();
		ls.add("using System;");
		ls.add("namespace " + namespace + "{");
		
		final String base = struct.getBase();
		final String name = struct.getName();
		final boolean isDynamic = struct.isDynamic() ;
		ls.add(String.format("public %s class %s %s {", isDynamic ? "abstract" : "sealed", name, (base.isEmpty() ? "" : ": " + base)));
		
		if(isDynamic) {
			if(base.isEmpty()) {
				ls.add("public abstract int GetTypeId();");
			}
		} else {
			ls.add(String.format("public const int TYPEID = %s;", struct.getTypeId()));
			ls.add(String.format("public %s int GetTypeId() { return TYPEID; }", (base.isEmpty() ? "" : "override")));
		}
		
		genStructConsts(struct, ls);

		
		final ArrayList<String> ds = new ArrayList<String>();
		final ArrayList<String> cs = new ArrayList<String>();
		
		if(!base.isEmpty()) {
			cs.add(String.format("public %s(cfg.DataStream fs) : base(fs) {", name));
		} else {
			cs.add(String.format("public %s(cfg.DataStream fs) {", name));
		}
		
		for(Field f : struct.getFields()) {
			String ftype = f.getType();
			String jtype = toJavaType(ftype);
			final String fname = f.getName();
			final List<String> ftypes = f.getTypes();
			if(f.checkInGroup(Main.groups)) {
				if(f.isRaw()) {
					ds.add(String.format("public readonly %s %s;", jtype, fname));
					cs.add(String.format("this.%s = %s;", fname, readType(jtype)));
				} else if(f.isStruct()) {
					ds.add(String.format("public readonly %s %s;", jtype, fname));
					cs.add(String.format("this.%s = %s;", fname, readType(jtype)));
				} else if(f.isEnum()) {
					ds.add(String.format("public readonly int %s;", fname));
					cs.add(String.format("this.%s = %s;", fname, readType("int")));
				} else if(f.isContainer()) {
					switch(ftype) {
						case "list": {
							final String valueType = toJavaType(ftypes.get(1));
							ds.add(String.format("public readonly System.Collections.Generic.List<%s> %s = new System.Collections.Generic.List<%s>();", valueType, fname, valueType));
							
							cs.add("for(int n = fs.GetInt(); n-- > 0 ; ) {");
							cs.add(String.format("this.%s.Add(%s);", fname, readType(valueType)));
							cs.add("}");
							
							if(!f.getIndexs().isEmpty()) {
								cs.add(String.format("foreach(var _V in this.%s) {", fname));
								Struct s = Struct.get(valueType);
								for(String idx : f.getIndexs()) {
									Field idxf = s.getField(idx);
									final String keyType = toJavaType(idxf.getType());
									ds.add(String.format("public readonly System.Collections.Generic.Dictionary<%s, %s> %s_%s = new System.Collections.Generic.Dictionary<%s, %s>();",
											keyType, valueType, fname, idx, keyType, valueType));
									cs.add(String.format("this.%s_%s.Add(_V.%s, _V);", fname, idx, idx));
								}
								cs.add("}");
							}
							
							break;
						}
						case "set": {
							final String valueType = toJavaType(ftypes.get(1));
							ds.add(String.format("public readonly System.Collections.Generic.HashSet<%s> %s = new System.Collections.Generic.HashSet<%s>();", valueType, fname, valueType));
							cs.add("for(int n = fs.GetInt(); n-- > 0 ; ) {");
							cs.add(String.format("this.%s.Add(%s);", fname, readType(valueType)));
							cs.add("}");
							break;
						}
						case "map": {
							final String keyType = toJavaType(ftypes.get(1));
							final String valueType = toJavaType(ftypes.get(2));
							ds.add(String.format("public readonly System.Collections.Generic.Dictionary<%s, %s> %s = new System.Collections.Generic.Dictionary<%s, %s>();", keyType, valueType, fname, keyType, valueType));
							cs.add("for(int n = fs.GetInt(); n-- > 0 ; ) {");
							cs.add(String.format("this.%s[%s] = %s;", fname, readType(keyType), readType(valueType)));
							cs.add("}");
							break;
						}
					}
				} else {
					Utils.error("unknown type:" + ftype);
				}
			}
		}
		
		cs.add("}");
		ls.addAll(ds);
		ls.addAll(cs);
		
		ls.add("}");
		ls.add("}");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s/%s.cs", Main.codeDir, namespace.replace('.', '/'), name);
		Utils.save(outFile, code);
	}
	
	public String toJavaType(String rawType) {
		return ENUM.isEnum(rawType) ? "int" : rawType;
	}
	
	String toJavaValue(String type, String value) {
		switch(type) {
		case "string": return "\"" + value + "\"";
		case "float": return value + "f";
		default: return value;
		}
	}
	
	String getIndexType(Config c) {
		final String type = Struct.get(c.getType()).getField(c.getIndex()).getType();
		return ENUM.isEnum(type) ? "int" : type;
	}
	
	void genConfig() {
		final List<Config> exportConfigs = Config.getExportConfigs();
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = "cfg";
		ls.add("using System;");
		ls.add("namespace " + namespace + "{");
		ls.add("public class CfgMgr {");
		ls.add("public class DataDir {");
		ls.add("public static string Dir { set; get;} ");
		ls.add("public static string Encoding { set; get; }");
		ls.add("}");
		exportConfigs.forEach(c -> {
			if(!c.isSingle()) {
				ls.add(String.format("public static readonly System.Collections.Generic.Dictionary<%s, %s> %s = new System.Collections.Generic.Dictionary<%s, %s>();",
					getIndexType(c), c.getType(), c.getName(), getIndexType(c), c.getType()));
			} else {
				ls.add(String.format("public static %s %s;", c.getType(), c.getName()));
			}
			});

		ls.add("public static void Load() {");
		exportConfigs.forEach(
				c -> {
				ls.add("{");ls.add(String.format("var fs = cfg.DataStream.Create(DataDir.Dir + \"/%s\", DataDir.Encoding);", c.getOutputDataFile()));
				if(!c.isSingle()) {
				ls.add(String.format("%s.Clear();", c.getName()));
				ls.add("for(var n = fs.GetInt() ; n-- > 0 ; ) {");
				if(Struct.isDynamic(c.getType())) {
					ls.add(String.format("var v = (%s)Create(fs.GetString(), fs);", c.getType()));
				} else {
					ls.add(String.format("var v = (%s)Create(\"%s\", fs);", c.getType(), c.getType()));
				}
				ls.add(String.format("%s.Add(v.%s, v);", c.getName(), c.getIndex()));
				ls.add("}");
				} else {
					ls.add("fs.GetInt();");
					if(Struct.isDynamic(c.getType())) {
						ls.add(String.format("%s = (%s)Create(fs.GetString(), fs);", c.getName(), c.getType()));
					} else {
						ls.add(String.format("%s = (%s)Create(\"%s\", fs);", c.getName(), c.getType(), c.getType()));
					}
				}
				ls.add("}");
			});
//		Config.configs.values().forEach(c -> 
//			ls.add(String.format("%s = new %s(DataStream.Create(DataDir.Dir + \"/%s\", DataDir.Encoding));", c.getName(), c.getType(), c.getOutputDataFile())));
		ls.add("}");
		
		ls.add("public static Object Create(string name, cfg.DataStream fs) {");
		ls.add("try {");
		ls.add("return Type.GetType(name).GetConstructor(new []{typeof (cfg.DataStream)}).Invoke(new object[]{fs});");
		ls.add("} catch (Exception e) {");
		ls.add("System.Console.WriteLine(e);");
		ls.add("return null;");
		ls.add("}");
		ls.add("}");
		ls.add("}");
		ls.add("}");
	
		final String outFile = String.format("%s/%s/CfgMgr.cs", Main.codeDir, namespace.replace('.', '/'));
		final String code = ls.stream().collect(Collectors.joining("\n"));
		Utils.save(outFile, code);
	}

	public void genMarshallCode() {
		ENUM.getExports().forEach(e -> genEnumXmlMarshal(e));
		Struct.getExports().forEach(s -> genStructXmlMarshallCode(s));
	}
	
	
	public String toMarshalType(String type) {
		int idx = type.lastIndexOf('.');
		return idx < 0 ? type : xmlPrefix + type;
	}
	
	private final static String xmlPrefix = "xml.";
	
	void genEnumXmlMarshal(ENUM e) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = xmlPrefix + e.getNamespace();
		ls.add("namespace " + namespace + "{");
		final String name = e.getName();
		ls.add(String.format("public sealed class %s {", name));
		for(Map.Entry<String, Integer> me : e.getCases().entrySet()) {
			ls.add(String.format("public const int %s = %d;", me.getKey(), me.getValue()));
		}
		ls.add("}");
		ls.add("}");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s.%s.cs", Main.csmarshalcodeDir, namespace, name);
		Utils.save(outFile, code);
	}
	
	String getNamespaceOfType(String type) {
		int idx = type.lastIndexOf('.');
		return idx < 0 ? type : type.substring(0, idx);
	}
	
	String upperFirstChar(String type) {
		return type.substring(0, 1).toUpperCase() + type.substring(1);
	}
	
	String readXmlType(String os, String name, String type) {
		final String marshalType = toMarshalType(type);
		switch(type) {
			case "bool":
			case "int":
			case "long":
			case "float":
			case "string":
			 return String.format("Read%s(%s, \"%s\")", upperFirstChar(type), os, name);
			default: {
				//System.out.println(type);
				Struct struct = Struct.get(type);
				return struct.isDynamic() ?
					String.format("ReadDynamicObject<%s>(%s, \"%s\", \"%s\")", marshalType, os, name, getNamespaceOfType(marshalType))
				:   String.format("ReadObject<%s>(%s, \"%s\", \"%s\")", marshalType, os, name, marshalType);
			}
		}
	}
	
	String readXmlType(String os, String type) {
		final String marshalType = toMarshalType(type);
		switch(type) {
			case "bool":
			case "int":
			case "long":
			case "float":
			case "string":
			 return String.format("Read%s(%s)", upperFirstChar(type), os);
			default: {
				//System.out.println(type);
				Struct struct = Struct.get(type);
				return struct.isDynamic() ?
					String.format("ReadDynamicObject<%s>(%s, \"%s\")", marshalType, os, getNamespaceOfType(marshalType))
				:   String.format("ReadObject<%s>(%s, \"%s\")", marshalType, os, marshalType);
			}
		}
	}

	private void genStructXmlMarshallCode(Struct struct) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace =  xmlPrefix + struct.getNamespace();
		ls.add("using System;");
		ls.add("namespace " + namespace + " {");
		
		final String base = struct.getBase();
		final String name = struct.getName();
		final boolean isDynamic = struct.isDynamic();
		ls.add(String.format("public %s class %s %s {", isDynamic ? "abstract" : "sealed", name, (base.isEmpty() ? ": xml.cfg.XmlMarshaller" : ": " + toMarshalType(base))));
		
		genStructConsts(struct, ls);
		
		final ArrayList<String> ds = new ArrayList<String>();
		final ArrayList<String> ws = new ArrayList<String>();
		final ArrayList<String> rs = new ArrayList<String>();
		ws.add("public override void Write(System.IO.TextWriter os) {");
		rs.add("public override void Read(System.Xml.XmlNode os) {");
		if(!base.isEmpty()) {
			ws.add("base.Write(os);");
			rs.add("base.Read(os);");
		}
		
		for(Field f : struct.getFields()) {
			String ftype = f.getType();
			String jtype = toJavaType(ftype);
			final String fname = f.getName();
			final List<String> ftypes = f.getTypes();
			ws.add(String.format("Write(os, \"%s\", this.%s);", fname, fname));
			
			if (f.isRaw()) {
				ds.add(String.format("public %s %s;", jtype, fname));
				rs.add(String.format("this.%s = %s;", fname, readXmlType("os", fname, jtype)));
			} else if (f.isStruct()) {
				ds.add(String.format("public %s %s;", toMarshalType(jtype), fname));
				rs.add(String.format("this.%s = %s;", fname, readXmlType("os", fname, jtype)));
			} else if(f.isEnum()) {
				ds.add(String.format("public string %s;", fname));
				rs.add(String.format("this.%s = %s;", fname, readXmlType("os", fname, "string")));
			} else if (f.isContainer()) {
				switch (ftype) {
				case "list": {
					final String valueType = toJavaType(ftypes.get(1));
					ds.add(String.format(
							"public readonly System.Collections.Generic.List<%s> %s = new System.Collections.Generic.List<%s>();",
							toMarshalType(valueType), fname, toMarshalType(valueType)));
					rs.add(String.format("GetChilds(GetOnlyChild(os, \"%s\")).ForEach(_ => this.%s.Add(%s));", fname, fname, readXmlType("_", valueType)));
					break;
				}
				case "set": {
					final String valueType = toJavaType(ftypes.get(1));
					ds.add(String.format(
							"public readonly System.Collections.Generic.HashSet<%s> %s = new System.Collections.Generic.HashSet<%s>();",
							toMarshalType(valueType), fname, toMarshalType(valueType)));
					rs.add(String.format("GetChilds(GetOnlyChild(os, \"%s\")).ForEach(_ => this.%s.Add(%s));", fname, fname, readXmlType("_", valueType)));
					break;
				}
				case "map": {
					final String keyType = toJavaType(ftypes.get(1));
					final String valueType = toJavaType(ftypes.get(2));
					ds.add(String.format(
							"public readonly System.Collections.Generic.Dictionary<%s, %s> %s = new System.Collections.Generic.Dictionary<%s, %s>();",
							toMarshalType(keyType), toMarshalType(valueType), fname, toMarshalType(keyType),
							toMarshalType(valueType)));
					rs.add(String.format("GetChilds(GetOnlyChild(os, \"%s\")).ForEach(_ => this.%s.Add(%s, %s));", fname, fname,
						readXmlType("_", "key", valueType), readXmlType("_", "value", valueType)));
					break;
				}
				}
			} else {
				throw new RuntimeException("unknown type:" + jtype);
			}
		}
		
		ws.add("}");
		rs.add("}");
		ls.addAll(ds);
		ls.addAll(ws);
		ls.addAll(rs);
		
		ls.add("}");
		ls.add("}");
		
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s.%s.cs", Main.csmarshalcodeDir, namespace, name);
		Utils.save(outFile, code);
	}

}
