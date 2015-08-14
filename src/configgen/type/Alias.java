package configgen.type;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import configgen.Utils;

public final class Alias {
	// 类别名到实际名字的映射表
	public final static HashMap<String, String> alias2orgin = new HashMap<String, String>();
	
	public static void load(Element data) {
		final String type = data.getAttribute("type");
		final String[] names = Utils.split(data, "names");;
		for(String name : names) {
			alias2orgin.put(name, type);
		}
	}
	
	public static boolean isType(String type) {
		return Field.isRaw(type) || Field.isContainer(type) || Field.isEnum(type) || Field.isStruct(type);
	}
	
	public static String getOriginName(String type) {
		return isType(type) ? type : alias2orgin.get(type);
	}
	
	public static void verityDefine() {
		for(Map.Entry<String, String> e : alias2orgin.entrySet()) {
			final String a = e.getKey();
			final String t = e.getValue();
			if(!isType(t)) {
				throw new RuntimeException("type:" + t + " isn't valid type");
			}
			if(isType(a)) {
				throw new RuntimeException("alias:" + a + " is type!");
			}
		}
	}

}
