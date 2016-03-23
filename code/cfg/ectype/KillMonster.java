package cfg.ectype;
public final class KillMonster extends cfg.ectype.Action {
	public final static int TYPEID = -905870288;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.ectype.MissionKillMonster> missions = new java.util.ArrayList<cfg.ectype.MissionKillMonster>();
	public final java.util.Map<Integer, cfg.ectype.MissionKillMonster> missions_monsterid = new java.util.HashMap<Integer, cfg.ectype.MissionKillMonster>();
	public KillMonster(cfg.DataStream fs) {
		super(fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.missions.add((cfg.ectype.MissionKillMonster)cfg.DataStream.create("cfg.ectype.MissionKillMonster", fs));
		}
		for(cfg.ectype.MissionKillMonster _V : this.missions) {
			this.missions_monsterid.put(_V.monsterid, _V);
		}
	}
}