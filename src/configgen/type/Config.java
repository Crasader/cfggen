package configgen.type;

import configgen.Main;
import configgen.RowColumnStream;
import configgen.Utils;
import configgen.data.DataVisitor;
import configgen.data.FList;
import configgen.data.Type;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Config {
	public final static HashMap<String, Config> configs = new HashMap<String, Config>();
	
	private final String namespace;
	private final String name;
	private String type;
	private final String dir;
	private final String[] inputFiles;
	private final String outputFile;
	private String[] indexs;
	private final String[] groups;
	private final HashSet<String> hsGroups = new HashSet<>();
	private final boolean manager; // 是否出现在CfgMgr的加载列表里
	private final boolean single;
	private final boolean notload;
	
	private FList data;
	public Config(String namespace, Element data, String csvDir) {
		this.namespace = namespace;
		dir = csvDir;
		final String nameStr = data.getAttribute("name");
		if(nameStr.isEmpty())
			Utils.error("config名字为空");
		type = namespace + "." + nameStr;
		name = nameStr.toLowerCase();
		if(configs.put(name, this) != null) {
			Utils.error("config:" + name + " is duplicate!");
		}

		notload = data.getAttribute("input").isEmpty();
//		if(notload) {
//			Utils.error("config:%s input is missing!", name);
//		}
		inputFiles = Utils.split(data, "input");
		for(int i = 0 ; i < inputFiles.length ; i++)
			inputFiles[i] = Utils.combine(Main.csvDir, Utils.combine(dir, inputFiles[i]));
		outputFile = Utils.combine(dir, name + ".data");
		
		groups = Utils.split(data, "group");
		hsGroups.addAll(Arrays.asList(groups));
		if(hsGroups.isEmpty())
			hsGroups.add("all");

		single = data.getAttribute("single").equals("true");
		indexs = Utils.split(data, "index");
		if(indexs.length > 1)
			Utils.error("config:%s 只能有一个index!", type);
		else if(indexs.length == 0 && !single)
			indexs = new String[] { Struct.get(type).getFields().get(0).getName() };
		manager = !data.getAttribute("manager").equals("false");
		this.data = new FList(null, new Field(null, name, "list:" + type,
			new String[]{"list", type},
			indexs,
			new String[]{},
			groups));
	}
	
	public String getName() {
		return name;
	}
	
	public final String getType() {
		return type;
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
		sb.append(",file=").append(inputFiles);
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

	private void loadFrom(String fileName) throws Exception {
		final File file = new File(fileName);
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				if(f.isDirectory()) {
					loadFrom(f.getPath());
				} else {
					data.load(f);
				}
			}
		} else if(fileName.endsWith(".xml")) {
			data.load(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement());
		} else {
			data.load(new RowColumnStream(Utils.parse(fileName)));
		}
	}
	
	public void loadData() throws Exception {
		System.out.println("==load config:" + name);
		if(notload) return;
		for(String file : inputFiles) {
			loadFrom(file);
		}
		if(isSingle() && data.values.size() != 1)
			Utils.error("config:%s is single. but size=%d", name, data.values.size());
		System.out.println("==load config:" + name + ",size:" + data.values.size());
		Main.println(data.values.size());
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
		if(notload || !checkInGroup(groups)) return;
		final DataVisitor vs = new DataVisitor(groups);
		data.accept(vs);
		Utils.save(Utils.combine(Main.dataDir, outputFile), vs.toData());
	}
	
	public void verifyData() {
		System.out.println("==verify config:" + name);
		if(notload) return;
		data.verifyData();
	}
	
}
