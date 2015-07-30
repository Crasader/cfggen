package configgen.data;

import configgen.FlatStream;
import configgen.type.Field;

public final class FLong extends Type {

	public FLong(FStruct host, Field define, FlatStream is) {
		super(host, define);
		value = is.getLong();
	}
	
	public final long value;

	public String toString() {
		return "long:" + value;
	}
	
	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FLong)) return false;
		return value == ((FLong)o).value;
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}
}
