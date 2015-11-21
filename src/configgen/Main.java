package configgen;



import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import configgen.data.DataGen;
import configgen.type.Config;
import configgen.type.ENUM;
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
	public static String csmarshalcodeDir = "";
	public static String outputEncoding = "utf8";
	public static String inputEncoding = "GBK";
	public static boolean verbose = false;
	public static boolean check = false;
	
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
        System.out.println("    -csmarshalcodedir   csharp marshal code output directory" );
        System.out.println("    -group server:client:all:xxx   group to export, can be multi.");
        System.out.println("    -outputencoding  output encoding. default utf8");
        System.out.println("    -inputencoding   input encoding. default GBK");
        System.out.println("    -verbose  show detail. default not");
        System.out.println("    -check load and check even not set -datadir");
        System.out.println("    --help show usage");
        System.out.println("===============example.xml==============:");
        System.out.println(
        "<configs namespace='cfg.skill'>\n" +
        " <import file='item.xml:buff.xml'/>\n" +
        " <import file='equip.xml'/>\n" +
        " <group name='server:client:designer'/>\n" +
        " <enum name='ProfessionType'>\n" +
        "    <const name='Qinyun' alias='青云门:青云帮'/>枚举值为0\n" +
        "    <const name='Guimen'/>枚举值为1\n" +
        "    <const name='Hehuan' alias='合欢门' value='10'/>枚举值为10\n" +
        "    <const name='Tianyin alias='天音门'/>枚举值为11\n" +
        " </enum>\n" +
        " <struct name='Item'>\n" +
        "      <const name='CONST1' value='12'/>\n" +
        "      <const name='CONST2' type='float' value='1.2'/>\n" +
        "      <const name='const3' type='string' value='xxyy'/>\n" +
        "      <field name='id' type='int' ref='bag'/>引用bag表,id必须为合法的bag id\n" +
        "      <field name='itemid' type='int'/>\n" +
        " </struct>\n" + 
        
        " <config name='Bag' input='bag.xml' output='bag.data' />不指定index的话以第一个字段为index, 不指定output的话以 <name.tolowercase()>.data为输出文件名\n" +
        "    <field name='id' type='int'/>\n" +
        "    <field name='id2' type='int'/>\n" +
        "    <field name='type' type='int'/>\n" +
        " </config>\n" +
        
        "  <config name='Skill' index='id' input='skill.xlsx' output='skill.data' group='server'>此配置只导出server版\n" + 
        "      <field name='profession' type='ProfessionType'/>未指定分组的字段为all分组\n" +
        "      <field name='description' type='string' group='client'/>为client分组\n" +
        "      <field name='level' type='int' group='server'/>\n" +
        "      <field name='designername' type='string' group='designer'/>\n" +
        "      <field name='fbool' type='bool'/>\n" +
        "      <field name='fint' type='int ref='bag'/>\n" +
        "      <field name='flong' type='long'/>\n" +
        "      <field name='fstring' type='string'/>\n" +
        "      <field name='item' type='Item'/>\n" +
        "      <field name='lista' type='list:int' ref='bag'/>列表中每个值都必须为合法的bag id\n" +
        "      <field name='lists' type='set:string'/>\n" +
        "      <field name='mapx' type='map:int:int' ref='bag'/>map中每个key都必须为合法bag id\n" +
        "      <field name='mapx2' type='map:int:int' ref=':bag'/>map中每个value都必须为合法bag id\n" +
        "      <field name='mapx2' type='map:int:int' ref='bag:bag'/>map中每个key和value都必须为合法bag id\n" +
        "      <field name='mapy' type='map:long:Item'/>\n" +
        "      <field name='listz' type='list:Item' index='id:id2'/> 按Item中的id和id2字段分别建立索引(map<int,Item>).只有list:Struct类型可以建立索引\n" + 
        "   </config>\n" +
        
        "</configs>\n"
        		
        		
        		);

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
			case "-csmarshalcodedir":
				csmarshalcodeDir = args[++i];
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
			case "-check":
				check = true;
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
			usage("-codedir miss");
		
		if(codeDir.isEmpty() && dataDir.isEmpty() && csmarshalcodeDir.isEmpty() && !check)
			usage("needs -codeDir or -dataDir or csmarshalcodedir or -check");

        final File cfgxml = new File(xmlSchemeFile);
        final Path parent = cfgxml.toPath().getParent();
        csvDir = parent != null ? parent.toString() : ".";
        Element root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(cfgxml).getDocumentElement();
        
        curXml = xmlSchemeFile;
        loadDefine(root, "");
        dumpDefine();
        verifyDefine();
        
        if(!codeDir.isEmpty() && !languages.isEmpty()) {
	        for(String lan : languages) {
	        		Class<?> cls =  Class.forName("configgen.lans." + lan + ".CodeGen");
	            	Generator generator = (Generator)cls.newInstance();
	            	generator.gen();
	        }
        }
        
        if(!csmarshalcodeDir.isEmpty()) {
        	(new configgen.lans.cs.CodeGen()).genMarshallCode();
        	return;
        }
        
        if(!dataDir.isEmpty() || check) {
	        try {
	        	loadData();
	        	verifyData();
	        } catch(Exception e) {
	        	System.out.println("=================last datas=====================");
	        	lastLoadDatas.forEach(d ->System.out.println(d));
	        	System.out.println("=================last datas=====================");
	        	e.printStackTrace();
	        	System.exit(1);
	        }

	        if(!dataDir.isEmpty()) {
	        	new DataGen().gen();
	        }
        }
	}

	public static String curXml = "";
	public static void loadDefine(Element root, String relateDir) throws Exception {
		final String namespace = root.getAttribute("namespace");
		if(namespace.isEmpty())
			Utils.error("xml:%s configs's attribute<namespace> missing", curXml);
        for(Element ele : Utils.getChildsByTagName(root, "group")) {
        	Group.load(ele);
        }
        
        for(Element ele : Utils.getChildsByTagName(root, "enum")) {
        	new ENUM(namespace, ele);
        }
        
        for(Element ele : Utils.getChildsByTagName(root, "struct")) {
        	new Struct(namespace, ele);
        }
  
        for(Element ele : Utils.getChildsByTagName(root, "config")) {
        	new Struct(namespace, ele);
        	new Config(namespace, ele, relateDir);
        }
        
        for(Element ele : Utils.getChildsByTagName(root, "import")) {
        	for(String file : Utils.split(ele, "input")) {
        		final String oldXml = curXml;
        		curXml = file;
        		final String newRelateDir = file.contains("/") ? Utils.combine(relateDir, file.substring(0, file.lastIndexOf('/'))) : relateDir;
        		loadDefine(DocumentBuilderFactory.newInstance().newDocumentBuilder().
        			parse(Utils.combine(csvDir, relateDir) + "/" + file).getDocumentElement()
        			, newRelateDir);
        		curXml = oldXml;
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
		Config.configs.values().forEach(c -> println(c.toString()));
		Struct.getStructs().values().forEach(s -> println(s.toString()));	
	}
	
	private static void verifyDefine() {
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
		if(lastLoadDatas.size() > 10) {
			lastLoadDatas = lastLoadDatas.subList(5, lastLoadDatas.size());
		}
		lastLoadDatas.add(data);
	}
}
