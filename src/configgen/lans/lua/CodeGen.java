package configgen.lans.lua;

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
		genStructAndEnums();		
		genConfig();
	}
	
	void genStructBody(Struct struct, ArrayList<String> ls) {
		final String base = struct.getBase();
		if(Field.isStruct(base)) {
			genStructBody(Struct.get(base), ls);
		}
		
		for(Field f : struct.getFields()) {
			String ftype = f.getType();
			String fname = f.getName();
			final List<String> ftypes = f.getTypes();
			if(f.checkInGroup(Main.groups)) {
				if(f.isRaw()) {
					ls.add(String.format("o.%s = self:get_%s()", fname, ftype));
				} else if(f.isStruct()) {
					ls.add(String.format("o.%s = self:get_%s()", fname, ftype.replace('.', '_')));
				} else if(f.isEnum()) {
					ls.add(String.format("o.%s = self:get_int()", fname));
				} else if(f.isContainer()) {
					switch(ftype) {
						case "list": {
							final String valueType = ftypes.get(1);
							ls.add(String.format("local _list = self:get_list('%s')", valueType.replace('.', '_')));
							ls.add(String.format("o.%s = _list", fname));
							
							if(!f.getIndexs().isEmpty()) {
								final ArrayList<String> cs = new ArrayList<String>();
								cs.add("for _, _V in ipairs(_list) do");
								for(String idx : f.getIndexs()) {
									ls.add(String.format("o.%s_%s = {}", fname, idx));
									cs.add(String.format("o.%s_%s[_V.%s] = _V", fname, idx, idx));
								}
								cs.add("end");
								ls.addAll(cs);
							}
							
							break;
						}
						case "set": {
							final String valueType = ftypes.get(1);
							ls.add(String.format("o.%s = self:get_set('%s')", fname, valueType.replace('.', '_')));
							break;
						}
						case "map": {
							final String keyType = ftypes.get(1);
							final String valueType = ftypes.get(2);
							ls.add(String.format("o.%s = self:get_map('%s', '%s')", fname, keyType.replace('.', '_'), valueType.replace('.', '_')));
							break;
						}
					}
				} else {
					Utils.error("unknown type:" + ftype);
				}
			}
		}
	}
	
	String toLuaValue(String type, String value) {
		switch(type) {
		case "string": return "\"" + value + "\"";
		case "list:int":
		case "list:float": return "{" + value + "}";
		default: return value;
		}

	}
	
	void genStructAndEnums() {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = "cfg";
		ls.add(String.format("local os = require '%s.datastream'", namespace));

		ls.add("local insert = table.insert");
		ls.add("local ipairs = ipairs");
		ls.add("local setmetatable = setmetatable");
		ls.add("local find = string.find");
		ls.add("local sub = string.sub");
		
		ls.add("local function get_or_create(namespace)");
		ls.add("local t = _G");
		ls.add("local idx = 1");
		ls.add("while true do");
		ls.add("local start, ends = find(namespace, '.', idx, true)");
		ls.add("local subname = sub(namespace, idx, start and start - 1)");
		ls.add("local subt = t[subname]");
		ls.add("if not subt then");
		ls.add("subt = {}");
		ls.add("      t[subname] = subt");
		ls.add("end");
		ls.add("    t = subt");
		ls.add("if start then"); 
		ls.add("idx = ends + 1");
		ls.add("else"); 
		ls.add("return t");
		ls.add("end");
		ls.add("end");
		ls.add("end");

		ls.add("function os:gettype(typename)");
		ls.add("return self['get_' .. typename:gsub('%.', '_')](self)");
		ls.add("end");
		ls.add("");
		ls.add("local meta");

		for(Struct struct : Struct.getExports()) {
			final String fullname = struct.getFullName();
			final String name = struct.getName();
			
			ls.add("meta = {}");
			ls.add("meta.__index = meta");
			ls.add("meta.class = '" + fullname + "'");
			for(Const c : struct.getConsts()) {
				ls.add(String.format("meta.%s = %s", c.getName(), toLuaValue(c.getType(), c.getValue())));
			}
			ls.add(String.format("get_or_create('%s')['%s'] = meta", struct.getNamespace(), name));
			
			ls.add(String.format("function os:get_%s()", fullname.replace('.', '_')));
			if(struct.isDynamic()) {
				ls.add("return self['get_' .. self:get_string():gsub('%.', '_')](self)");
			} else {
				ls.add("local o = {}");
				ls.add(String.format("setmetatable(o, %s)", fullname));
				genStructBody(struct, ls);
				ls.add("return o");
			}
			ls.add("end");
		}
		
		for(ENUM e : ENUM.getExports()) {
			final String name = e.getName();
			ls.add(String.format("get_or_create('%s')['%s'] = {", e.getNamespace(), name));
			for(Map.Entry<String, Integer> me : e.getCases().entrySet()) {
				ls.add(String.format("%s = %d,", me.getKey(), me.getValue()));
			}
			ls.add("}");
		}


	
		ls.add("return os");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s/structs.lua", Main.codeDir, namespace.replace('.', '/'));
		Utils.save(outFile, code);
	}
	
	void genConfig() {
		final List<Config> exportConfigs = Config.getExportConfigs();
		final String namespace = "cfg";
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add(String.format("local os = require '%s.structs'", namespace));
		ls.add("local create_datastream = create_datastream");
		ls.add("local cfgs = {}");
		ls.add("for _, s in ipairs({");
		exportConfigs.forEach(c -> ls.add(String.format("{name='%s', type='%s', index='%s', output='%s', single=%s},",
			c.getName(), c.getType(), c.isSingle() ? "" : c.getIndex(), c.getOutputDataFile(), c.isSingle())));
		ls.add("}) do");
		

		ls.add(String.format("local fs = create_datastream(s.output)"));
		ls.add("local method = 'get_' .. s.type:gsub('%.', '_')");
		ls.add("if not s.single then");
		ls.add("local c = {}");
		ls.add("for i = 1, fs:get_int() do");
		ls.add("local v = fs[method](fs)");
		ls.add("c[v[s.index]] = v");
		ls.add("end");
		ls.add("cfgs[s.name] = c");
		ls.add("else");
		ls.add("if fs:get_int() ~= 1 then error('single config size != 1') end");
		ls.add("cfgs[s.name] = fs[method](fs)");
		ls.add("end");
		ls.add("end");
		
		ls.add("return cfgs");
		
		final String outFile = String.format("%s/%s/configs.lua", Main.codeDir, namespace.replace('.', '/'));
		final String code = ls.stream().collect(Collectors.joining("\n"));
		Utils.save(outFile, code);
	}

}
