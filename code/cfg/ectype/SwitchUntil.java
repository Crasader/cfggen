package cfg.ectype;
public final class SwitchUntil extends cfg.ectype.Action {
	public final static int TYPEID = -841220578;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.ectype.Case> cases = new java.util.ArrayList<cfg.ectype.Case>();
	public SwitchUntil(cfg.DataStream fs) {
		super(fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.cases.add((cfg.ectype.Case)cfg.DataStream.create("cfg.ectype.Case", fs));
		}
	}
}