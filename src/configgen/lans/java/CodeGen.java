package configgen.lans.java;

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
			case "boolean":
			case "Boolean": return "fs.getBool()";
			case "int":
			case "Integer": return "fs.getInt()";
			case "long":
			case "Long": return "fs.getLong()";
			case "float":
			case "Float":  return "fs.getFloat()";
			case "String": return "fs.getString()";
			default: {
				Struct struct = Struct.get(type);
				if(struct.isDynamic())
					return String.format("(%s)CfgMgr.create(fs.getString(), fs)", type);
				else
					return "(" + type + ")CfgMgr.create(\"" + type + "\", fs)";
			}
		}
	}
	
	String toJavaValue(String type, String value) {
		switch(type) {
		case "string": return "\"" + value + "\"";
		case "float": return value + "f";
		default: return value;
		}

	}
	
	void genEnum(ENUM e) {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("package " + namespace + ";");
		final String name = e.getName();
		ls.add(String.format("public final class %s {", name));
		for(Map.Entry<String, Integer> me : e.getCases().entrySet()) {
			ls.add(String.format("public final static int %s = %d;", me.getKey(), me.getValue()));
		}
		ls.add("}");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s/%s.java", Main.codeDir, namespace, name);
		Utils.save(outFile, code);
	}
	
	void genStruct(Struct struct) {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("package " + namespace + ";");
		
		final String base = struct.getBase();
		final String name = struct.getName();
		ls.add(String.format("public %s class %s %s {", (struct.isDynamic() ? "abstract" : "final"), name, (base.isEmpty() ? "" : "extends " + base)));
		
		for(Const c : struct.getConsts()) {
			ls.add(String.format("public static final %s %s = %s;",
				toJavaType(c.getType()), c.getName(), toJavaValue(c.getType(), c.getValue())));
		}
		
		final ArrayList<String> ds = new ArrayList<String>();
		final ArrayList<String> cs = new ArrayList<String>();
		cs.add(String.format("public %s(DataStream fs) {", name));
		
		if(!base.isEmpty()) {
			cs.add("super(fs);");
		}
		
		for(Field f : struct.getFields()) {
			String ftype = f.getType();
			String jtype = toJavaType(ftype);
			final String fname = f.getName();
			final List<String> ftypes = f.getTypes();
			if(f.checkInGroup(Main.groups)) {
				if(f.isRaw()) {
					ds.add(String.format("public final %s %s;", jtype, fname));
					cs.add(String.format("this.%s = %s;", fname, readType(jtype)));
				} else if(f.isStruct()) {
					ds.add(String.format("public final %s %s;", jtype, fname));
					cs.add(String.format("this.%s = %s;", fname, readType(jtype)));
				} else if(f.isEnum()) {
					ds.add(String.format("public final int %s;", fname));
					cs.add(String.format("this.%s = %s;", fname, readType("int")));
				} else if(f.isContainer()) {
					switch(ftype) {
						case "list": {
							final String valueType = toBoxType(toJavaType(ftypes.get(1)));
							ds.add(String.format("public final java.util.List<%s> %s = new java.util.ArrayList<%s>();", valueType, fname, valueType));
							
							cs.add("for(int n = fs.getInt(); n-- > 0 ; ) {");
							cs.add(String.format("this.%s.add(%s);", fname, readType(valueType)));
							cs.add("}");
							
							if(!f.getIndexs().isEmpty()) {
								cs.add(String.format("for(%s _V : this.%s) {", valueType, fname));
								Struct s = Struct.get(valueType);
								for(String idx : f.getIndexs()) {
									Field idxf = s.getField(idx);
									final String keyType = toBoxType(toJavaType(idxf.getType()));
									ds.add(String.format("public final java.util.Map<%s, %s> %s_%s = new java.util.HashMap<%s, %s>();",
										keyType, valueType, fname, idx, keyType, valueType));
									cs.add(String.format("this.%s_%s.put(_V.%s, _V);", fname, idx, idx));
								}
								cs.add("}");
							}
							
							break;
						}
						case "set": {
							final String valueType = toBoxType(toJavaType(ftypes.get(1)));
							ds.add(String.format("public final java.util.Set<%s> %s = new java.util.HashSet<%s>();", valueType, fname, valueType));
							cs.add("for(int n = fs.getInt(); n-- > 0 ; ) {");
							cs.add(String.format("this.%s.add(%s);", fname, readType(valueType)));
							cs.add("}");
							break;
						}
						case "map": {
							final String keyType = toBoxType(toJavaType(ftypes.get(1)));;
							final String valueType = toBoxType(toJavaType(ftypes.get(2)));
							ds.add(String.format("public final java.util.Map<%s, %s> %s = new java.util.HashMap<%s, %s>();", keyType, valueType, fname, keyType, valueType));
							cs.add("for(int n = fs.getInt(); n-- > 0 ; ) {");
							cs.add(String.format("this.%s.put(%s, %s);", fname, readType(keyType), readType(valueType)));
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
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s/%s.java", Main.codeDir, namespace, name);
		Utils.save(outFile, code);
	}
	
	public String toJavaType(String rawType) {
		switch(rawType) {
		case "bool" : return "boolean";
		case "string" : return "String";
		}
		return rawType;
	}
	
	public String toBoxType(String type) {
		switch(type) {
		case "boolean": return "Boolean";
		case "int": return "Integer";
		case "long": return "Long";
		case "float": return "Float";
		default: return type;
		}
	}
	
	String getIndexType(Config c) {
		return toBoxType(Struct.get(c.getType()).getField(c.getIndex()).getType());
	}
	
	void genConfig() {
		final List<Config> exportConfigs = Config.getExportConfigs();
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add("package " + namespace + ";");
		ls.add("public class CfgMgr {");
		ls.add("public static class DataDir { public static String dir; public static String encoding; }");
		exportConfigs.forEach(c -> ls.add(String.format("public static final java.util.Map<%s, %s> %s = new java.util.HashMap<>();", 
				getIndexType(c), c.getType(), c.getName())));
		ls.add("public static void load() {");
		exportConfigs.forEach(
			c -> {
			ls.add("{");
			ls.add(String.format("%s.clear();", c.getName()));
			ls.add(String.format("DataStream fs =DataStream.create(DataDir.dir + \"/%s\", DataDir.encoding);", c.getOutputDataFile()));
			ls.add("for(int n = fs.getInt() ; n-- > 0 ; ) {");
			if(Struct.isDynamic(c.getType())) {
				ls.add(String.format("final %s v = (%s)create(fs.getString(), fs);", c.getType(), c.getType()));
			} else {
				ls.add(String.format("final %s v = (%s)create(\"%s\", fs);", c.getType(), c.getType(), c.getType()));
			}
			ls.add(String.format("%s.put(v.%s, v);", c.getName(), c.getIndex()));
			ls.add("}}");
		});
		ls.add("}");
		
		ls.add("public static Object create(String name, DataStream fs) {");
		ls.add("try {");
		ls.add("return Class.forName(\"cfg.\" + name).getConstructor(DataStream.class).newInstance(fs);");
		ls.add("} catch (Exception e) {");
		ls.add("e.printStackTrace();");
		ls.add("return null;");
		ls.add("}");
		ls.add("}");
		ls.add("}");
		final String outFile = String.format("%s/%s/CfgMgr.java", Main.codeDir, namespace);
		final String code = ls.stream().collect(Collectors.joining("\n"));
		Utils.save(outFile, code);
	}

}
