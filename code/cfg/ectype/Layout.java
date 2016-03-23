package cfg.ectype;
public final class Layout  {
	public final static int TYPEID = -1290598122;
	final public int getTypeId() { return TYPEID; }
	public final int id;
	public final String name;
	public final int gfxid;
	public final boolean candrag;
	public final int dragcount;
	public final cfg.ectype.Area area;
	public final int type;
	public final java.util.List<cfg.ectype.Passage> enters = new java.util.ArrayList<cfg.ectype.Passage>();
	public final java.util.Map<Integer, cfg.ectype.Passage> enters_id = new java.util.HashMap<Integer, cfg.ectype.Passage>();
	public final java.util.List<cfg.ectype.Passage> exits = new java.util.ArrayList<cfg.ectype.Passage>();
	public final java.util.Map<Integer, cfg.ectype.Passage> exits_id = new java.util.HashMap<Integer, cfg.ectype.Passage>();
	public final java.util.List<cfg.ectype.Action> scripts = new java.util.ArrayList<cfg.ectype.Action>();
	public final float startrotation;
	public Layout(cfg.DataStream fs) {
		this.id = fs.getInt();
		this.name = fs.getString();
		this.gfxid = fs.getInt();
		this.candrag = fs.getBool();
		this.dragcount = fs.getInt();
		this.area = (cfg.ectype.Area)cfg.DataStream.create(fs.getString(), fs);
		this.type = fs.getInt();
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.enters.add((cfg.ectype.Passage)cfg.DataStream.create("cfg.ectype.Passage", fs));
		}
		for(cfg.ectype.Passage _V : this.enters) {
			this.enters_id.put(_V.id, _V);
		}
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.exits.add((cfg.ectype.Passage)cfg.DataStream.create("cfg.ectype.Passage", fs));
		}
		for(cfg.ectype.Passage _V : this.exits) {
			this.exits_id.put(_V.id, _V);
		}
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.scripts.add((cfg.ectype.Action)cfg.DataStream.create(fs.getString(), fs));
		}
		this.startrotation = fs.getFloat();
	}
}