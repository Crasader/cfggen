package configgen.type;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;

import configgen.RowColumnStream;
import configgen.FlatStream;
import configgen.Main;
import configgen.Utils;
import configgen.data.DataVisitor;
import configgen.data.FList;
import configgen.data.Type;

public class Config {
	public final static HashMap<String, Config> configs = new HashMap<String, Config>();
	public final static HashSet<String> refStructs = new HashSet<String>();
	
	private final String name;
	private String type;
	private final String dir;
	private final String inputFile;
	private final String outputFile;
	private final String[] indexs;
	private final String[] groups;
	private final HashSet<String> hsGroups = new HashSet<>();
	private final boolean manager; // 是否出现在CfgMgr的加载列表里
	
	private FList data;
	public Config(Element data, String csvDir) {
		dir = csvDir;
		type = data.getAttribute("name");
		name = type.toLowerCase();
		if(configs.put(name, this) != null) {
			Utils.error("config:" + name + " is duplicate!");
		}
		
		inputFile = Utils.combine(dir, data.getAttribute("input"));
		if(data.getAttribute("output").isEmpty())
			Utils.error("config:%s output miss", name);
		outputFile = Utils.combine(dir, data.getAttribute("output"));
		
		groups = Utils.split(data, "group");
		hsGroups.addAll(Arrays.asList(groups));
		if(hsGroups.isEmpty())
			hsGroups.add("all");
		
		indexs = Utils.split(data, "index");
		if(indexs.length != 1)
			Utils.error("config:%s indexs can only have one!", type);
		manager = !data.getAttribute("manager").equals("false");
	}
	
	public String getName() {
		return name;
	}
	
	public final String getType() {
		return type;
	}

	public String getFiles() {
		return inputFile;
	}
	
	public String getIndex() {
		return indexs[0];
	}
	
	public String getOutputDataFile() {
		return outputFile;
	}
	
	public boolean inManager() {
		return manager;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("config{name=").append(name).append(",type=").append(type);
		sb.append(",file=").append(inputFile);
		sb.append("}}");
		return sb.toString();
	}
	
	public void verifyDefine() {
		if(name.isEmpty()) {
			throw new RuntimeException("Config name is missing");
		}
		final String t = Alias.getOriginName(type);
		if(t == null || !Field.isStruct(t)) {
			throw new RuntimeException("config:" + name + " type:" + t + "isn't struct!");
		}
		type = t;
	}
	
	public void loadData() throws Exception {
		System.out.println("==load config:" + name);
		if(inputFile.isEmpty()) return;
		final String fullPath = Utils.combine(Main.csvDir, inputFile);
		final File f = new File(fullPath);
		if(f.isDirectory()) {
			data = new FList(null, new Field(".", name, "list:" + type, 
					new String[]{"list", type},
					indexs,
					new String[]{},
					groups),
					f);
		} else if(!inputFile.endsWith(".xml")) { 
			final FlatStream fs = new RowColumnStream(Utils.parse(fullPath));
			data = new FList(null, new Field(".", name, "list:" + type, 
					new String[]{"list", type},
					indexs,
					new String[]{},
					groups),
					fs);
		} else {
			data = new FList(null, new Field(".", name, "list:" + type, 
					new String[]{"list", type},
					indexs,
					new String[]{},
					groups),
					DocumentBuilderFactory.newInstance().newDocumentBuilder().
        			parse(fullPath).getDocumentElement());
		}
		Main.println(data);
	}
	
	public static void collectRefStructs() {
		for(Struct s : Struct.getStructs().values()) {
			refStructs.add(s.getName());
		}
		//configs.values().forEach(c -> collectRef(c.getType()));
		//Main.println(refStructs);
	}
	
	static void collectRef(String struct) {
		if(!refStructs.add(struct)) return;
		Struct s = Struct.get(struct);
		for(Field f : s.getFields()) {
			for(String t : f.getTypes()) {
				if(Field.isStruct(t)) {
					collectRef(t);
				}
			}
		}
		for(Struct sub : s.getSubTypes()) {
			collectRef(sub.getName());
		}
		if(!s.getBase().isEmpty()) {
			collectRef(s.getBase());
		}
	}
	
	public static HashSet<Type> getData(String name) {
		try {
			return configs.get(name).data.indexs.values().iterator().next();
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("config:" + name + " can't find");
		}
	}
	
	public boolean checkInGroup(Set<String> gs) {
		return Utils.checkInGroup(hsGroups, gs);
	}
	
	public static List<Config> getExportConfigs() {
		return configs.values().stream().filter(c -> c.checkInGroup(Main.groups)).collect(Collectors.toList());
	}
	
	public void save(Set<String> groups) {
		if(inputFile.isEmpty() || !checkInGroup(groups)) return;
		final DataVisitor vs = new DataVisitor(groups);
		data.accept(vs);	
		final String outDataFile = Utils.combine(Main.dataDir, getOutputDataFile());
		Utils.save(outDataFile, vs.toData());
	}
	
	public void verifyData() {
		if(inputFile.isEmpty()) return;
		System.out.println("==verify config:" + name);
		data.verifyData();
	}
	
}
