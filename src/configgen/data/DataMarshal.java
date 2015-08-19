package configgen.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class DataMarshal {
	private final List<String> line = new ArrayList<>();
	
	private DataMarshal put(String x) {
		line.add(x);
		return this;
	}
	
	public DataMarshal putBool(boolean x) {
		return put(x ? "true" : "false");
	}
	
	public DataMarshal putInt(int x) {
		return put(Integer.toString(x));
	}
	
	public DataMarshal putLong(long x) {
		return put(Long.toString(x));
	}
	
	public DataMarshal putFloat(float x) {
		return put(Float.toString(x));
	}
	
	public DataMarshal putString(String x) {
		return put(x);
	}
	
	public String toData() {
		return line.stream().collect(Collectors.joining("\n"));
	}
}
