package configgen;

import java.util.List;

public final class RowColumnStream extends FlatStream {
	private final List<List<String>> lines;
	private int col;
	private int row;
	
	private final String EOL = "##"; // %#
	private final String END = "]]"; // %]
	private final String EMPTY = "\"\"";
	
	public static RowColumnStream Cur;
	
	public RowColumnStream(List<List<String>> data) {
		lines = data;
		col = -1;
		row = 0;
		Cur = this;
	}
	
	private String getNext() {
		while(true) {
			if(row >= lines.size()) return null;
			++col;
			List<String> line = lines.get(row);
			if(col >= line.size()) {
				row++;
				col = -1;
				continue;
			}
			final String data = line.get(col);
			if(data.startsWith(EOL)) {
				row++;
				col = -1;
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
			col++;
			List<String> line = lines.get(row);
			if(col >= line.size()) {
				row++;
				col = -1;
				continue;
			}
			final String data = line.get(col);
			if(data.startsWith(EOL)) {
				row++;
				col = -1;
				continue;
			}
			if(!data.isEmpty()) {
				if(data.startsWith(END)) {
					return true;
				} else {
					col--;
					return false;
				}
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
		throw new RuntimeException(String.format("%d:%d %s", row + 1, col + 1, err));
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
		try {
			return Integer.parseInt(s);
		} catch(Exception e) {
			error(s + " isn't int");
			return -1;
		}
	}
	
	@Override
	public long getLong() {
		final String s = getNextAndCheckNotEmpty();
		try {
			return Long.parseLong(s);
		} catch(Exception e) {
			error(s + " isn't long");
			return -1;
		}
	}
	
	@Override
	public float getFloat() {
		final String s = getNextAndCheckNotEmpty();
		try {
			return Float.parseFloat(s);
		} catch(Exception e) {
			error(s + " isn't float");
			return 0f;
		}
	}

	@Override
	public String getString() {
		final String s = getNextAndCheckNotEmpty();
		if(s.equals(EMPTY)) return "";
		if(s.indexOf('\n') >= 0)
			Utils.error("can't contain \n in string! please choose alternative char.");
		return s.indexOf('%') >= 0 ? s.replace("%#", "#").replace("%]", "]") : s;
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
