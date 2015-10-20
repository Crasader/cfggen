package configgen.type;

import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Element;

import configgen.Utils;

public final class ENUM {
	public final static HashMap<String, ENUM> enums = new HashMap<>();
	public static boolean isEnum(String name) {
		return enums.containsKey(name.toLowerCase());
	}
	
	public static ENUM get(String name) {
		return enums.get(name.toLowerCase());
	}
	
	public static void put(String name, ENUM e) {
		enums.put(name.toLowerCase(), e);
	}
	
	public static Collection<ENUM> getExports() {
		return enums.values();
	}
	
	public String getName() {
		return name;
	}
	
	public HashMap<String, Integer> getCases() {
		return cases;
	}
	
	private final String name;
	private final HashMap<String, Integer> cases = new HashMap<>();
	private final HashMap<String, String> aliass = new HashMap<>();
	public ENUM(Element ele) {
		name = ele.getAttribute("name");
		if(Utils.existType(name))
			error("duplicate name!");
		put(name, this);
		
		for(Element c : Utils.getChildsByTagName(ele, "const")) {
			final String cname = c.getAttribute("name");
			final String strValue = c.getAttribute("value");
			if(strValue.isEmpty())
				error(String.format("const:%s value missing", cname));
			final int value = Integer.parseInt(c.getAttribute("value"));
			if(cases.put(cname, value) != null)
				error(String.format("const:%s duplicate!", cname));
			aliass.put(cname, cname);
			for(String aliasName : Utils.split(c, "alias")) {
				if(aliass.put(aliasName, cname) != null)
					error(String.format("enum const alias<%s, %s> duplicate!", cname, aliasName));
			}
		}
	}
	
	public int getEnumValueByName(String name) {
		final String cname = aliass.get(name);
		if(cname == null)
			error("name:" + name + " not a valid case name");
		return cases.get(cname);
	}
	
	public void error(String err) {
		Utils.error("enum:%s %s", name, err);
	}
}
