package configgen.type;

import configgen.Utils;
import org.w3c.dom.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Field {
	private final Struct parent;
	private final String name;
	private final String fullType;
	private final List<String> types;
	private final boolean localized;

    private String compounddelimiter;

	private final HashSet<String> indexs = new HashSet<>();
	private final HashSet<String> groups = new HashSet<>();
	private final List<String> refs = new ArrayList<>();
    private final List<String> refPath = new ArrayList<>();
	
	private final static HashSet<String> RawTypes = new HashSet<>(Arrays.asList("bool", "int", "float", "long", "string", "text"));
	private final static HashSet<String> ConTypes = new HashSet<>(Arrays.asList("list", "set", "map"));
	private final static HashSet<String> ReserveNames = new HashSet<>(Arrays.asList("end", "base", "super"));//, "typeid", "friend"));

	private final static Pattern namePattern = Pattern.compile("[a-zA-Z]\\w*");
	public Field(Struct parent, String name, String compounddelimiter, String fulltype, String[] types, String[] indexs, String[] refs, String[] refPath, String[] groups) {
		this.parent = parent;
		this.name = name;
		this.fullType = fulltype;
		this.types = Arrays.asList(types);
		if(this.types.isEmpty())
			error("没有定义 type");
		if(fulltype.equalsIgnoreCase("text")) {
			this.localized = true;
			this.types.set(0, "string");
		} else {
			this.localized = false;
		}
		
		for(int i = 0 ; i < types.length ; i++) {
			String t = types[i];
			if(!isRaw(t) && !isContainer(t) && !t.contains("."))
				types[i] = parent.getNamespace() + "." + types[i];
		}

		if(name.isEmpty())
			error("没有定义 name");
		final Matcher matcher = namePattern.matcher(name);
		if(!matcher.matches())
			error("非法变量名:" + name);
		if(ReserveNames.contains(name))
			error("保留关键字:" + name);

        this.compounddelimiter = compounddelimiter;
		for(String idx : indexs)
			this.indexs.add(idx);
		
		this.refs.addAll(Arrays.asList(refs));
        this.refPath.addAll(Arrays.asList(refPath));
		
//		else if(!name.isEmpty() && isEnum())
//			error("enum can't have name");
		
		for(String groupName : groups) {
			this.groups.add(groupName);
		}
		
		if(this.groups.isEmpty()) 
			this.groups.add("all");
	}

	private Field valueFieldDefine;
	private Field mapKeyFieldDefine;
	private Field mapValueFieldDefine;
	
	public Field(Struct parent, Element data) {
		this(
			parent, 
			data.getAttribute("name"),
            data.getAttribute("delimiter"),
			data.getAttribute("type"),
			Utils.split(data, "type"),
			Utils.split(data, "index"),
			Utils.split(data, "ref"),
            Utils.split(data, "refpath", ";|,|:"),
			Utils.split(data, "group")
			);	
	}
	
	private Field(Struct parent, String name, String delimiter, String fullType, List<String> types, HashSet<String> groups, List<String> refs, List<String> refPaths) {
		this.parent = parent;
		this.name = name;
		this.fullType = fullType;
		this.types = types;
		if(types.get(0).equalsIgnoreCase("text")) {
			this.localized = true;
			this.types.set(0, "string");
		} else {
			this.localized = false;
		}
		this.groups.addAll(groups);
        this.compounddelimiter = delimiter;
        this.refs.addAll(refs);
        this.refPath.addAll(refPaths);
	}


	public Field getValueFieldDefine() {
		return valueFieldDefine;
    }

    public Field getMapKeyFieldDefine() {
		return mapKeyFieldDefine;
    }

    public Field getMapValueFieldDefine() {
		return mapValueFieldDefine;
    }

	public boolean isLocalized() {
		return localized;
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

    public boolean isCompound() {
        return !compounddelimiter.isEmpty();
    }

    public String getdelimiter() {
        return compounddelimiter;
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

    public final List<String> getRefPath() {
        return refPath;
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
			error("没有定义 type");
	}
	
	public void checkType(int idx) {
		if(types.size() <= idx)
			error("没有定义 type");
	}
	
	public void error(String err) {
		throw new RuntimeException(String.format("%s.%s %s", parent, name, err));
	}

	public boolean isRawOrEnumOrStruct() {
		return (isRaw()
				|| isEnum()
				|| isStruct());
	}
	public static boolean isRawOrEnumOrStruct(String typeName) {
		return (isRaw(typeName)
			|| isEnum(typeName)
			|| isStruct(typeName));
	}
	
	public void verifyDefine() {
		checkType(0);
		final String type = getType();
		if(isRaw()) {
			
		} else if(isStruct()) {
            if(compounddelimiter.isEmpty())
                compounddelimiter = Struct.get(types.get(0)).getdelimiter();
		} else if(isEnum()) {
			
		} else if(isContainer()) {
			if("map".equals(type)) {
				checkType(1);
				final String keyType = types.get(1);
				if(!isRawOrEnumOrStruct(keyType))
					error("非法的map key类型:" + keyType);
				checkType(2);
				final String valueType = types.get(2);
				if(!isRawOrEnumOrStruct(valueType))
					error("非法的map value类型:" + valueType);

				valueFieldDefine = null;
				Struct s = Struct.get(valueType);
				mapKeyFieldDefine = new Field(parent, name, (s != null ? s.getdelimiter() : ""), fullType, types.subList(1, types.size()), groups,
						refs.isEmpty() ? Collections.emptyList() : Arrays.asList(refs.get(0)),
						Collections.emptyList());
				mapValueFieldDefine =  new Field(parent, name, (s != null ? s.getdelimiter() : ""), fullType, types.subList(1, types.size()), groups, refs, refPath);
			} else if("set".equals(type)) {
				checkType(1);
				final String valueType = types.get(1);
				if(!isRawOrEnumOrStruct(valueType))
					error("非法的set value类型:" + valueType);

				mapKeyFieldDefine = mapValueFieldDefine = null;
				Struct s = Struct.get(valueType);
				valueFieldDefine = new Field(parent, name, (s != null ? s.getdelimiter() : ""), fullType, types.subList(1, types.size()), groups, refs, refPath);
			} else if("list".equals(type)) {
				checkType(1);
				final String valueType = types.get(1);
				if(!isRawOrEnumOrStruct(valueType))
					error("非法的list value类型:" + valueType);
				if(!indexs.isEmpty()) {
					if(!isStruct(valueType)) {
						error("list的 value 类型:" + valueType + "必须是struct才能index");
					}
				}
				mapKeyFieldDefine = mapValueFieldDefine = null;
				Struct s = Struct.get(valueType);
				valueFieldDefine = new Field(parent, name, (s != null ? s.getdelimiter() : ""), fullType, types.subList(1, types.size()), groups, refs, refPath);
			}
			for(int i = 0 ; i < types.size() ; i++) {
				if(types.get(i).equals("text"))
					types.set(i, "string");
			}
		} else {
			error("未知类型:" + type);
		}
		for(String name : groups) {
			if(!Group.isGroup(name))
				error("未知 group:" + name);
		}

		if(types.get(0).equals("list")) {
			final String valueType = types.get(1);
			if(Field.isStruct(valueType)) {
				Struct s = Struct.get(valueType);
				for(String idx : indexs) {
					if(s.getField(idx) == null)
						error("index:" + idx + " 不是struct:" + valueType + " 的字段!");
				}
			}
		}
	}

}
