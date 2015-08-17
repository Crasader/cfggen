package cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

public final class CSVStream {
	private final List<List<String>> lines;
	private int col;
	private int row;
	
	private final String EOL = "##";
	private final String END = "]]";
	
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
	
	private void error(String err) {
		throw new RuntimeException(String.format("%d:%d %s", (col > 0 ? row : row-1), (col > 0 ? col - 1 : lines.get(row-1).size() - 1), err));
	}
	
	private String getNextAndCheckNotEmpty() {
		final String s = getNext();
		if(s == null) 
			error("read not enough");
		return s;
	}
	
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
	
	public int getInt() {
		final String s = getNextAndCheckNotEmpty();
		return Integer.parseInt(s);
	}
	
	public long getLong() {
		final String s = getNextAndCheckNotEmpty();
		return Long.parseLong(s);
	}
	
	public float getFloat() {
		final String s = getNextAndCheckNotEmpty();
		return Float.parseFloat(s);
	}

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

	public static CSVStream create(String dataFile, String inputEncoding) {
		try {
			return new CSVStream(CSV.parse(new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataFile)), inputEncoding))));
		} catch (Exception e) {
			throw new RuntimeException("data file:" + dataFile + " loads fail!");
		}
	}

}
