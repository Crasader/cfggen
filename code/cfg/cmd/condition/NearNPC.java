package cfg.cmd.condition;
public final class NearNPC extends cfg.cmd.condition.Condition {
	public final static int TYPEID = 163745256;
	final public int getTypeId() { return TYPEID; }
	public final int npcid;
	public NearNPC(cfg.DataStream fs) {
		super(fs);
		this.npcid = fs.getInt();
	}
}