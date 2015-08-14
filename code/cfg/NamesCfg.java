package cfg;
public class NamesCfg  {
public final static int __TYPE_ID__ = 1000003;
public final int getTypeid() { return __TYPE_ID__; }
public final java.util.Map<Integer, String> names = new java.util.HashMap<Integer, String>();
public NamesCfg(CSVStream fs) {
while(!fs.isSectionEnd()) {
this.names.put(fs.getInt(), fs.getString());
}
}
}