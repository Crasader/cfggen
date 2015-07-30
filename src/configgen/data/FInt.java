package configgen.data;

import configgen.FlatStream;
import configgen.type.Field;

public final class FInt extends Type {

	public FInt(FStruct host, Field define, FlatStream is) {
		super(host, define);
		value = is.getInt();
	}
	
	public final int value;
	
	public String toString() {
		return "int:" + value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FInt)) return false;
		return value == ((FInt)o).value;
	}
	
	@Override
	public int hashCode() {
		return value;
	}
	
}
