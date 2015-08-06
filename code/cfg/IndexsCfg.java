package cfg;
public class IndexsCfg  {
public final int d1;
public final int d2;
public final int d3;
public final int d4;
public final java.util.List<Integer> e1 = new java.util.ArrayList<Integer>();
public final java.util.Set<Integer> e2 = new java.util.HashSet<Integer>();
public final java.util.Map<Integer, String> e3 = new java.util.HashMap<Integer, String>();
public final java.util.Map<String, Integer> e4 = new java.util.HashMap<String, Integer>();
public final java.util.Map<Integer, Integer> e5 = new java.util.HashMap<Integer, Integer>();
public IndexsCfg(CSVStream fs) {
this.d1 = fs.getInt();
this.d2 = fs.getInt();
this.d3 = fs.getInt();
this.d4 = fs.getInt();
while(!fs.isSectionEnd()) {
this.e1.add(fs.getInt());
}
while(!fs.isSectionEnd()) {
this.e2.add(fs.getInt());
}
while(!fs.isSectionEnd()) {
this.e3.put(fs.getInt(), fs.getString());
}
while(!fs.isSectionEnd()) {
this.e4.put(fs.getString(), fs.getInt());
}
while(!fs.isSectionEnd()) {
this.e5.put(fs.getInt(), fs.getInt());
}
}
}