package cfg.item;
public final class ItemConfig  {
	public final static int TYPEID = -17625978;
	final public int getTypeId() { return TYPEID; }
	public final cfg.cmd.condition.FixCurrency bagextendcost;
	public final int initialbagcell;
	public final int maxbagcell;
	public ItemConfig(cfg.DataStream fs) {
		this.bagextendcost = (cfg.cmd.condition.FixCurrency)cfg.DataStream.create(fs.getString(), fs);
		this.initialbagcell = fs.getInt();
		this.maxbagcell = fs.getInt();
	}
}