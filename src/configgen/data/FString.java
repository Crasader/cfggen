package configgen.data;

import configgen.Main;
import org.w3c.dom.Element;

import configgen.FlatStream;
import configgen.type.Field;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class FString extends Type {
	public FString(FStruct host, Field define, String is) {
		super(host, define);
		value = is;
	}
	

	private final static String EMPTY = "null";
	public FString(FStruct host, Field define, FlatStream is) {
		super(host, define);
		final String s = is.getString();
		// 因为null用来表示空字符串,%n 存在的意义是为了能够在 string 里配出 null.
		value = s.equals(EMPTY) ? "" :
			(s.indexOf('%') >= 0 ? s.replace("%#", "#").replace("%]", "]").replace("%n", "n") : s);
	}
	
	public FString(FStruct host, Field define, Element node) {
		this(host, define, node.getFirstChild() != null ? node.getFirstChild().getTextContent() : "");
	}

	public String value;

	public String toString() {
		return "string:'" + value + "'";
	}
	

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FString)) return false;
		return this.value.equals(((FString)o).value);
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	@Override
	public boolean isNull() {
		return value.isEmpty();
	}

    public String toFinalPath(String path) {
        return (new File(path).isAbsolute() ? path : Main.csvDir + "/" + path).replace("?", value).replace("*", value.toLowerCase());
    }

    @Override
    public void verifyData() {
        super.verifyData();
        final List<String> refPaths = define.getRefPath();
        if(!refPaths.isEmpty() && !isNull()) {
            final List<String> finalRefPaths = refPaths.stream().map(path -> toFinalPath(path)).collect(Collectors.toList());
            if(finalRefPaths.stream().noneMatch(path -> new File(path).exists())) {
                errorRef(this, finalRefPaths.stream().collect(Collectors.joining("] or [", "[", "]")));
            }
        }
    }
}
