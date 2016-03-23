package cfg.cmd.condition;
public final class Limits extends cfg.cmd.condition.Condition {
	public final static int TYPEID = 90615273;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.cmd.condition.Limit> limits = new java.util.ArrayList<cfg.cmd.condition.Limit>();
	public Limits(cfg.DataStream fs) {
		super(fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.limits.add((cfg.cmd.condition.Limit)cfg.DataStream.create("cfg.cmd.condition.Limit", fs));
		}
	}
}