package configgen.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private final String[] inputFiles;
	private final String outputFile;
	private final String[] indexs;
	private final String[] groups;
	private final boolean manager; // 是否出现在CfgMgr的加载列表里
	
	private FList data;
	public Config(Element data, String csvDir) {
		dir = csvDir;
		type = data.getAttribute("name");
		name = type.toLowerCase();
		if(configs.put(name, this) != null) {
			Utils.error("config:" + name + " is duplicate!");
		}
		
		inputFiles = Utils.split(data, "input");
		for(int i = 0 ; i < inputFiles.length ; i++) {
			inputFiles[i] = Utils.combine(dir, inputFiles[i]);
		}
		if(data.getAttribute("output").isEmpty())
			Utils.error("config:%s output miss", name);
		outputFile = Utils.combine(dir, data.getAttribute("output"));
		
		groups = Utils.split(data, "groups");
		
		indexs = Utils.split(data, "indexs");
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

	public String[] getFiles() {
		return inputFiles;
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
		sb.append(",files={");
		for(String f : inputFiles) {
			sb.append(f).append(",");
		}
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
		List<List<String>> lines = new ArrayList<>();
		for(String file : inputFiles) {
			file = Main.csvDir + "/" + file;
			System.out.println("load " + name + ", file:" + file);
			//System.out.println(lines);
			lines.addAll(Utils.parse(file));
		}
		
		final FlatStream fs = new RowColumnStream(lines);
		data = new FList(null, new Field(".", name, "list:" + type, 
				new String[]{"list", type},
				indexs,
				new String[]{},
				groups),
				fs);
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
	
	public void save(Set<String> groups) {
		if(inputFiles.length == 0) return;
		final DataVisitor vs = new DataVisitor(groups);
		data.accept(vs);	
		final String outDataFile = Main.dataDir + "/" + getOutputDataFile();
		Utils.save(outDataFile, vs.toData());
	}
	
	public void verifyData() {
		data.verifyData();
	}
	
}
