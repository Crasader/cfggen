package configgen.type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import configgen.CSV;
import configgen.CSVStream;
import configgen.FlatStream;
import configgen.Main;
import configgen.Utils;
import configgen.data.DataVisitor;
import configgen.data.FStruct;
import configgen.data.Type;

public class Config {
	public final static HashMap<String, Config> configs = new HashMap<String, Config>();
	public final static HashSet<String> refStructs = new HashSet<String>();
	
	private final String name;
	private String type;
	private final String[] files;
	
	private configgen.data.FStruct data;
	public Config(Element data) {
		name = data.getAttribute("name");
		if(configs.put(name, this) != null) {
			throw new RuntimeException("config:" + name + " is duplicate!");
		}
		type = data.getAttribute("type");
		files = Utils.split(data, "files");
	}
	
	public String getName() {
		return name;
	}
	
	public final String getType() {
		return type;
	}

	public String[] getFiles() {
		return files;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("config{name=").append(name).append(",type=").append(type);
		sb.append(",files={");
		for(String f : files) {
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
	
	public void loadData() throws IOException {
		List<List<String>> lines = new ArrayList<>();
		for(String file : files) {
			file = Main.csvDir + "/" + file;
			System.out.println("load " + name + ", csv:" + file);
			lines.addAll(CSV.parse(new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)), Main.inputEncoding))));
			//System.out.println(lines);
		}
		
		final FlatStream fs = new CSVStream(lines);
		data = new FStruct(null, null, type, fs);
		Main.println(data.toString());
	}
	
	public static void collectRefStructs() {
		configs.values().forEach(c -> collectRef(c.getType()));
		Main.println(refStructs);
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
	
	public static Type getData(String namePath) {
		try {
			final String[] names = namePath.split("\\.");
			Type type = configs.get(names[0]).data;
			for(int i = 1 ; i < names.length ; i++) {
				type = ((FStruct)type).getField(names[i]);
			}
			return type;
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("index data:" + namePath + " can't find");
		}
	}
	
	public void save(Set<String> groups) {
		final List<List<String>> lines = new ArrayList<List<String>>();
		final CSVStream is = new CSVStream(lines);
		data.accept(new DataVisitor(is, groups));
		
		final String outDataFile = Main.dataDir + "/" + files[0];
		Utils.save(outDataFile, is.toCSVData());
		
	}
	
	public void verifyData() {
		data.veryfyData();
	}
	
}
