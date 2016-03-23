package cfg.ectype;
public final class StoryLayout  {
	public final static int TYPEID = -142124525;
	final public int getTypeId() { return TYPEID; }
	public final int id;
	public final java.util.List<cfg.ectype.Enviroment> enviroments = new java.util.ArrayList<cfg.ectype.Enviroment>();
	public final java.util.List<cfg.ectype.Layout> layouts = new java.util.ArrayList<cfg.ectype.Layout>();
	public final java.util.Map<Integer, cfg.ectype.Layout> layouts_id = new java.util.HashMap<Integer, cfg.ectype.Layout>();
	public final int storyexitscene;
	public StoryLayout(cfg.DataStream fs) {
		this.id = fs.getInt();
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.enviroments.add((cfg.ectype.Enviroment)cfg.DataStream.create("cfg.ectype.Enviroment", fs));
		}
		for(int n = fs.getInt(); n-- > 0 ; ) {
			this.layouts.add((cfg.ectype.Layout)cfg.DataStream.create("cfg.ectype.Layout", fs));
		}
		for(cfg.ectype.Layout _V : this.layouts) {
			this.layouts_id.put(_V.id, _V);
		}
		this.storyexitscene = fs.getInt();
	}
}