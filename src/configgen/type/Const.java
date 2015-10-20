package configgen.type;

import org.w3c.dom.Element;

public final class Const {
	private final String parent;
	private final String name;
	private final String type;
	
	private final String value;
	public Const(String parent, Element data) {
		this.parent = parent;
		name = data.getAttribute("name");
		if(name.isEmpty())
			error("name miss");
		final String strType = data.getAttribute("type");
		type = strType.isEmpty() ? "int" : strType;
		if(!Field.isRaw(type))
			error("const type:" + type + " must be raw type! ");
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
		
	}
	
	public void verifyData() {
		
	}
	
}
