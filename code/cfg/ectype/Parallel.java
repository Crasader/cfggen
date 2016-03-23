package cfg.ectype;
public final class Parallel extends cfg.ectype.Action {
	public final static int TYPEID = -851545869;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.ectype.Action> actions = new java.util.ArrayList<cfg.ectype.Action>();
	public Parallel(cfg.DataStream fs) {
		super(fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.actions.add((cfg.ectype.Action)cfg.DataStream.create(fs.getString(), fs));
		}
	}
}