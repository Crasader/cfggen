package cfg.ectype;
public final class Case  {
	public final static int TYPEID = 1772688412;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.ectype.ExeCondition> conditions = new java.util.ArrayList<cfg.ectype.ExeCondition>();
	public final cfg.ectype.Action action;
	public Case(cfg.DataStream fs) {
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.conditions.add((cfg.ectype.ExeCondition)cfg.DataStream.create(fs.getString(), fs));
		}
		this.action = (cfg.ectype.Action)cfg.DataStream.create(fs.getString(), fs);
	}
}