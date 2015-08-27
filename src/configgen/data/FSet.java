package configgen.data;

import java.util.HashSet;
import java.util.Set;

import configgen.FlatStream;
import configgen.type.Config;
import configgen.type.Field;

public class FSet extends Type {
	final public Set<Type> values = new HashSet<Type>();
	public FSet(FStruct host, Field define, FlatStream is) {
		super(host, define);
		Field valueDefine = define.stripAdoreType();
		while(!is.isSectionEnd()) {
			Type value = Type.create(host, valueDefine, is);
			if(!values.add(value)) {
				throw new RuntimeException(String.format("field:%s value:%s duplicate!", define, value));
			}
		}
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Set<").append(define.getFullType()).append(">{");
		values.forEach(v -> sb.append(v).append(","));
		sb.append("}");
		return sb.toString();
	}
	

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public void verifyData() {
		final String ref = define.getRef();
		if(ref.isEmpty()) return;
		HashSet<Type> validValues = Config.getData(ref);
		for(Type d : values) {
			if(!validValues.contains(d))
				System.out.println("field:" + define.getName() + " value:" + d + " can't find in index:" + ref);
		}
	}
}
