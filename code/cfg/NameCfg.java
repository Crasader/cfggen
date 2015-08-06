package cfg;
public class NameCfg  {
public final java.util.Map<Integer, String> names = new java.util.HashMap<Integer, String>();
public NameCfg(CSVStream fs) {
while(!fs.isSectionEnd()) {
this.names.put(fs.getInt(), fs.getString());
}
}
}