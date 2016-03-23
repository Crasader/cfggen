package cfg.role;
public final class Profession  {
	public final static int TYPEID = 1653976074;
	final public int getTypeId() { return TYPEID; }
	public final int id;
	public final int faction;
	public final int gender;
	public final boolean isopen;
	public final String modelname;
	public final int weight;
	public final int skillactionid;
	public final int defaultweaponid;
	public final int createweaponid;
	public final int createarmourid;
	public Profession(cfg.DataStream fs) {
		this.id = fs.getInt();
		this.faction = fs.getInt();
		this.gender = fs.getInt();
		this.isopen = fs.getBool();
		this.modelname = fs.getString();
		this.weight = fs.getInt();
		this.skillactionid = fs.getInt();
		this.defaultweaponid = fs.getInt();
		this.createweaponid = fs.getInt();
		this.createarmourid = fs.getInt();
	}
}