package configgen.lans.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import configgen.Generator;
import configgen.Main;
import configgen.Utils;
import configgen.type.Config;
import configgen.type.Const;
import configgen.type.Field;
import configgen.type.Struct;

public class CodeGen implements Generator {
	public final String namespace = "cfg";
	@Override
	public void gen() {
		Config.refStructs.forEach(s -> genStruct(Struct.get(s)));
		
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
	
	void genStruct(Struct struct) {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("using System;");
		ls.add("namespace " + namespace + "{");
		
		final String base = struct.getBase();
		final String name = struct.getName();
		ls.add(String.format("public %s class %s %s {", struct.isDynamic() ? "abstract" : "sealed", name, (base.isEmpty() ? "" : ": " + base)));
		
		if(struct.isDynamic()) {
			ls.add("public abstract int GetTypeid();");
		} else {
		ls.add(String.format("public const int __TYPE_ID__ = %s;", struct.getTypeid()));
		ls.add(String.format("public %s int GetTypeid() { return __TYPE_ID__; }", struct.getBase().isEmpty() ? "" : "override"));
		}
		
		for(Const c : struct.getConsts()) {
			ls.add(String.format("public const %s %s = %s;",
				toJavaType(c.getType()), c.getName(), toJavaValue(c.getType(), c.getValue())));
		}
		
		final ArrayList<String> ds = new ArrayList<String>();
		final ArrayList<String> cs = new ArrayList<String>();
		
		if(!base.isEmpty()) {
			cs.add(String.format("public %s(CSVStream fs) : base(fs) {", name));
		} else {
			cs.add(String.format("public %s(CSVStream fs) {", name));
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
					final String ename = f.getEnums().get(0);
					jtype = toJavaType(ftypes.get(1));
					ds.add(String.format("public readonly %s %s;", jtype, ename));
					cs.add(String.format("this.%s = %s;", ename, readType(jtype)));
				} else if(f.isContainer()) {
					switch(ftype) {
						case "list": {
							boolean isEnum = Field.isEnum(ftypes.get(1));
							final String valueType = toBoxType(toJavaType(isEnum ?ftypes.get(2) : ftypes.get(1)));
							ds.add(String.format("public readonly System.Collections.Generic.List<%s> %s = new System.Collections.Generic.List<%s>();", valueType, fname, valueType));
							
							cs.add("while(!fs.IsSectionEnd()) {");
							cs.add(String.format("this.%s.Add(%s);", fname, readType(valueType)));
							cs.add("}");
							
							if(!f.getIndexs().isEmpty()) {
								cs.add(String.format("foreach(var _V in this.%s) {", fname));
								Struct s = Struct.get(valueType);
								for(String idx : f.getIndexs()) {
									Field idxf = s.getField(idx);
									final String keyType = toBoxType(toJavaType(idxf.getType()));
									ds.add(String.format("public readonly System.Collections.Generic.Dictionary<%s, %s> %s_%s = new System.Collections.Generic.Dictionary<%s, %s>();",
											keyType, valueType, fname, idx, keyType, valueType));
									cs.add(String.format("this.%s_%s.Add(_V.%s, _V);", fname, idx, idx));
								}
								cs.add("}");
							}
							
							//容器类中只有List的value允许为enum类型
							if(isEnum) {
								int i = 0;
								for(String ename : f.getEnums()) {
									ds.add(String.format("public readonly %s %s;", valueType, ename));
									cs.add(String.format("this.%s = this.%s[%d];", ename, fname, i++));
								}
							}
							break;
						}
						case "set": {
							final String valueType = toBoxType(toJavaType(ftypes.get(1)));
							ds.add(String.format("public readonly System.Collections.Generic.HashSet<%s> %s = new System.Collections.Generic.HashSet<%s>();", valueType, fname, valueType));
							cs.add("while(!fs.IsSectionEnd()) {");
							cs.add(String.format("this.%s.Add(%s);", fname, readType(valueType)));
							cs.add("}");
							break;
						}
						case "map": {
							final String keyType = toBoxType(toJavaType(ftypes.get(1)));;
							final String valueType = toBoxType(toJavaType(ftypes.get(2)));
							ds.add(String.format("public readonly System.Collections.Generic.Dictionary<%s, %s> %s = new System.Collections.Generic.Dictionary<%s, %s>();", keyType, valueType, fname, keyType, valueType));
							cs.add("while(!fs.IsSectionEnd()) {");
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
		Main.println(code);
		final String outFile = String.format("%s/%s/%s.cs", Main.codeDir, namespace, name);
		Utils.save(outFile, code);
	}
	
	public String toJavaType(String rawType) {
		return rawType;
	}
	
	public String toBoxType(String type) {
		return type;
	}
	
	String toJavaValue(String type, String value) {
		switch(type) {
		case "string": return "\"" + value + "\"";
		case "float": return value + "f";
		default: return value;
		}

	}
	
	void genConfig() {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("using System;");
		ls.add("namespace " + namespace + "{");
		ls.add("public class CfgMgr {");
		ls.add("public class DataDir {");
		ls.add("public static string Dir { set; get;} ");
		ls.add("public static string Encoding { set; get; }");
		ls.add("}");
		ls.add("public static void Load() {  }");
		Config.configs.values().forEach(c -> ls.add(String.format("public static readonly %s %s;", c.getType(), c.getName())));
		ls.add("static CfgMgr() {");
		Config.configs.values().forEach(c -> 
			ls.add(String.format("%s = new %s(CSVStream.Create(DataDir.Dir + \"/%s\", DataDir.Encoding));", c.getName(), c.getType(), c.getOutputDataFile())));
		ls.add("}");
		
		ls.add("public static Object Create(string name, CSVStream fs) {");
		ls.add("try {");
		ls.add("return Type.GetType(\"" + namespace + ".\" + name).GetConstructor(new []{typeof (CSVStream)}).Invoke(new object[]{fs});");
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

}
