package configgen.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import configgen.FlatStream;
import configgen.type.Config;
import configgen.type.Field;

public class FList extends Type {
	public final List<Type> values = new ArrayList<Type>();
	public final HashMap<String, HashSet<Type>> indexs = new HashMap<String, HashSet<Type>>();

	public FList(FStruct host, Field define, FlatStream is) {
		super(host, define);
		Field valueDefine = define.stripAdoreType();
		while(!is.isSectionEnd()) {
			values.add(Type.create(host, valueDefine, is));
		}
		
		for(String idx : define.getIndexs()) {
			final HashSet<Type> m = new HashSet<Type>();
			for(Type v : values) {
				FStruct s = (FStruct)v;
				Type key = s.getField(idx);
				if(!m.add(key)) 
					throw new RuntimeException(String.format("field:%s idx:%s key:%s duplicate!", define, idx, key));
			}
			indexs.put(idx, m);
		}
	}
	
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("List<").append(define.getFullType()).append(">{");
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
		if(!ref.isEmpty()) {
			HashSet<Type> validValues = Config.getData(ref);
			for(Type d : values) {
				if(!validValues.contains(d))
					System.out.println("field:" + define.getName() + " value:" + d + " can't find in index:" + ref);
			}
		}
		if(Field.isStruct(define.getTypes().get(1)))
			for(Type d : values) {
				d.verifyData();
			}
	}
	
}
