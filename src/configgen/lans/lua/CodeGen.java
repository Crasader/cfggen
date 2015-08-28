package configgen.lans.lua;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import configgen.Generator;
import configgen.Main;
import configgen.Utils;
import configgen.type.Config;
import configgen.type.Field;
import configgen.type.Struct;

public class CodeGen implements Generator {
	public final String namespace = "cfg";	
	@Override
	public void gen() {	
		genStructs();		
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
					ls.add(String.format("o.%s = self:get_%s()", fname, ftype));
				} else if(f.isContainer()) {
					switch(ftype) {
						case "list": {
							final String valueType = ftypes.get(1);
							ls.add(String.format("local _list = self:get_list('%s')", valueType));
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
							ls.add(String.format("o.%s = self:get_set('%s')", fname, valueType));
							break;
						}
						case "map": {
							final String keyType = ftypes.get(1);
							final String valueType = ftypes.get(2);
							ls.add(String.format("o.%s = self:get_map('%s', '%s')", fname, keyType, valueType));
							break;
						}
					}
				}
			}
		}
	}
	
	void genStructs() {
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add(String.format("local os = require '%s.datastream'", namespace));
		ls.add("local insert = table.insert");
		ls.add("local ipairs = ipairs");

		for(String name : Config.refStructs) {
			final Struct struct = Struct.get(name);
			
			ls.add(String.format("function os:get_%s()", name));
			if(struct.isDynamic()) {
				ls.add("return self['get_' .. self:get_string()](self)");
			} else {
				ls.add("local o = {}");
				genStructBody(struct, ls);
				ls.add("return o");
			}
			ls.add("end");
		}


	
		ls.add("return os");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s/structs.lua", Main.codeDir, namespace);
		Utils.save(outFile, code);
	}
	
	void genConfig() {

		
		final ArrayList<String> ls = new ArrayList<String>();
		ls.add(String.format("local os = require '%s.structs'", namespace));
		ls.add("local create_datastream = create_datastream");
		ls.add("local cfgs = {}");
		ls.add("for _, s in ipairs({");
		Config.configs.values().forEach(c -> ls.add(String.format("{name='%s', type='%s', index='%s', output='%s'},",
			c.getName(), c.getType(), c.getIndex(), c.getOutputDataFile())));
		ls.add("}) do");
		
		ls.add("local c = {}");
		ls.add(String.format("local fs = create_datastream(s.output);"));
		ls.add("for i = 1, fs:get_int() do");
		ls.add("local v = fs['get_' .. s.type](fs)");
		ls.add("c[v[s.index]] = v");
		ls.add("cfgs[s.name] = c");
		ls.add("end");
		ls.add("end");
		
		ls.add("return cfgs");
		
		final String outFile = String.format("%s/%s/configs.lua", Main.codeDir, namespace);
		final String code = ls.stream().collect(Collectors.joining("\n"));
		Utils.save(outFile, code);
	}

}
