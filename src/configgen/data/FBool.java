package configgen.data;

import configgen.FlatStream;
import configgen.type.Field;

public class FBool extends Type {

	public FBool(FStruct host, Field define, FlatStream is) {
		super(host, define);
		value = is.getBool();
	}
	
	public final boolean value;
	
	public String toString() {
		return "bool:" + value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FBool)) return false;
		return value == ((FBool)o).value;
	}
	
	@Override
	public int hashCode() {
		return Boolean.hashCode(value);
	}
	
}
