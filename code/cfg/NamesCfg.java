package cfg;
public class NamesCfg  {
public final java.util.Map<Integer, String> names = new java.util.HashMap<Integer, String>();
public NamesCfg(CSVStream fs) {
while(!fs.isSectionEnd()) {
this.names.put(fs.getInt(), fs.getString());
}
}
}