package configgen.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
			if(define.isAline()) {
				is.checkLineEnd();
			}
		}
		
		for(String idx : define.getIndexs()) {
			final HashSet<Type> m = new HashSet<Type>();
			for(Type v : values) {
				FStruct s = (FStruct)v;
				m.add(s.getField(idx));
			}
			indexs.put(idx, m);
		}

		define.getEnums().addAll(valueDefine.getEnums());
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
		if(ref.isEmpty()) return;
		final String[] subRef = ref.split("@");
		Type data = Config.getData(subRef[0]);
		if(data instanceof FList) {
			String idx = subRef[1];
			HashSet<Type> validValues = ((FList)data).indexs.get(idx);
			for(Type d : values) {
				if(!validValues.contains(d))
					System.out.println("field:" + define.getName() + " value:" + d + " can't find in index:" + ref);
			}
		} else {
			Map<Type, Type> validValues = ((FMap)data).values;
			for(Type d : values) {
				if(!validValues.containsKey(d))
					System.out.println("field:" + define.getName() + " value:" + d + " can't find in index:" + ref);
			}
		}
	}
	
}
