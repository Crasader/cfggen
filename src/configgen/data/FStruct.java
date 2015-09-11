package configgen.data;

import java.util.ArrayList;

import configgen.FlatStream;
import configgen.Main;
import configgen.type.Field;
import configgen.type.Struct;

public class FStruct extends Type {
	private final String type;
	private ArrayList<Type> values = new ArrayList<Type>();
	public FStruct(FStruct host, Field define, String type, FlatStream is) {
		super(host, define);
		this.type = type;
		load(Struct.get(type), is);
	}
	
	
	
	public final ArrayList<Type> getValues() {
		return values;
	}



	public final void setValues(ArrayList<Type> values) {
		this.values = values;
	}



	public final String getType() {
		return type;
	}
	
	public final Type getField(String name) {
		for(Type t : values) {
			if(t.getDefine().getName().equals(name))
				return t;
		}
		return null;
	}


	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	

	void load(Struct self, FlatStream is) {
		final String base = self.getBase();
		if(!base.isEmpty()) {
			load(Struct.get(base), is);
		}
		for(Field f : self.getFields()) {
			values.add(Type.create(this, f, is));
		}
		// 最近一条读取的数据.便于分析
		if(!self.isDynamic())
			Main.addLastLoadData(this);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("struct<").append(type).append(">{");
		values.forEach(v -> sb.append(v.getDefine().getName()).append(":").append(v).append(","));
		sb.append("}");
		return sb.toString();
	}


	@Override
	public void verifyData() {
		values.forEach(v -> v.verifyData());
	}

}
