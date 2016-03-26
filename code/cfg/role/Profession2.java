package cfg.role;
public final class Profession2 extends cfg.CfgObject {
	public final static int TYPEID = -266349208;
	final public int getTypeId() { return TYPEID; }
	public final int id;
	public final int faction;
	public final int gender;
	public final boolean isopen;
	public final String modelname;
	public final int weight;
	public final int skillactionid;
	public final int defaultweaponid;
	public final int createroleweaponid;
	public final int creatermourid;
	public Profession2(cfg.DataStream fs) {
		this.id = fs.getInt();
		this.faction = fs.getInt();
		this.gender = fs.getInt();
		this.isopen = fs.getBool();
		this.modelname = fs.getString();
		this.weight = fs.getInt();
		this.skillactionid = fs.getInt();
		this.defaultweaponid = fs.getInt();
		this.createroleweaponid = fs.getInt();
		this.creatermourid = fs.getInt();
	}
}