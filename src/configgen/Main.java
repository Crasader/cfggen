package configgen;



import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import configgen.data.DataGen;
import configgen.type.Alias;
import configgen.type.Config;
import configgen.type.Group;
import configgen.type.Struct;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Main {
	public static String xmlSchemeFile = "";
	public static String csvDir = "";
	public static String codeDir = "";
	public static String dataDir = "";
	public static String outputEncoding = "utf8";
	public static String inputEncoding = "GBK";
	public static boolean verbose = false;
	public static boolean noverify = false;
	
	public static final Set<String> languages = new HashSet<String>();
	public static final Set<String> groups = new HashSet<String>();
	
	private static List<Object> lastLoadDatas = new ArrayList<>();
	
    private static void usage(String reason) {
        System.out.println(reason);

        System.out.println("Usage: java -jar config.jar [options]");
        System.out.println("    -lan cs:lua:java     language type. can be multi.");
        System.out.println("    -configxml       config xml file");
        System.out.println("    -codedir         output code directory.");
        System.out.println("    -datadir output data directory");
        System.out.println("    -group server:client:all:xxx   group to export, can be multi.");
        System.out.println("    -outputencoding  output encoding. default utf8");
        System.out.println("    -inputencoding   input encoding. default GBK");
        System.out.println("    -verbose  show detail. default not");
        System.out.println("    -noverify no verify reference");
        System.out.println("    --help show usage");

        Runtime.getRuntime().exit(1);
    }
	
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; ++i) {
			switch (args[i]) {
			case "-lan":
				languages.addAll(Arrays.asList(args[++i].split(":")));
				break;
			case "-configxml":
				xmlSchemeFile = args[++i];
				break;
			case "-codedir":
				codeDir = args[++i];
				break;
			case "-datadir":
				dataDir = args[++i];
				break;
			case "-group":
				groups.addAll(Arrays.asList(args[++i].split(":")));
				break;
			case "-outputencoding":
				outputEncoding = args[++i];
				break;
			case "-inputencoding":
				inputEncoding = args[++i];
				break;
			case "-verbose":
				verbose = true;
				break;
			case "-noverify":
				noverify = true;
				break;
			case "--help":
				usage("");
				break;
			default:
				usage("unknown args " + args[i]);
				break;
			}
		}

		if(xmlSchemeFile.isEmpty())
			usage("-configxml miss");
		if(groups.isEmpty())
			usage("-group miss");
		if(codeDir.isEmpty() && !languages.isEmpty())
			usage("-lan miss");

        final File cfgxml = new File(xmlSchemeFile);
        final Path parent = cfgxml.toPath().getParent();
        csvDir = parent != null ? parent.toString() : ".";
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(cfgxml).getDocumentElement();
        
        loadDefine(root, "");
        verifyDefine();
        Config.collectRefStructs();
        try {
        	loadData();
        } catch(Exception e) {
        	System.out.println("=================last datas=====================");
        	lastLoadDatas.forEach(d ->System.out.println(d));
        	System.out.println("=================last datas=====================");
        	e.printStackTrace();
        	System.exit(1);
        }
        
        if(!noverify)
        	verifyData();
        if(!dataDir.isEmpty()) {
        	new DataGen().gen();
        }
        
        if(!codeDir.isEmpty() && !languages.isEmpty()) {
	        for(String lan : languages) {
	        		Class<?> cls =  Class.forName("configgen.lans." + lan + ".CodeGen");
	            	Generator generator = (Generator)cls.newInstance();
	            	generator.gen();
	        }
        }
	}

	public static void loadDefine(Element root, String relateDir) throws Exception {
        for(Element ele : Utils.getChildsByTagName(root, "group")) {
        	Group.load(ele);
        }
        
        for(Element ele : Utils.getChildsByTagName(root, "alias")) {
        	Alias.load(ele);
        }
        
        for(Element ele : Utils.getChildsByTagName(root, "struct")) {
        	new Struct(ele);
        }
  
        for(Element ele : Utils.getChildsByTagName(root, "config")) {
        	new Struct(ele);
        	new Config(ele, relateDir);
        }
        
        for(Element ele : Utils.getChildsByTagName(root, "import")) {
        	for(String file : Utils.split(ele, "input")) {
        		final String newRelateDir = file.contains("/") ? Utils.combine(relateDir, file.substring(0, file.lastIndexOf('/'))) : relateDir;
        		loadDefine(DocumentBuilderFactory.newInstance().newDocumentBuilder().
        			parse(csvDir + "/" + file).getDocumentElement()
        			, newRelateDir);
        	}
        }
        
	}

	
	public static void println(Object s) {
		if(verbose) {
			System.out.println(s);
		}
	}
	
	public static void dumpDefine() {
		println("groups:" + Group.groups);
		println("alias:" + Alias.alias2orgin);
		Config.configs.values().forEach(c -> println(c.toString()));
		Struct.getStructs().values().forEach(s -> println(s.toString()));	
	}
	
	private static void verifyDefine() {
		Alias.verityDefine();
		Struct.getStructs().values().forEach(s -> s.verityDefine());
		Config.configs.values().forEach(c -> c.verifyDefine());
	}
	
	static void loadData() throws Exception {
		for(Config c : Config.configs.values()) {
			c.loadData();
		}
	}
	
	private static void verifyData() {
		for(Config c : Config.configs.values()) {
			c.verifyData();
		}
	}
	
	public static void addLastLoadData(Object data) {
		if(lastLoadDatas.size() > 100) {
			lastLoadDatas = lastLoadDatas.subList(50, lastLoadDatas.size());
		}
		lastLoadDatas.add(data);
	}
}
