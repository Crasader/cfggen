package cfg.item;
public final class ItemCurrency extends cfg.item.ItemBasic {
	public final static int TYPEID = 1396848725;
	final public int getTypeId() { return TYPEID; }
	public final int amount;
	public ItemCurrency(cfg.DataStream fs) {
		super(fs);
		this.amount = fs.getInt();
	}
}