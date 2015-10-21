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
		return structs.get(name.toLowerCase());
	}
	
	public static Struct put(String name, Struct struct) {
		return structs.put(name.toLowerCase(), struct);
	}
	
	public final static HashMap<String, Struct> getStructs() {
		return structs;
	}
	
	public final static boolean isStruct(String name) {
		return structs.containsKey(name.toLowerCase());
	}
	
	public final static boolean isDynamic(String name) {
		Struct s = get(name);
		return s != null && s.isDynamic();
	}
	
	public static List<Struct> getExports() {
		return structs.values().stream().filter(s -> s.checkInGroup(Main.groups)).collect(Collectors.toList());
	}
	
	private final String name;
	private final String base;
	private final ArrayList<Field> fields = new ArrayList<>();
	private final ArrayList<Const> consts = new ArrayList<>();
	private final HashSet<Struct> subs = new HashSet<>();
	private final HashSet<String> groups = new HashSet<>();
	
	public Struct(Element data) {
		this(data, "");
	}
	
	public Struct(Element data, String base) {
		name = data.getAttribute("name");
		System.out.println("== struct:" + name);
		this.base = base;
		if(Utils.existType(name)) {
			error(" is duplicate!");
		}
		put(name, this);
		groups.addAll(Arrays.asList(Utils.split(data, "groups")));
		if(groups.isEmpty())
			groups.add("all");
		final NodeList nodes = data.getChildNodes();
		for(int i = 0 ; i < nodes.getLength() ; i++) {
			final Node node = nodes.item(i);
			if (Node.ELEMENT_NODE != node.getNodeType()) continue;
			Element ele = (Element)node;
			final String nodeName = ele.getNodeName();
			if(nodeName.equals("field")) {
				fields.add(new Field(name, ele));
			} else if(nodeName.equals("struct")){
				subs.add(new Struct(ele, name));
			} else if(nodeName.equals("const")) {
				consts.add(new Const(name, ele));
			} else {
				error("element:" + nodeName + " unknown");
			}
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getBase() {
		return base;
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
	
	public final ArrayList<Const> getConsts() {
		return consts;
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
			throw new RuntimeException("struct:" + name + " unknown base type:" + base);
		}
		
		HashSet<String> fnames = new HashSet<String>();
		for(Field f : fields) {
			if(!fnames.add(f.getName()))
				error("duplicate field:" + f.getName());
		}
		fnames.clear();
		for(Const c : consts) {
			if(!fnames.add(c.getName()))
				error("duplicate const:" + c.getName());
		}
		
//		if(!base.isEmpty()) {
//			Struct.get(base).subs.add(this);
//		}
		fields.stream().forEach(f -> f.verifyDefine());
		
	}

}
