package configgen.type;

import org.w3c.dom.Element;

public final class Const {
	private final String parent;
	private final String name;
	private final String type;
	
	private String value;
	public Const(String parent, Element data) {
		this.parent = parent;
		name = data.getAttribute("name");
		if(name.isEmpty())
			error("name miss");
		final String strType = data.getAttribute("type");
		type = strType.isEmpty() ? "int" : strType;
		value = data.getAttribute("value");
		
		if(value.isEmpty() && !type.equals("string")) {
			error("const type:" + type + " can't be empty!");
		}
	}
	
	public final String getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}

	public final String getValue() {
		return value;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Field{name=").append(name).append(",type=").append(type).append(",value=").append(value).append("}");
		return sb.toString();
	}
	
	public void error(String err) {
		throw new RuntimeException(String.format("%s.%s %s", parent, name, err));
	}
	
	public void verifyDefine() {
		if(type.equals("int")) {
			try {
				Integer.parseInt(value);
			} catch (Exception ex) {
				// 表明它是常量引用
				final int idx = value.lastIndexOf('.');
				final String clsName = idx >= 0 ? value.substring(0, idx) : parent;
				final String cstName = idx >= 0 ? value.substring(idx + 1) : value;
				final Struct struct = Struct.get(clsName);
				if(struct != null) {
					String v = struct.getConstValue(cstName);
					if(v == null)
						error(String.format("const %s not define!", this.value));
					value = v;
				} else {
					final ENUM e = ENUM.get(clsName);
					if(e == null)
						error(String.format("const %s not define!", this.value));
					String v = e.getConstValue(cstName);
					if(v == null)
						error(String.format("const %s not define!", this.value));
					value = v;
				}
			}
		}
	}
	
	public void verifyData() {
		
	}
	
}
