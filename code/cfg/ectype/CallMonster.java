package cfg.ectype;
public final class CallMonster extends cfg.ectype.Action {
	public final static int TYPEID = -2138020560;
	final public int getTypeId() { return TYPEID; }
	public final int id;
	public CallMonster(cfg.DataStream fs) {
		super(fs);
		this.id = fs.getInt();
	}
}