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
	public final String namespace = "cfg";
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
					return String.format("(%s)CfgMgr.Create(fs.GetString(), fs)", type);
				else
					return "(" + type + ")CfgMgr.Create(\"" + type + "\", fs)";
			}
		}
	}
	
	void genEnum(ENUM e) {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("package " + namespace + ";");
		final String name = e.getName();
		ls.add(String.format("public sealed class %s {", name));
		for(Map.Entry<String, Integer> me : e.getCases().entrySet()) {
			ls.add(String.format("public const int %s = %d;", me.getKey(), me.getValue()));
		}
		ls.add("}");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s/%s.cs", Main.codeDir, namespace, name);
		Utils.save(outFile, code);
	}
	
	void genStruct(Struct struct) {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("using System;");
		ls.add("namespace " + namespace + "{");
		
		final String base = struct.getBase();
		final String name = struct.getName();
		ls.add(String.format("public %s class %s %s {", struct.isDynamic() ? "abstract" : "sealed", name, (base.isEmpty() ? "" : ": " + base)));
		
		for(Const c : struct.getConsts()) {
			ls.add(String.format("public const %s %s = %s;",
				toJavaType(c.getType()), c.getName(), toJavaValue(c.getType(), c.getValue())));
		}
		
		final ArrayList<String> ds = new ArrayList<String>();
		final ArrayList<String> cs = new ArrayList<String>();
		
		if(!base.isEmpty()) {
			cs.add(String.format("public %s(DataStream fs) : base(fs) {", name));
		} else {
			cs.add(String.format("public %s(DataStream fs) {", name));
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
		final String outFile = String.format("%s/%s/%s.cs", Main.codeDir, namespace, name);
		Utils.save(outFile, code);
	}
	
	public String toJavaType(String rawType) {
		return rawType;
	}
	
	String toJavaValue(String type, String value) {
		switch(type) {
		case "string": return "\"" + value + "\"";
		case "float": return value + "f";
		default: return value;
		}

	}
	
	String getIndexType(Config c) {
		return Struct.get(c.getType()).getField(c.getIndex()).getType();
	}
	
	void genConfig() {
		final List<Config> exportConfigs = Config.getExportConfigs();
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("using System;");
		ls.add("namespace " + namespace + "{");
		ls.add("public class CfgMgr {");
		ls.add("public class DataDir {");
		ls.add("public static string Dir { set; get;} ");
		ls.add("public static string Encoding { set; get; }");
		ls.add("}");
		exportConfigs.forEach(c -> ls.add(String.format("public static readonly System.Collections.Generic.Dictionary<%s, %s> %s = new System.Collections.Generic.Dictionary<%s, %s>();",
				getIndexType(c), c.getType(), c.getName(), getIndexType(c), c.getType())));

		ls.add("public static void Load() {");
		exportConfigs.forEach(
				c -> {
				ls.add("{");
				ls.add(String.format("%s.Clear();", c.getName()));
				ls.add(String.format("var fs =DataStream.Create(DataDir.Dir + \"/%s\", DataDir.Encoding);", c.getOutputDataFile()));
				ls.add("for(var n = fs.GetInt() ; n-- > 0 ; ) {");
				if(Struct.isDynamic(c.getType())) {
					ls.add(String.format("var v = (%s)Create(fs.GetString(), fs);", c.getType()));
				} else {
					ls.add(String.format("var v = (%s)Create(\"%s\", fs);", c.getType(), c.getType()));
				}
				ls.add(String.format("%s.Add(v.%s, v);", c.getName(), c.getIndex()));
				ls.add("}}");
			});
//		Config.configs.values().forEach(c -> 
//			ls.add(String.format("%s = new %s(DataStream.Create(DataDir.Dir + \"/%s\", DataDir.Encoding));", c.getName(), c.getType(), c.getOutputDataFile())));
		ls.add("}");
		
		ls.add("public static Object Create(string name, DataStream fs) {");
		ls.add("try {");
		ls.add("return Type.GetType(\"" + namespace + ".\" + name).GetConstructor(new []{typeof (DataStream)}).Invoke(new object[]{fs});");
		ls.add("} catch (Exception e) {");
		ls.add("System.Console.WriteLine(e);");
		ls.add("return null;");
		ls.add("}");
		ls.add("}");
		ls.add("}");
		ls.add("}");
	
		final String outFile = String.format("%s/%s/CfgMgr.cs", Main.codeDir, namespace);
		final String code = ls.stream().collect(Collectors.joining("\n"));
		Utils.save(outFile, code);
	}

	public void genMarshallCode() {
		Config.refStructs.forEach(s -> genStructMarshallCode(Struct.get(s)));
	}

	private void genStructMarshallCode(Struct struct) {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("using System;");
		ls.add("namespace " + namespace + ".marshal {");
		
		final String base = struct.getBase();
		final String name = struct.getName();
		ls.add(String.format("public %s class %s %s {", struct.isDynamic() ? "abstract" : "sealed", name, (base.isEmpty() ? ": IMarshaller" : ": " + base)));

		
		final ArrayList<String> ds = new ArrayList<String>();
		final ArrayList<String> cs = new ArrayList<String>();
		final String adorn = base.isEmpty() ? (struct.isDynamic() ? "virtual" : "") : "override";
		cs.add(String.format("public %s void Write(DataWriter dw) {", adorn));
		if(!base.isEmpty())
			cs.add("base.Write(dw);");
		
		for(Field f : struct.getFields()) {
			String ftype = f.getType();
			String jtype = toJavaType(ftype);
			final String fname = f.getName();
			final List<String> ftypes = f.getTypes();
			
			//if(f.checkInGroup(Main.groups)) {
				if(f.isRaw()) {
					cs.add(String.format("dw.Write(this.%s);", fname));
					ds.add(String.format("public %s %s;", jtype, fname));
					
				} else if(f.isStruct()) {
					cs.add(String.format("dw.Write(this.%s, %s);", fname, Struct.isDynamic(jtype)));
					ds.add(String.format("public %s %s;", jtype, fname));
				} else if(f.isContainer()) {
					switch(ftype) {
						case "list": {
							final String valueType = toJavaType(ftypes.get(1));
							cs.add(String.format("dw.Write(this.%s, %s);", fname, Struct.isDynamic(valueType)));
							ds.add(String.format("public readonly System.Collections.Generic.List<%s> %s = new System.Collections.Generic.List<%s>();", valueType, fname, valueType));
							break;
						}
						case "set": {
							final String valueType = toJavaType(ftypes.get(1));
							cs.add(String.format("dw.Write(this.%s, %s);", fname, Struct.isDynamic(valueType)));
							ds.add(String.format("public readonly System.Collections.Generic.HashSet<%s> %s = new System.Collections.Generic.HashSet<%s>();", valueType, fname, valueType));
							break;
						}
						case "map": {
							final String keyType = toJavaType(ftypes.get(1));
							final String valueType = toJavaType(ftypes.get(2));
							cs.add(String.format("dw.Write(this.%s, %s, %s);", fname, Struct.isDynamic(keyType), Struct.isDynamic(valueType)));
							ds.add(String.format("public readonly System.Collections.Generic.Dictionary<%s, %s> %s = new System.Collections.Generic.Dictionary<%s, %s>();", keyType, valueType, fname, keyType, valueType));
							break;
						}
					}
				//}
			}
		}
		
		cs.add("}");
		ls.addAll(ds);
		ls.addAll(cs);
		
		ls.add("}");
		ls.add("}");
		
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s.cs", Main.csmarshalcodeDir, name);
		Utils.save(outFile, code);
	}

}
