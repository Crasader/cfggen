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
	
	private final String namespace;
	private final String name;
	private String type;
	private final String dir;
	private final String inputFile;
	private final String outputFile;
	private String[] indexs;
	private final String[] groups;
	private final HashSet<String> hsGroups = new HashSet<>();
	private final boolean manager; // 是否出现在CfgMgr的加载列表里
	private final boolean single;
	
	private FList data;
	public Config(String namespace, Element data, String csvDir) {
		this.namespace = namespace;
		dir = csvDir;
		final String nameStr = data.getAttribute("name");
		type = namespace + "." + nameStr;
		name = nameStr.toLowerCase();
		if(configs.put(name, this) != null) {
			Utils.error("config:" + name + " is duplicate!");
		}
		
		final String inputStr = data.getAttribute("input");
		if(inputStr.isEmpty()) {
			Utils.error("config:%s input is missing!", name);
		}
		inputFile = Utils.combine(dir, inputStr);
		final String outputStr = data.getAttribute("output");
		outputFile = Utils.combine(dir, outputStr.isEmpty() ? name + ".data" : outputStr);
		
		groups = Utils.split(data, "group");
		hsGroups.addAll(Arrays.asList(groups));
		if(hsGroups.isEmpty())
			hsGroups.add("all");
		
		indexs = Utils.split(data, "index");
		if(indexs.length > 1)
			Utils.error("config:%s indexs can only have one!", type);
		else if(indexs.length == 0)
			indexs = new String[] { Struct.get(type).getFields().get(0).getName() };
		manager = !data.getAttribute("manager").equals("false");
		single = data.getAttribute("single").equals("true");
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

	public final boolean isSingle() {
		return single;
	}

	public final String getNamespace() {
		return namespace;
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
		if(type == null || !Field.isStruct(type)) {
			throw new RuntimeException("config:" + name + " type:" + type + "isn't struct!");
		}
	}
	
	public void loadData() throws Exception {
		System.out.println("==load config:" + name);
		final String fullPath = Utils.combine(Main.csvDir, inputFile);
		final File f = new File(fullPath);
		if(f.isDirectory()) {
			data = new FList(null, new Field(null, name, "list:" + type, 
					new String[]{"list", type},
					indexs,
					new String[]{},
					groups),
					f);
		} else if(!inputFile.endsWith(".xml")) { 
			final FlatStream fs = new RowColumnStream(Utils.parse(fullPath));
			data = new FList(null, new Field(null, name, "list:" + type, 
					new String[]{"list", type},
					indexs,
					new String[]{},
					groups),
					fs);
		} else {
			data = new FList(null, new Field(null, name, "list:" + type, 
					new String[]{"list", type},
					indexs,
					new String[]{},
					groups),
					DocumentBuilderFactory.newInstance().newDocumentBuilder().
        			parse(fullPath).getDocumentElement());
		}
		if(isSingle() && data.values.size() != 1)
			Utils.error("config:%s is single. but size=%d", name, data.values.size());
		Main.println(data);
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
		if(!checkInGroup(groups)) return;
		final DataVisitor vs = new DataVisitor(groups);
		data.accept(vs);	
		final String outDataFile = Utils.combine(Main.dataDir, getOutputDataFile());
		Utils.save(outDataFile, vs.toData());
	}
	
	public void verifyData() {
		System.out.println("==verify config:" + name);
		data.verifyData();
	}
	
}
