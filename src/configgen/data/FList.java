package configgen.data;

import configgen.FlatStream;
import configgen.Main;
import configgen.RowColumnStream;
import configgen.Utils;
import configgen.type.Config;
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

	public void load(FlatStream is) {
		final Field valueDefine = define.stripAdoreType();
		while(!is.isSectionEnd()) {
			addValue(Type.create(host, valueDefine, is));
		}
	}


	public void load(Element ele) {
		Field valueDefine = define.stripAdoreType();
		final NodeList nodes = ele.getChildNodes();
		for(int i = 0, n = nodes.getLength() ; i < n ; i++) {
			final Node node = nodes.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				addValue(Type.create(host, valueDefine, (Element)node));
			}
		}
	}

	public void load(File file) {
		Field valueDefine = define.stripAdoreType();
		try {
			addValue(file.getName().endsWith(".xml") ?
				 Type.create(host, valueDefine, DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement())
				:Type.create(host, valueDefine, new RowColumnStream(Utils.parse(file.getAbsolutePath()))));
		} catch (Exception e) {
			System.out.printf("【加载文件失败】 %s\n", file.getAbsolutePath());
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
		final String ref = define.getRef();
		if(!ref.isEmpty()) {
			HashSet<Type> validValues = Config.getData(ref);
			for(Type d : values) {
				if(!d.isNull() && !validValues.contains(d))
					errorRef(d);
			}
		}
		if(Field.isStruct(define.getTypes().get(1))) {
			for (Type d : values) {
				d.verifyData();
			}
		}
	}

	@Override
	public boolean isNull() {
		return false;
	}
	
}
