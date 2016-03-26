package cfg.currency;
public final class Currency extends cfg.CfgObject {
	public final static int TYPEID = 1524312676;
	final public int getTypeId() { return TYPEID; }
	public final int type;
	public final String name;
	public final String icon;
	public final int itemid;
	public final String introduction;
	public Currency(cfg.DataStream fs) {
		this.type = fs.getInt();
		this.name = fs.getString();
		this.icon = fs.getString();
		this.itemid = fs.getInt();
		this.introduction = fs.getString();
	}
}