package cfg.role;
public final class Name  {
	public final static int TYPEID = 736962585;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.role.Names> firstnames = new java.util.ArrayList<cfg.role.Names>();
	public final cfg.role.Names lastnames;
	public final java.util.List<cfg.role.DecorateName> deconames = new java.util.ArrayList<cfg.role.DecorateName>();
	public Name(cfg.DataStream fs) {
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.firstnames.add((cfg.role.Names)cfg.DataStream.create("cfg.role.Names", fs));
		}
		this.lastnames = (cfg.role.Names)cfg.DataStream.create("cfg.role.Names", fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.deconames.add((cfg.role.DecorateName)cfg.DataStream.create("cfg.role.DecorateName", fs));
		}
	}
}