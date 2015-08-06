package configgen;

import java.util.ArrayList;
import java.util.List;

public final class CSVStream extends FlatStream {
	private final List<List<String>> lines;
	private int col;
	private int row;
	
	private final String EOL = "##"; // %#
	private final String END = "]]"; // %]
	//private final String EMPTY = "\"\""; // %"
	
	public static CSVStream Cur;
	
	public CSVStream(List<List<String>> data) {
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
	
	private CSVStream put(String x) {
		if(lines.isEmpty()) {
			lines.add(new ArrayList<String>());
		}
		lines.get(lines.size() - 1).add(x);
		return this;
	}
	
	@Override
	public CSVStream putBool(boolean x) {
		return put(x ? "true" : "false");
	}
	
	@Override
	public int getInt() {
		final String s = getNextAndCheckNotEmpty();
		return Integer.parseInt(s);
	}
	
	@Override
	public CSVStream putInt(int x) {
		return put(Integer.toString(x));
	}
	
	@Override
	public long getLong() {
		final String s = getNextAndCheckNotEmpty();
		return Long.parseLong(s);
	}
	
	@Override
	public CSVStream putLong(long x) {
		return put(Long.toString(x));
	}
	
	@Override
	public float getFloat() {
		final String s = getNextAndCheckNotEmpty();
		return Float.parseFloat(s);
	}
	
	@Override
	public CSVStream putFloat(float x) {
		return put(Float.toString(x));
	}

	@Override
	public String getString() {
		final String s = getNextAndCheckNotEmpty();
		return s.replace("\\#", "#").replace("\\]", "]").replace("\\s", "").replace("\\\\", "\\");
	}
	
	@Override
	public CSVStream putString(String x) {
		if(x.isEmpty()) {
			return put("\\s");
		}
		x = x.replace("\\", "\\\\").replace("]", "\\]").replace("#", "\\#");
		
		if(x.contains(",") || x.contains("\"") || x.contains(" ") || x.contains("\t")) {
			return put("\"" + (x.replace("\"", "\"\"")) + "\"");
		} else {
			return put(x);
		}
	}
	
	@Override
	public CSVStream putSectionEnd() {
		return put(END);
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
