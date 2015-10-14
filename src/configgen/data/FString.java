package configgen.data;

import org.w3c.dom.Element;

import configgen.FlatStream;
import configgen.type.Field;

public class FString extends Type {

	public FString(FStruct host, Field define, FlatStream is) {
		super(host, define);
		value = is.getString();
	}
	
	public FString(FStruct host, Field define, Element node) {
		super(host, define);
		value = node.getFirstChild() != null ? node.getFirstChild().getTextContent() : "";
	}

	public String value;

	public String toString() {
		return "string:'" + value + "'";
	}
	

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FString)) return false;
		return this.value.equals(((FString)o).value);
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
}
