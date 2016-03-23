package cfg.ectype;
public final class CallSpecialEffect extends cfg.ectype.Action {
	public final static int TYPEID = -219218816;
	final public int getTypeId() { return TYPEID; }
	public final int id;
	public final int time;
	public CallSpecialEffect(cfg.DataStream fs) {
		super(fs);
		this.id = fs.getInt();
		this.time = fs.getInt();
	}
}