package configgen.data;

import org.w3c.dom.Element;

import configgen.FlatStream;
import configgen.type.Field;

public class FFloat extends Type {
	public FFloat(FStruct host, Field define, String is) {
		super(host, define);
		value = is.equalsIgnoreCase(NULL_STR) ? NULL_VALUE : Float.parseFloat(is);
	}
	
	public FFloat(FStruct host, Field define, FlatStream is) {
		this(host, define, is.getString());
	}
	
	public FFloat(FStruct host, Field define, Element node) {
		this(host, define, node.getFirstChild().getTextContent());
	}

	public final float value;
	
	public String toString() {
		return String.format("float:%.2f", value);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FFloat)) return false;
		return value == ((FFloat)o).value;
	}
	
	@Override
	public int hashCode() {
		return Float.hashCode(value);
	}
	
}
