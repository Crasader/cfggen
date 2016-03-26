package cfg.item;
public final class ItemConfig extends cfg.CfgObject {
	public final static int TYPEID = -17625978;
	final public int getTypeId() { return TYPEID; }
	public final cfg.cmd.condition.FixCurrency bagextendcost;
	public final int initialbagcell;
	public final int maxbagcell;
	public ItemConfig(cfg.DataStream fs) {
		this.bagextendcost = (cfg.cmd.condition.FixCurrency)fs.getObject(fs.getString());
		this.initialbagcell = fs.getInt();
		this.maxbagcell = fs.getInt();
	}
}