package configgen.data;

import org.w3c.dom.Element;

import configgen.FlatStream;
import configgen.type.ENUM;
import configgen.type.Field;

public final class FEnum extends Type {

	public FEnum(FStruct host, Field define, FlatStream is) {
		super(host, define);
		enumName = is.getString();
		value = ENUM.get(define.getType()).getEnumValueByName(enumName);
	}
	
	public FEnum(FStruct host, Field define, Element node) {
		super(host, define);
		enumName = node.getFirstChild().getTextContent();
		value = ENUM.get(define.getType()).getEnumValueByName(enumName);
	}

	public final String enumName;
	public final int value;
	
	public String toString() {
		return "enum:" + value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FEnum)) return false;
		return value == ((FEnum)o).value;
	}
	
	@Override
	public int hashCode() {
		return value;
	}

}
