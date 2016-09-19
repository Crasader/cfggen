package configgen.data;

import configgen.FlatStream;
import configgen.Main;
import configgen.RowColumnStream;
import configgen.Utils;
import configgen.type.Field;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FList extends Type {
	public final List<Type> values = new ArrayList<Type>();
	public final HashMap<String, HashSet<Type>> indexs = new HashMap<String, HashSet<Type>>();
	public FList(FStruct host, Field define) {
		super(host, define);
		for(String idx : define.getIndexs()) {
			indexs.put(idx, new HashSet<Type>());
		}
	}

	public void addValue(Type value) {
		if(host == null) {
			Main.addLastLoadData(value);
		}
		values.add(value);
		for(String idx : define.getIndexs()) {
			final HashSet<Type> m = indexs.get(idx);
			FStruct s = (FStruct)value;
			Type key = s.getField(idx);
			if(!m.add(key))
				throw new RuntimeException(String.format("field:%s idx:%s key:%s duplicate!", define, idx, key));
		}
	}

    public void loadMultiRecordNotCheckEnd(FlatStream is) {
        final Field valueDefine = define.getValueFieldDefine();
        while(!is.isSectionEnd()) {
            addValue(Type.create(host, valueDefine, is));
        }
    }

	public void loadMultiRecord(FlatStream is) {
		final Field valueDefine = define.getValueFieldDefine();
		while(!is.isSectionEnd()) {
			addValue(Type.create(host, valueDefine, is));
		}
		try {
		    is.getString();
            throw new RuntimeException("有部分未读数据,可能是错误地提前输入了列表结束符 ]] !");
        } catch (Exception e) {
            // 读完所有数据后应该不再有有效数据.
        }
	}


	public void loadMultiRecord(Element ele) {
		Field valueDefine = define.getValueFieldDefine();
		final NodeList nodes = ele.getChildNodes();
		for(int i = 0, n = nodes.getLength() ; i < n ; i++) {
			final Node node = nodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				addValue(Type.create(host, valueDefine, (Element)node));
			}
		}
	}

	public void loadOneRecord(File file) throws Exception {
		Field valueDefine = define.getValueFieldDefine();
		try {
			addValue(file.getName().endsWith(".xml") ?
				 Type.create(host, valueDefine, DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement())
				:Type.create(host, valueDefine, new RowColumnStream(Utils.parse(file.getAbsolutePath()))));
		} catch (Exception e) {
			System.out.printf("【加载文件失败】 %s\n", file.getAbsolutePath());
            throw e;
        }
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("List<").append(define.getFullType()).append(">{");
		values.forEach(v -> sb.append(v).append(","));
		sb.append("}");
		return sb.toString();
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public void verifyData() {
        for (Type d : values) {
            if (host == null) {
                Main.setCurVerifyData(d);
            }
            d.verifyData();
        }
    }

	@Override
	public boolean isNull() {
		return false;
	}
	
}
