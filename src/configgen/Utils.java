package configgen;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class Utils {
	
	public static String[] split(Element ele, String attr) {
		final String str = ele.getAttribute(attr);
		if(str.isEmpty()) {
			return new String[0];
		} else {
			return str.split(":");
		}
	}
	
	public static String concat(String[] strs, String splitor) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String s : strs) {
			if(!first) 
				sb.append(splitor);
			else
				first = false;
			sb.append(s);
		}
		return sb.toString();
	}
	
	public static Collection<Element> getChildsByTagName(Element data, String tag) {
		ArrayList<Element> eles = new ArrayList<Element>();
		final NodeList nodes = data.getChildNodes();
		for(int i = 0 ; i < nodes.getLength() ; i++) {
			final Node node = nodes.item(i);
			if (Node.ELEMENT_NODE != node.getNodeType() || !tag.equals(node.getNodeName())) continue;
			eles.add((Element)node);
		}
		return eles;
	}
	
	public static void createDirIfNotExist(String dir) {
		final File dirFile = new File(dir);
		if(!dirFile.exists()) {
			dirFile.mkdirs();
		}
	}
	
	public static void save(String file, String text) {
		try {
			Files.write(new File(file).toPath(), text.getBytes(Main.outputEncoding));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
