package configgen.data;

import configgen.FlatStream;
import configgen.type.Field;

public class FFloat extends Type {

	public FFloat(FStruct host, Field define, FlatStream is) {
		super(host, define);
		value = is.getFloat();
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