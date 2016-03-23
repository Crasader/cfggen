package cfg.ectype;
public final class CollectMineral extends cfg.ectype.Action {
	public final static int TYPEID = -1525242804;
	final public int getTypeId() { return TYPEID; }
	public final java.util.List<cfg.ectype.MissionCollectMineral> missions = new java.util.ArrayList<cfg.ectype.MissionCollectMineral>();
	public final java.util.Map<Integer, cfg.ectype.MissionCollectMineral> missions_mineralid = new java.util.HashMap<Integer, cfg.ectype.MissionCollectMineral>();
	public CollectMineral(cfg.DataStream fs) {
		super(fs);
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.missions.add((cfg.ectype.MissionCollectMineral)cfg.DataStream.create("cfg.ectype.MissionCollectMineral", fs));
		}
		for(cfg.ectype.MissionCollectMineral _V : this.missions) {
			this.missions_mineralid.put(_V.mineralid, _V);
		}
	}
}