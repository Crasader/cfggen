package cfg.item;
public final class Amulet extends cfg.item.ItemBasic {
	public final static int TYPEID = -1974422397;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<Integer> propertyamountweight = new java.util.ArrayList<>();
	public Amulet(cfg.DataStream fs) {
		super(fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.propertyamountweight.add(fs.getInt());
		}
	}
}