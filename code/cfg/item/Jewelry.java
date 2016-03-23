package cfg.item;
public final class Jewelry extends cfg.item.ItemBasic {
	public final static int TYPEID = -1907341543;
	final public int getTypeId() { return TYPEID; }
	public final int initalvalue;
	public final float maturerate;
	public final int exp;
	public Jewelry(cfg.DataStream fs) {
		super(fs);
		this.initalvalue = fs.getInt();
		this.maturerate = fs.getFloat();
		this.exp = fs.getInt();
	}
}