package cfg;
public class CfgMgr {
	public static class DataDir { public static String dir; public static String encoding; }
	public static final java.util.LinkedHashMap<Integer, cfg.role.Profession> profession = new java.util.LinkedHashMap<>();
	public static cfg.role.Name name;
	public static final java.util.LinkedHashMap<Integer, cfg.item.ItemBasic> itembasic = new java.util.LinkedHashMap<>();
	public static final java.util.LinkedHashMap<Integer, cfg.role.Profession2> profession2 = new java.util.LinkedHashMap<>();
	public static cfg.item.ItemConfig itemconfig;
	public static final java.util.LinkedHashMap<Integer, cfg.currency.Currency> currency = new java.util.LinkedHashMap<>();
	public static cfg.role.RoleConfig roleconfig;
	public static final java.util.LinkedHashMap<Integer, cfg.ectype.StoryLayout> storylayout = new java.util.LinkedHashMap<>();
	public static void load() {
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/profession.data", DataDir.encoding);
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.role.Profession v = new cfg.role.Profession(fs);
				profession.put(v.id, v);
			}
		}
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/name.data", DataDir.encoding);
			if(fs.getInt() != 1) throw new RuntimeException("single conifg size != 1");
			name = new cfg.role.Name(fs);
		}
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/item/itembasic.data", DataDir.encoding);
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.item.ItemBasic v = (cfg.item.ItemBasic)fs.getObject(fs.getString());
				itembasic.put(v.id, v);
			}
		}
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/profession2.data", DataDir.encoding);
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.role.Profession2 v = new cfg.role.Profession2(fs);
				profession2.put(v.id, v);
			}
		}
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/item/itemconfig.data", DataDir.encoding);
			if(fs.getInt() != 1) throw new RuntimeException("single conifg size != 1");
			itemconfig = new cfg.item.ItemConfig(fs);
		}
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/currency/currency.data", DataDir.encoding);
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.currency.Currency v = new cfg.currency.Currency(fs);
				currency.put(v.type, v);
			}
		}
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/roleconfig.data", DataDir.encoding);
			if(fs.getInt() != 1) throw new RuntimeException("single conifg size != 1");
			roleconfig = new cfg.role.RoleConfig(fs);
		}
		{
			final cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/ectype/storylayout.data", DataDir.encoding);
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.ectype.StoryLayout v = new cfg.ectype.StoryLayout(fs);
				storylayout.put(v.id, v);
			}
		}
	}
}