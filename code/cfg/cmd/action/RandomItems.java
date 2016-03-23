package cfg.cmd.action;
public final class RandomItems extends cfg.cmd.action.Bonus {
	public final static int TYPEID = -1301042461;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.cmd.action.Item> items = new java.util.ArrayList<cfg.cmd.action.Item>();
	public RandomItems(cfg.DataStream fs) {
		super(fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.items.add((cfg.cmd.action.Item)cfg.DataStream.create("cfg.cmd.action.Item", fs));
		}
	}
}