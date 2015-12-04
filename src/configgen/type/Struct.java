package configgen.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import configgen.Main;
import configgen.Utils;

public final class Struct {
	private final static HashMap<String, Struct> structs = new HashMap<String, Struct>();
	
	public static Struct get(String name) {
		Struct s= structs.get(name.toLowerCase());
		return s != null && s.getFullName().equals(name) ?  s : null;
	}
	
	public static Struct put(String name, Struct struct) {
		return structs.put(name.toLowerCase(), struct);
	}
	
	public final static HashMap<String, Struct> getStructs() {
		return structs;
	}
	
	public final static boolean isStruct(String name) {
		Struct s = structs.get(name.toLowerCase());
		return s != null && s.getFullName().equals(name);
	}
	
	public final static boolean isDynamic(String name) {
		Struct s = get(name);
		return s != null && s.isDynamic();
	}
	
	public static List<Struct> getExports() {
		return structs.values().stream().filter(s -> s.checkInGroup(Main.groups)).collect(Collectors.toList());
	}

	private final int typeid;
	private final String namespace;
	private final String name;
	private final String fullname;
	private final String base;
	private final ArrayList<Field> fields = new ArrayList<>();
	private final ArrayList<Const> consts = new ArrayList<>();
	private final HashSet<Struct> subs = new HashSet<>();
	private final HashSet<String> groups = new HashSet<>();
	
	private final static HashSet<Integer> typeids = new HashSet<>();
	
	public Struct(String namespace, Element data) {
		this(namespace, data, "");
	}
	
	public Struct(String namespace, Element data, String base) {
		this.namespace = namespace;
		name = data.getAttribute("name");
		this.fullname = namespace + "." + name;
		int newTypeid = this.fullname.hashCode();
		while(!typeids.add(newTypeid))
			newTypeid++;
		this.typeid = newTypeid;
		//System.out.printf("== xml:%s struct:%s\n", Main.curXml, fullname);
		this.base = base;
		if(Utils.existType(name)) {
			error(" 类型重复");
		}
		put(fullname, this);
		groups.addAll(Arrays.asList(Utils.split(data, "group")));
		if(groups.isEmpty())
			groups.add("all");
		final NodeList nodes = data.getChildNodes();
		for(int i = 0 ; i < nodes.getLength() ; i++) {
			final Node node = nodes.item(i);
			if (Node.ELEMENT_NODE != node.getNodeType()) continue;
			Element ele = (Element)node;
			final String nodeName = ele.getNodeName();
			if(nodeName.equals("field")) {
				fields.add(new Field(this, ele));
			} else if(nodeName.equals("struct")){
				subs.add(new Struct(namespace, ele, fullname));
			} else if(nodeName.equals("const")) {
				consts.add(new Const(this.fullname, ele));
			} else {
				error("element:" + nodeName + " 未知");
			}
		}
	}
	
	public String getFullName() {
		return fullname;
	}
	
	public String getName() {
		return name;
	}
	
	public String getBase() {
		return base;
	}
	
	public int getTypeId() {
		return typeid;
	}
	
	public boolean isDynamic() {
		return !subs.isEmpty();
	}
	
	public HashSet<Struct> getSubTypes() {
		return subs;
	}
	
	public ArrayList<Field> getFields() {
		return fields;
	}
	
	public Field getField(String name) {
		for(Field f : fields) {
			if(f.getName().equals(name))
				return f;
		}
		return null;
	}
	
	public final String getNamespace() {
		return namespace;
	}

	public final ArrayList<Const> getConsts() {
		return consts;
	}
	
	public final String getConstValue(String name) {
		for(Const c : consts) {
			if(c.getName().equals(name))
				return c.getValue();
		}
		return null;
	}
	
	public final String getConstType(String name) {
		for(Const c : consts) {
			if(c.getName().equals(name))
				return c.getType();
		}
		return null;
	}
	
	public static boolean isDeriveFrom(String child, String ancestor) {
		while(true) {
			if(child.equals(ancestor)) return true;
			if(child.isEmpty()) return false;
			child = Struct.get(child).getBase();
		}
	}
	
	public final boolean checkInGroup(Set<String> gs) {
		return Utils.checkInGroup(groups, gs);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("struct{name=").append(name);
		if(base != null)
			sb.append(",base=").append(base);
		sb.append(",fields:{");
		fields.forEach(f -> sb.append(f).append(","));
		sb.append("},consts:{");
		consts.forEach(f -> sb.append(f).append(","));
		sb.append("},subs:{");
		subs.forEach(sub -> sb.append(sub.name).append(","));
		sb.append("}}");
		return sb.toString();
	}
	
	public void error(String err) {
		throw new RuntimeException("struct:" + name + " err:" + err);
	}
	
	public void verityDefine() {
		if(!base.isEmpty() && Struct.get(base) == null) {
			throw new RuntimeException("struct:" + name + "未知父类:" + base);
		}
		
		HashSet<String> fnames = new HashSet<String>();
		for(Field f : fields) {
			if(!fnames.add(f.getName()))
				error("field名重复:" + f.getName());
		}
		fnames.clear();
		for(Const c : consts) {
			if(!fnames.add(c.getName()))
				error("const名重复:" + c.getName());
			c.verifyDefine();
		}
		
//		if(!base.isEmpty()) {
//			Struct.get(base).subs.add(this);
//		}
		fields.stream().forEach(f -> f.verifyDefine());
		
	}

}
