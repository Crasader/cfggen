package cfg.ectype;
public final class CircularArea extends cfg.ectype.Area {
	public final static int TYPEID = 1109053624;
	final public int getTypeId() { return TYPEID; }
	public final float radius;
	public CircularArea(cfg.DataStream fs) {
		super(fs);
		this.radius = fs.getFloat();
	}
}