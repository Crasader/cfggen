package configgen;

import configgen.type.ENUM;
import configgen.type.Struct;
import org.apache.poi.ss.usermodel.*;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Utils {
	
	public static boolean existType(String name) {
		name = name.toLowerCase();
		return Struct.isStruct(name) || ENUM.isEnum(name);
	}
	
	public static void error(String fmt, Object... params) {
		throw new RuntimeException(String.format(fmt, params));
	}
	
	
	public static String combine(String parent, String sub) {
		return parent.isEmpty() ? sub : parent + "/" + sub;
	}
	
	public static String[] split(Element ele, String attr) {
		final String str = ele.getAttribute(attr);
		if(str.isEmpty()) {
			return new String[0];
		} else {
			return str.split(":");
		}
	}

    public static String[] split(Element ele, String attr, String delimiter) {
        final String str = ele.getAttribute(attr);
        if(str.isEmpty()) {
            return new String[0];
        } else {
            return str.split(delimiter);
        }
    }
	
	public static List<Element> getChildsByTagName(Element data, String tag) {
		ArrayList<Element> eles = new ArrayList<Element>();
		final NodeList nodes = data.getChildNodes();
		for(int i = 0 ; i < nodes.getLength() ; i++) {
			final Node node = nodes.item(i);
			if (Node.ELEMENT_NODE != node.getNodeType() || !tag.equals(node.getNodeName())) continue;
			eles.add((Element)node);
		}
		return eles;
	}
	
	public static String getFileExtension(String file) {
		try {
			return file.substring(file.lastIndexOf('.') + 1);
		} catch (Exception e) {
			return "";
		}
	}
    public static String elementToString(Node n) {
        String name = n.getNodeName();
        short type = n.getNodeType();
        if (Node.CDATA_SECTION_NODE == type) {
            return "<![CDATA[" + n.getNodeValue() + "]]&gt;";
        }
        if (name.startsWith("#")) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        sb.append('<').append(name);

        NamedNodeMap attrs = n.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                sb.append(' ').append(attr.getNodeName()).append("=\"").append(attr.getNodeValue()).append(
                        "\"");
            }
        }
        String textContent;
        NodeList children = n.getChildNodes();
        if (children.getLength() == 0) {
            if ((textContent = n.getTextContent()) != null && !"".equals(textContent)) {
                sb.append(textContent).append("</").append(name).append('>');
            } else {
                sb.append("/>").append('\n');
            }
        } else {
            sb.append('>').append('\n');
            boolean hasValidChildren = false;
            for (int i = 0; i < children.getLength(); i++) {
                String childToString = elementToString(children.item(i));
                if (!"".equals(childToString)) {
                    sb.append(childToString);
                    hasValidChildren = true;
                }
            }

            if (!hasValidChildren && ((textContent = n.getTextContent()) != null)) {
                sb.append(textContent);
            }

            sb.append("</").append(name).append('>');
        }
        return sb.toString();
    }

	public static List<List<String>> parseExcelXml(Element ele) {
	    final List<List<String>> lines = new ArrayList<>();
	    final NodeList rows = ele.getElementsByTagName("Row");
	    for(int i = 0 ; i < rows.getLength() ; i++) {
	        final List<String> line = new ArrayList<>();
	        final Element row = (Element)rows.item(i);
	        final NodeList cells = row.getElementsByTagName("ss:Data");
	        final NodeList datas = row.getElementsByTagName("Data");
	        if(cells.getLength() != 0)
	            throw new RuntimeException("excel文件包含了不能识别的 tag <ss:Data><YYY>xxxxx</YYY></ss:Data>,请手动将ss:Data替换成Data,并且去掉内嵌的子tag,变成如右形式 <Data>xxxxx</Data>." + elementToString(row));
	        for(int j = 0 ; j < datas.getLength() ; j++) {
	            final Element c = (Element)datas.item(j);
                String value = c.getFirstChild().getTextContent();
                if(c.getAttribute("ss:Type").equals("Boolean"))
                    value = Boolean.toString(Integer.parseInt(value) == 1);
	            datas.item(j).getFirstChild().getTextContent();
	            if(value.startsWith("##")) break;
	            line.add(value);
            }
            lines.add(line);
        }
        return lines;
    }

	public static Object parseAsXmlOrFlatStream(String file) throws Exception {
        switch(getFileExtension(file)) {
            case "csv": return parseCSV(file);
            case "lne": return parseLineFile(file);
            case "xml": {
                final Element ele = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(file)).getDocumentElement();
                if (ele.getTagName().equals("Workbook")) {
                    // excel 2003 xml格式
                    //System.out.println("parse:" + file);
                    return parseExcelXml(ele);
                } else {
                    return ele;
                }
            }
            default : return parseExcel(file);
        }
    }
	
	private static List<List<String>> parseLineFile(String file) throws Exception {
		final List<String> lines = Files.readAllLines(new File(file).toPath());
		final List<List<String>> rowcol = new ArrayList<>();
		rowcol.add(lines);
		return rowcol;
	}


	static List<List<String>> parseCSV(String file) throws IOException {
		return CSV.parse(new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)), Main.inputEncoding)));
	}
	
	public static List<List<String>> parseExcel(String file) throws Exception {
		final Workbook workbook = WorkbookFactory.create(new File(file));
		final List<List<String>> lines = new ArrayList<>();
		final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		for(int i = 0 , n = workbook.getNumberOfSheets() ; i < n ; i++) {
			final Sheet sheet = workbook.getSheetAt(i);
			for(Row row : sheet) {
				final List<String> line = new ArrayList<>();
				lines.add(line);
				
				for(Cell cell : row) {
		            switch (cell.getCellType()) {
	                case Cell.CELL_TYPE_STRING:
	                    line.add(cell.getRichStringCellValue().getString());
	                    break;
	                case Cell.CELL_TYPE_NUMERIC:
	                    if (DateUtil.isCellDateFormatted(cell)) {
	                        line.add(cell.getDateCellValue().toString());
	                    } else {
	                        line.add(niceConvert(cell.getNumericCellValue()));
	                    }
	                    break;
	                case Cell.CELL_TYPE_BOOLEAN:
	                    line.add(Boolean.toString(cell.getBooleanCellValue()));
	                    break;
	                case Cell.CELL_TYPE_FORMULA:
	                    final CellValue value = evaluator.evaluate(cell);
	                    switch(value.getCellType()) {
	                    case Cell.CELL_TYPE_BOOLEAN: line.add(Boolean.toString(value.getBooleanValue())); break;
	                    case Cell.CELL_TYPE_NUMERIC: line.add(niceConvert(value.getNumberValue())); break;
	                    case Cell.CELL_TYPE_STRING: line.add(value.getStringValue()); break;
	                    case Cell.CELL_TYPE_BLANK : break;
	                    default : throw new RuntimeException("unknown formula result:" + value);
	                    }
	                    break;
	                case Cell.CELL_TYPE_BLANK:
	                	break;
	                default:
	                    throw new RuntimeException("unknown cell type:" + cell.getCellType());
	            }
				}
			}
			
		}
		return lines;	
	}
	
	public static String niceConvert(double value) {
		final long lvalue = (long)value;
		if(lvalue == value) {
			return Long.toString(lvalue);
		} else {
			return Double.toString(value);
		}
		
	}
	
	public static void createDirIfNotExist(String dir) {
		final File dirFile = new File(dir);
		if(!dirFile.exists()) {
			dirFile.mkdirs();
		}
	}
	
	static public void deleteDirectory(String dir) {
		deleteDirectory(new File(dir));
	}
	
	static public void deleteDirectory(File path) {
		if (path.exists()) {
			for (File file : path.listFiles()) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
			path.delete();
		}
	}
	
	public static void save(String file, String text) {
		try {
			File f = new File(file);
			if(!f.getParentFile().exists()) {
				f.getParentFile().mkdirs();
			}
			Files.write(new File(file).toPath(), text.getBytes(Main.outputEncoding));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static boolean checkInGroup(Set<String> toCheck, Set<String> groups) {
		if(toCheck.contains("all")) return true;
		if(groups.contains("all")) return true;
		for(String g : groups) {
			if(toCheck.contains(g))
				return true;
		}
		return false;
	}
	
	public static void main(String[] args) throws Exception {
//		System.out.println(parse("F:/cfggen.git/trunk/csv/test.csv"));
//		System.out.println(parse("F:/cfggen.git/trunk/csv/test.xlsx"));
//		System.out.println(parse("F:/cfggen.git/trunk/csv/test.xls"));
        System.out.println(parseAsXmlOrFlatStream("D:\\workspace\\luxianres\\branches\\test826\\csv\\ectype\\ectypebasic2.xml"));
	}
}
