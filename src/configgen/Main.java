package configgen;



import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;

import configgen.data.DataGen;
import configgen.type.Alias;
import configgen.type.Config;
import configgen.type.Group;
import configgen.type.Struct;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Main {
	public static String xmlSchemeFile = "";
	public static String csvDir = "";
	public static String codeDir = "";
	public static String dataDir = "";
	public static String outputEncoding = "utf8";
	public static String inputEncoding = "GBK";
	public static boolean verbose = false;
	
	public static final Set<String> languages = new HashSet<String>();
	public static final Set<String> groups = new HashSet<String>();
	
    private static void usage(String reason) {
        System.out.println(reason);

        System.out.println("Usage: java -jar config.jar [options]");
        System.out.println("    -lan cs:lua:java     language type. can be multi.");
        System.out.println("    -configdir       config data directory");
        System.out.println("    -configxml       config xml file");
        System.out.println("    -codedir         output code directory.");
        System.out.println("    -datadir output data directory");
        System.out.println("    -group server:client:all:xxx   group to export, can be multi.");
        System.out.println("    -outputencoding  output encoding. default GBK");
        System.out.println("    -inputencoding   input encoding. default GBK");
        System.out.println("    -verbose  show detail. default not");

        Runtime.getRuntime().exit(1);
    }
	
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < args.length; ++i) {
			switch (args[i]) {
			case "-lan":
				languages.addAll(Arrays.asList(args[++i].split(":")));
				break;
			case "-configdir":
				csvDir = args[++i];
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
			default:
				usage("unknown args " + args[i]);
				break;
			}
		}
		
		if(languages.isEmpty())
			usage("-lan miss");
		if(csvDir.isEmpty())
			usage("-configdir miss");
		if(xmlSchemeFile.isEmpty())
			usage("-configxml miss");
		if(groups.isEmpty())
			usage("-group miss");

		printArgs();
		
        final File cfgxml = new File(csvDir + "/" + xmlSchemeFile);
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(cfgxml).getDocumentElement();
        
        loadDefine(root);
        dumpDefine();
        verifyDefine();
        Config.collectRefStructs();
        
        loadData();
        
        verifyData();

		new DataGen().gen();
        
        for(String lan : languages) {
        	if(!codeDir.isEmpty()) {
        		Class<?> cls =  Class.forName("configgen.lans." + lan + ".CodeGen");
            	Generator generator = (Generator)cls.newInstance();
            	generator.gen();
        	}
//        	if(!dataDir.isEmpty()) {
//        		Class<?> cls =  Class.forName("configgen.lans." + lan + ".DataGen");
//            	Generator generator = (Generator)cls.newInstance();
//            	generator.gen();
//        	}
        	
        }
	}
	
	public static void printArgs() {
		System.out.println("-lan " + languages);
		System.out.println("-configdir " + csvDir);
		System.out.println("-configxml " + xmlSchemeFile);
		System.out.println("-codedir " + codeDir);
		System.out.println("-datadir " + dataDir);
		System.out.println("-group " + groups);
		System.out.println("-inputcoding " + inputEncoding);
		System.out.println("-outputencoding " + outputEncoding);
		System.out.println("-verbose " + verbose);
	}

	public static void loadDefine(Element root) {
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
        	final Config config = new Config(ele);
        	ele.setAttribute("name", config.getType());
        	new Struct(ele);
        }
	}
	
	public static void println(Object s) {
		if(verbose) {
			System.out.println(s);
		}
	}
	
	private static void dumpDefine() {
		println("groups:" + Group.groups);
		println("alias:" + Alias.alias2orgin);
		Config.configs.values().forEach(c -> println(c.toString()));
		Struct.structs.values().forEach(s -> println(s.toString()));
		
	}
	
	private static void verifyDefine() {
		Alias.verityDefine();
		Struct.structs.values().forEach(s -> s.verityDefine());
		Config.configs.values().forEach(c -> c.verifyDefine());
	}
	
	static void loadData() throws IOException {
		for(Config c : Config.configs.values()) {
			c.loadData();
		}
	}
	
	private static void verifyData() {
		for(Config c : Config.configs.values()) {
			c.verifyData();
		}
	}
}
