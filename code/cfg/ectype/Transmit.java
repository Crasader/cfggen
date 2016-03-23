package cfg.ectype;
public final class Transmit extends cfg.ectype.Action {
	public final static int TYPEID = -742058596;
	final public int getTypeId() { return TYPEID; }
	public Transmit(cfg.DataStream fs) {
		super(fs);
	}
}