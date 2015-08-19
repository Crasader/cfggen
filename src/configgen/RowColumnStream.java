package configgen;

import java.util.List;

public final class RowColumnStream extends FlatStream {
	private final List<List<String>> lines;
	private int col;
	private int row;
	
	private final String EOL = "##"; // %#
	private final String END = "]]"; // %]
	//private final String EMPTY = "\"\""; // %"
	
	public static RowColumnStream Cur;
	
	public RowColumnStream(List<List<String>> data) {
		lines = data;
		col = row = 0;
		Cur = this;
	}
	
	private String getNext() {
		while(true) {
			if(row >= lines.size()) return null;
			List<String> line = lines.get(row);
			if(col >= line.size()) {
				row++;
				col = 0;
				continue;
			}
			final String data = line.get(col++);
			if(data.startsWith(EOL)) {
				row++;
				col = 0;
				continue;
			}
			if(!data.isEmpty()) {
				return data;
			}
		}
	}
	
	@Override
	public boolean isSectionEnd() {
		while(true) {
			if(row >= lines.size()) return true;
			List<String> line = lines.get(row);
			if(col >= line.size()) {
				row++;
				col = 0;
				continue;
			}
			final String data = line.get(col);
			if(data.startsWith(EOL)) {
				row++;
				col = 0;
				continue;
			}
			if(!data.isEmpty()) {
				if(data.startsWith(END)) {
					col++;
					return true;
				} else {
					return false;
				}
			} else {
				col++;
			}
		}
	}

	@Override
	public void checkLineEnd() {
		while(true) {
			if(row >= lines.size()) return;
			List<String> line = lines.get(row);
			if(col >= line.size()) {
				return;
			}
			final String data = line.get(col);
			if(data.startsWith(EOL)) {
				row++;
				col = 0;
				return;
			}
			if(data.isEmpty()) {
				col++;
			} else {
				error("expect line end.");
			}
		}
	}
	
	private void error(String err) {
		throw new RuntimeException(String.format("%d:%d %s", row, col, err));
	}
	
	private String getNextAndCheckNotEmpty() {
		final String s = getNext();
		if(s == null) 
			error("read not enough");
		return s;
	}
	
	@Override
	public boolean getBool() {
		final String s = getNextAndCheckNotEmpty();
		if(s.equalsIgnoreCase("true"))
			return true;
		else if(s.equalsIgnoreCase("false"))
			return false;
		else 
			error(s + " isn't bool");
		return false;
	}
	
	@Override
	public int getInt() {
		final String s = getNextAndCheckNotEmpty();
		return Integer.parseInt(s);
	}
	
	@Override
	public long getLong() {
		final String s = getNextAndCheckNotEmpty();
		return Long.parseLong(s);
	}
	
	@Override
	public float getFloat() {
		final String s = getNextAndCheckNotEmpty();
		return Float.parseFloat(s);
	}

	@Override
	public String getString() {
		final String s = getNextAndCheckNotEmpty();
		return s.replace("\\#", "#").replace("\\]", "]").replace("\\s", "").replace("\\\\", "\\");
	}
	
	public String toCSVData() {
		StringBuilder sb = new StringBuilder();
		for(List<String> line : lines) {
			for(String s : line) {
				sb.append(s).append(",");
			}
		}
		return sb.toString();
	}


}
