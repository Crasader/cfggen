package cfg.role;
public final class RoleConfig  {
	public final static int TYPEID = -409859482;
	final public int getTypeId() { return TYPEID; }
	public final int bornmap;
	public final float bornpointx;
	public final float bornpointy;
	public final float viewportminradius;
	public final float viewportmaxradius;
	public final float worldbonus;
	public final float duegonbonus;
	public final int addtiliinterval;
	public final int addtilivalue;
	public final java.util.List<Integer> createroletasks = new java.util.ArrayList<Integer>();
	public RoleConfig(cfg.DataStream fs) {
		this.bornmap = fs.getInt();
		this.bornpointx = fs.getFloat();
		this.bornpointy = fs.getFloat();
		this.viewportminradius = fs.getFloat();
		this.viewportmaxradius = fs.getFloat();
		this.worldbonus = fs.getFloat();
		this.duegonbonus = fs.getFloat();
		this.addtiliinterval = fs.getInt();
		this.addtilivalue = fs.getInt();
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.createroletasks.add(fs.getInt());
		}
	}
}