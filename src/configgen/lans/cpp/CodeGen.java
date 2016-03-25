package configgen.lans.cpp;

import configgen.Generator;
import configgen.Main;
import configgen.Utils;
import configgen.type.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeGen implements Generator {
	@Override
	public void gen() {
		Struct.getExports().forEach(s -> {
			genStructHeadFile(s);
		});
		List<Struct> structs = Struct.getExports();
		for(int i = 0, n = (structs.size() + CLASS_PER_CONFIG - 1) / CLASS_PER_CONFIG ; i < n ; i++) {
			genStructCppFile(structs.subList(i * CLASS_PER_CONFIG, Math.min(structs.size(), (i+1) * CLASS_PER_CONFIG)), i);
		}
		genConfigHead();
		//genConfigCpp();
		genAllDefines();
		genStub();
	}

	void save(List<String> lines, String file) {
		final String code = lines.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s", Main.codeDir, file.toLowerCase());
		Utils.save(outFile, code);
	}

	private final static String ENUM_TYPE = "int32_t";
	
	String readType(String type) {
		switch(type) {
			case "bool": return "fs.getBool()";
			case "int": return "fs.getInt()";
			case "long": return "fs.getLong()";
			case "float":  return "fs.getFloat()";
			case "string": return "fs.getString()";
			default: {
				String cppType = toCppType(type);
				if(ENUM.isEnum(type)) {
					return String.format("fs.getInt()", cppType);
				}
				Struct struct = Struct.get(type);
				if(struct.isDynamic())
					return String.format("(%s*)fs.getObject(fs.getString())", cppType);
				else
					return String.format("new %s(fs)", cppType);
			}
		}
	}


	public String toCppType(String type) {
		if(Struct.isStruct(type)) {
			return type.replace(".", "::");
		}
		if(ENUM.isEnum(type)) {
			return ENUM_TYPE;
		}
		switch(type) {
			case "bool" :
			case "float": return type;
			case "string" : return "std::string";
			case "int" : return "int32_t";
			case "long" : return "int64_t";
			case "list" : return "std::vector";
			case "set" : return "std::set";
			case "map" : return "std::map";
		}
		throw new RuntimeException("unknown type:" + type);
	}

	public String toCppDefineType(String type) {
		if(Struct.isStruct(type)) {
			return type.replace(".", "::") + "*";
		}
		if(ENUM.isEnum(type)) {
			return ENUM_TYPE;
		}
		switch(type) {
			case "bool" :
			case "float": return type;
			case "string" : return "std::string";
			case "int" : return "int32_t";
			case "long" : return "int64_t";
			case "list" : return "std::vector";
			case "set" : return "std::set";
			case "map" : return "std::map";
		}
		throw new RuntimeException("unknown type:" + type);
	}

	String getIndexType(Config c) {
		return Struct.get(c.getType()).getField(c.getIndex()).getType();
	}
	
	String toCppValue(String type, String value) {
		switch(type) {
		case "string": return "\"" + value + "\"";
		default: return value;
		}
	}

	String getDefMacroBegin(String fullName) {
		final String macro = "__" + fullName.replace('.', '_').toUpperCase() + "__H__";
		return "#ifndef " + macro + "\n"
				+"#define " + macro + "\n";
	}

	String getDefMacroEnd() {
		return "#endif";
	}

	String getDefNamespaceBegin(String namespace) {
		final StringBuilder sb = new StringBuilder();
		for(String n : namespace.split("\\.")) {
			sb.append("namespace " + n  + "{");
		}
		return sb.toString();
	}

	String getDefNamespaceEnd(String namespace) {
		final StringBuilder sb = new StringBuilder();
		for(String n : namespace.split("\\.")) {
			sb.append("}");
		}
		return sb.toString();
	}

	String getAllDefins() {
		return  "#include \"alldefines.h\"";
	}
	
	void genStructHeadFile(Struct struct) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = struct.getNamespace();
		final String fullName = struct.getFullName();
		ls.add(getDefMacroBegin(fullName));
		ls.add(getAllDefins());
		
		final String base = struct.getBase();
		final String name = struct.getName();
		final boolean isDynamic = struct.isDynamic() ;

		if(!base.isEmpty()) {
			ls.add(String.format("#include \"%s.h\"", base.toLowerCase()));
		}

		ls.add(getDefNamespaceBegin(namespace));

		ls.add(String.format("class %s : public %s {", name, (base.isEmpty() ? "cfg::Object" : toCppType(base))));
		ls.add("public:");
		
		if(!isDynamic) {
			ls.add("static int TYPEID;");
			ls.add("int getTypeId() { return TYPEID; }");
		}
		
		for(Const c : struct.getConsts()) {
			final String type = c.getType();
			final String value = c.getValue();
			final String cname = c.getName();
			if(Field.isRaw(type)) {
				final String cppType = toCppType(type);
				ls.add(String.format("static %s %s;", cppType, cname));
			} else {
				switch(type) {
					case "list:int" :
						ls.add(String.format("static int32_t %s[];", cname));
						break;
					case "list:float" :
						ls.add(String.format("static double %s[];", cname, value));
						break;
					default:
						Utils.error("struct:%s const:%s unknown type:%s", struct.getFullName(), c.getName(), type);	
				}
			}
		}
		
		final ArrayList<String> ds = new ArrayList<String>();
		
		for(Field f : struct.getFields()) {
			String ftype = f.getType();
			final String fname = f.getName();
			final List<String> ftypes = f.getTypes();
			if(f.checkInGroup(Main.groups)) {
				if(f.isRawOrEnumOrStruct()) {
					String dtype = toCppDefineType(ftype);
					ds.add(String.format("%s %s;", dtype, fname));
				} else if(f.isContainer()) {
					switch(ftype) {
						case "list": {
							final String vtype = ftypes.get(1);
							final String dvtype = toCppDefineType(vtype);
							ds.add(String.format("std::vector<%s> %s;", dvtype, fname));
							if(!f.getIndexs().isEmpty()) {
								Struct s = Struct.get(vtype);
								for(String idx : f.getIndexs()) {
									Field idxf = s.getField(idx);
									final String dktype = toCppDefineType(idxf.getType());
									ds.add(String.format("std::map<%s, %s> %s_%s;", dktype, dvtype, fname, idx));
								}
							}
							break;
						}
						case "set": {
							final String vtype = ftypes.get(1);
							final String dvtype = toCppDefineType(vtype);
							ds.add(String.format("std::set<%s> %s;", dvtype, fname));
							break;
						}
						case "map": {
							final String ktype = ftypes.get(1);
							final String dktype = toCppDefineType(ktype);
							final String vtype = ftypes.get(2);
							final String dvtype = toCppDefineType(vtype);
							ds.add(String.format("std::map<%s, %s> %s;", dktype, dvtype, fname));
							break;
						}
					}
				} else {
					Utils.error("unknown type:" + ftype);
				}
			}
		}

		ls.addAll(ds);

		ls.add(String.format("%s(cfg::DataStream& fs);", name));
		ls.add("};");

		ls.add(getDefNamespaceEnd(namespace));
		ls.add(getDefMacroEnd());
		save(ls, fullName + ".h");
	}

	void genStructCppFile(List<Struct> structs, int id) {
		final ArrayList<String> ls = new ArrayList<String>();
		for(Struct struct : structs) {
			final String namespace = struct.getNamespace();
			final String fullName = struct.getFullName();

			final String name = struct.getName();
			final String base = struct.getBase();

			ls.add(include(fullName + ".h"));
			for (String refStructFullName : struct.getRefStructs()) {
				ls.add(include(refStructFullName + ".h"));
			}

			ls.add(getDefNamespaceBegin(namespace));
			if (!struct.isDynamic()) {
				ls.add(String.format("int %s::TYPEID = %s;", name, struct.getTypeId()));
			}

			for (Const c : struct.getConsts()) {
				final String type = c.getType();
				final String value = c.getValue();
				final String cname = c.getName();
				if (Field.isRaw(type)) {
					final String cppType = toCppType(type);
					ls.add(String.format("%s %s::%s = %s;", cppType, name, cname, toCppValue(type, value)));
				} else {
					switch (type) {
						case "list:int":
							ls.add(String.format("int32_t %s::%s[] = {%s};", name, cname, value));
							break;
						case "list:float":
							ls.add(String.format("double %s::%s[] = {%s};", name, cname, value));
							break;
						default:
							Utils.error("struct:%s const:%s unknown type:%s", fullName, c.getName(), type);
					}
				}
			}

			ls.add(String.format("%s::%s(cfg::DataStream& fs) %s {", name, name,
					(base.isEmpty() ? "" : String.format(": %s(fs)", Struct.get(base).getName()))));

			for (Field f : struct.getFields()) {
				String ftype = f.getType();
				final String fname = f.getName();
				final List<String> ftypes = f.getTypes();
				if (f.checkInGroup(Main.groups)) {
					if (f.isRawOrEnumOrStruct()) {
						ls.add(String.format("%s = %s;", fname, readType(ftype)));
					} else if (f.isContainer()) {
						switch (ftype) {
							case "list": {
								final String vtype = ftypes.get(1);
								final String dvtype = toCppDefineType(vtype);
								ls.add("for(int n = fs.getSize(); n-- > 0 ; ) {");
								if (!f.getIndexs().isEmpty()) {
									ls.add(String.format("%s _x = %s;", dvtype, readType(vtype)));
									ls.add(String.format("%s.push_back(_x);", fname));
									for (String idx : f.getIndexs()) {
										ls.add(String.format("%s_%s[_x->%s] = _x;", fname, idx, idx));
									}
								} else {
									ls.add(String.format("%s.push_back(%s);", fname, readType(vtype)));
								}
								ls.add("}");
								break;
							}
							case "set": {
								final String vtype = ftypes.get(1);
								ls.add("for(int n = fs.getSize(); n-- > 0 ; ) {");
								ls.add(String.format("%s.insert(%s);", fname, readType(vtype)));
								ls.add("}");
								break;
							}
							case "map": {
								final String ktype = ftypes.get(1);
								final String vtype = ftypes.get(2);
								ls.add("for(int n = fs.getSize(); n-- > 0 ; ) {");
								ls.add(String.format("%s _key = %s;", toCppDefineType(ktype), readType(ktype)));
								ls.add(String.format("%s[_key] = %s;", fname, readType(vtype)));
								ls.add("}");
								break;
							}
						}
					} else {
						Utils.error("unknown type:" + ftype);
					}
				}
			}
			ls.add("}");

			ls.add(getDefNamespaceEnd(namespace));

		}

		save(ls, "structs" + id + ".cpp");
	}

	String include(String header) {
		return "#include \"" + header.toLowerCase() + "\"";
	}

	final static int CLASS_PER_CONFIG = 100;
	void genConfigHead() {
		final ArrayList<String> ls = new ArrayList<String>();
		List<Config> configs = Config.getExportConfigs();
		final String namespace = "cfg";
		final String name = Main.cfgmgrName;
		final String fullName = namespace + "." + name;
		ls.add(getDefMacroBegin(fullName));
		ls.add(getAllDefins());
		ls.add(getDefNamespaceBegin(namespace));

		ls.add(String.format("class %s {", name));
		ls.add("public:");
		ls.add(String.format("%s& getInstance() { static %s instance; return instance; }", name, name));
		configs.forEach(c -> {
			final String dvtype = toCppDefineType(c.getType());
			final String cname = c.getName();
			if(!c.isSingle()) {
				ls.add(String.format("std::map<%s, %s> %s;", toCppDefineType(getIndexType(c)), dvtype, cname));
			} else {
				ls.add(String.format("%s %s;", dvtype, cname));
			}
			}
		);
		for(int i = 0, n = (configs.size() + CLASS_PER_CONFIG - 1) / CLASS_PER_CONFIG ; i < n ; i++) {
			ls.add(String.format("void load%s(const std::string& dataDir);", i));
		}
		ls.add("void load(const std::string& dataDir) {");
		for(int i = 0, n = (configs.size() + CLASS_PER_CONFIG - 1) / CLASS_PER_CONFIG ; i < n ; i++) {
			ls.add(String.format("load%s(dataDir);", i));
			genSubConfig(configs.subList(i * CLASS_PER_CONFIG, Math.min((i+1) * CLASS_PER_CONFIG, configs.size())), i);
		}
		ls.add("}");
		ls.add("};");
		ls.add(getDefNamespaceEnd(namespace));
		ls.add(getDefMacroEnd());
		save(ls, name + ".h");
	}

	void genSubConfig(List<Config> configs, int id) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = "cfg";
		final String name = Main.cfgmgrName;
		ls.add(include(name + ".h"));
		configs.forEach(s -> {
			ls.add(include(s.getType() + ".h"));
		});
		ls.add(getDefNamespaceBegin(namespace));

		ls.add(String.format("void CfgMgr::load%s(const std::string& dataDir) {", id));
		configs.forEach(
				c -> {
					ls.add("{");
					ls.add(String.format("cfg::DataStream& fs = *cfg::DataStream::create(dataDir + \"/\" + \"%s\");", c.getOutputDataFile()));
					final String cname = c.getName();
					final String vtype = c.getType();
					final String vdtype = toCppDefineType(vtype);
					if(!c.isSingle()) {
						ls.add("for(int n = fs.getSize() ; n-- > 0 ; ) {");
						ls.add(String.format("%s v = %s;", vdtype, readType(vtype)));
						ls.add(String.format("this->%s[v->%s] = v;", cname, c.getIndex()));
						ls.add("}");
						ls.add(String.format("std::cout << \"%s\" << \",size=\" << this->%s.size() << std::endl;", cname, cname));
					} else {
						ls.add("if(fs.getSize() != 1) throw cfg::Error(\"%s\", \"single config but size != 1\");");
						ls.add(String.format("this->%s = %s;", cname, readType(vtype)));
					}
					ls.add("}");
				});
		ls.add("}");

		ls.add(getDefNamespaceEnd(namespace));
		save(ls, name + id + ".cpp");
	}

	void genAllDefines() {
		final ArrayList<String> ls = new ArrayList<String>();
		final String name = "alldefines";
		ls.add(getDefMacroBegin(name));
		ls.add(include("datastream.hpp"));
		ls.add(include("object.h"));

		Struct.getExports().forEach(s -> {
			ls.add(getDefNamespaceBegin(s.getNamespace()));
			ls.add("class " + s.getName() + ";");
			ls.add(getDefNamespaceEnd(s.getNamespace()));
		});
		ENUM.getExports().forEach(e -> {
			final String namespace = e.getNamespace();
			ls.add(getDefNamespaceBegin(namespace));
			ls.add(String.format("class %s {enum {", e.getName()));
			for(Map.Entry<String, Integer> me : e.getCases().entrySet()) {
				ls.add(String.format("%s = %s,", me.getKey().replace("NULL", "null"), me.getValue()));
			}
			ls.add("};};");
			ls.add(getDefNamespaceEnd(namespace));
		});


		ls.add(getDefMacroEnd());
		save(ls, name + ".h");
	}

	void genStub() {
		final ArrayList<String> ls = new ArrayList<String>();
		List<Struct> exportStructs = Struct.getExports();

		final String namespace = "cfg";
		final String name = "Stub";
		final String fullName = namespace + "." + name;
		ls.add(getDefMacroBegin(fullName));
		ls.add(getAllDefins());

		exportStructs.forEach(s -> {
			if(!s.isDynamic())
				ls.add(include(s.getFullName() + ".h"));
		});

		ls.add(getDefNamespaceBegin(namespace));

		ls.add(String.format("class %s {", name));
		ls.add(String.format("public: %s() {", name));
		exportStructs.forEach(s -> {
			if(!s.isDynamic()) {
				ls.add(String.format("DataStream::registerType<%s>(\"%s\");", toCppType(s.getFullName()), s.getFullName()));
			}
		});
		ls.add("}} stub;");

		ls.add(getDefNamespaceEnd(namespace));
		ls.add(getDefMacroEnd());

		save(ls, "stub.cpp");
	}

}
