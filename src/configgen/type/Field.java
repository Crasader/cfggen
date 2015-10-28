package configgen.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import configgen.Utils;

public final class Field {
	private final Struct parent;
	private final String name;
	private final String fullType;
	private final List<String> types;

	private final HashSet<String> indexs = new HashSet<String>();
	private final HashSet<String> groups = new HashSet<String>();
	private final List<String> refs = new ArrayList<String>();
	
	public final static HashSet<String> RawTypes = new HashSet<String>(Arrays.asList("bool", "int", "float", "long", "string"));
	public final static HashSet<String> ConTypes = new HashSet<String>(Arrays.asList("list", "set", "map"));
	
	public Field(Struct parent, String name, String fulltype, String[] types, String[] indexs, String[] refs, String[] groups) {
		this.parent = parent;
		this.name = name;
		this.fullType = fulltype;
		this.types = Arrays.asList(types);
		if(this.types.isEmpty())
			error("type miss");
		
		for(int i = 0 ; i < types.length ; i++) {
			String t = types[i];
			if(!isRaw(t) && !isContainer(t) && t.indexOf(".") < 0)
				types[i] = parent.getNamespace() + "." + types[i];
		}

		if(name.isEmpty())
			error("name miss");
		
		for(String idx : indexs)
			this.indexs.add(idx.toLowerCase());
		
		if(types[0].equals("list")) {
			final String valueType = types[1];
			if(Field.isStruct(valueType)) {
				this.indexs.addAll(Arrays.asList(indexs));
				Struct s = Struct.get(valueType);
				for(String idx : indexs) {
					if(s.getField(idx) == null)
						error("idx:" + idx + " isn't struct:" + valueType + " field!");
				}
			}
		}
		
		this.refs.addAll(Arrays.asList(refs));
		
//		else if(!name.isEmpty() && isEnum())
//			error("enum can't have name");
		
		for(String groupName : groups) {
			this.groups.add(groupName);
		}
		
		if(this.groups.isEmpty()) 
			this.groups.add("all");
	}
	
	public Field(Struct parent, Element data) {
		this(
			parent, 
			data.getAttribute("name"),
			data.getAttribute("type"),
			Utils.split(data, "type"),
			Utils.split(data, "index"),
			Utils.split(data, "ref"),
			Utils.split(data, "group")
			);	
	}
	
	private Field(Struct parent, String name, String fullType, List<String> types, HashSet<String> groups) {
		this.parent = parent;
		this.name = name;
		this.fullType = fullType;
		this.types = types;
		this.groups.addAll(groups);
	}
	
	public Field stripAdoreType() {
		final List<String> newTypes = types.subList(1, types.size());
		return new Field(parent, name, fullType, newTypes, groups);
	}
	
	public final Struct getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}
	
	public final String getFullType() {
		return fullType;
	}

	public String getType() {
		return types.get(0);
	}
	
	public List<String> getTypes() {
		return types;
	}
	
	public final HashSet<String> getGroups() {
		return groups;
	}
	
	public final String getRef() {
		return refs.size() >= 1 ? refs.get(0) : "";
	}
	
	public final String getKeyRef() {
		return refs.size() >= 1 ? refs.get(0) : "";
	}
	
	public final String getValueRef() {
		return getType().equals("map") ? (refs.size() >= 2 ? refs.get(1) : "") : (refs.size() >= 1 ? refs.get(0) : ""); 
	}

	public final boolean checkInGroup(Set<String> gs) {
		if(groups.contains("all")) return true;
		if(gs.contains("all")) return true;
		for(String g : gs) {
			if(groups.contains(g))
				return true;
		}
		return false;
	}

	public final HashSet<String> getIndexs() {
		return indexs;
	}

	public boolean isRaw() {
		return isRaw(types.get(0));
	}

	public static boolean isRaw(String type) {
		return RawTypes.contains(type);
	}
	
	public boolean isContainer() {
		return isContainer(types.get(0));
	}

	public static boolean isContainer(String type) {
		return ConTypes.contains(type);
	}
	
	public boolean isStruct() {
		return isStruct(types.get(0));
	}
	
	public boolean isDynamic() {
		Struct s = Struct.get(types.get(0));
		return s != null && s.isDynamic();
	}
	
	public static boolean isStruct(String type) {
		return Struct.isStruct(type);
	}
	
	public boolean isEnum() {
		return ENUM.isEnum(types.get(0));
	}
	
	public static boolean isEnum(String type) {
		return ENUM.isEnum(type);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Field{name=").append(name).append(",types={");
		for(String t : types) {
			sb.append(t).append(",");
		}
		sb.append("}, groups={");
		for(String g : groups) {
			sb.append(g).append(",");
		}
		sb.append("}}");
		return sb.toString();
	}
	
	public void checkSize(int n) {
		if(types.size() != n)
			error("type miss");
	}
	
	public void checkType(int idx) {
		if(types.size() <= idx)
			error("type miss");
	}
	
	public void error(String err) {
		throw new RuntimeException(String.format("%s.%s %s", parent, name, err));
	}
	
	public void verifyDefine() {
		checkType(0);
		final String type = getType();
		if(isRaw()) {
			
		} else if(isStruct()) {
			
		} else if(isEnum()) {
			
		} else if(isContainer()) {
			if("map".equals(type)) {
				checkType(1);
				checkType(2);
			} else if("set".equals(type)) {
				checkType(1);
			} else if("list".equals(type)) {
				checkType(1);

				if(!indexs.isEmpty()) {
					final String valueType = types.get(1);
					if(!isStruct(valueType)) {
						error("list value type:" + valueType + "must be struct to indexed");
					}
				}
			}
		} else {
			//error("unknown type:" + type);
		}
		for(String name : groups) {
			if(!Group.isGroup(name))
				error("unknown group:" + name);
		}
	}

}
