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
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/profession.data", DataDir.encoding);
			profession.clear();
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.role.Profession v = (cfg.role.Profession)cfg.DataStream.create("cfg.role.Profession", fs);
				profession.put(v.id, v);
			}
		}
		{
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/name.data", DataDir.encoding);
			if(fs.getInt() != 1) throw new RuntimeException("single conifg size != 1");
			name = (cfg.role.Name)cfg.DataStream.create("cfg.role.Name", fs);
		}
		{
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/item/itembasic.data", DataDir.encoding);
			itembasic.clear();
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.item.ItemBasic v = (cfg.item.ItemBasic)cfg.DataStream.create(fs.getString(), fs);
				itembasic.put(v.id, v);
			}
		}
		{
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/profession2.data", DataDir.encoding);
			profession2.clear();
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.role.Profession2 v = (cfg.role.Profession2)cfg.DataStream.create("cfg.role.Profession2", fs);
				profession2.put(v.id, v);
			}
		}
		{
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/item/itemconfig.data", DataDir.encoding);
			if(fs.getInt() != 1) throw new RuntimeException("single conifg size != 1");
			itemconfig = (cfg.item.ItemConfig)cfg.DataStream.create("cfg.item.ItemConfig", fs);
		}
		{
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/currency/currency.data", DataDir.encoding);
			currency.clear();
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.currency.Currency v = (cfg.currency.Currency)cfg.DataStream.create("cfg.currency.Currency", fs);
				currency.put(v.type, v);
			}
		}
		{
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/role/roleconfig.data", DataDir.encoding);
			if(fs.getInt() != 1) throw new RuntimeException("single conifg size != 1");
			roleconfig = (cfg.role.RoleConfig)cfg.DataStream.create("cfg.role.RoleConfig", fs);
		}
		{
			cfg.DataStream fs = cfg.DataStream.create(DataDir.dir + "/ectype/storylayout.data", DataDir.encoding);
			storylayout.clear();
			for(int n = fs.getInt() ; n-- > 0 ; ) {
				final cfg.ectype.StoryLayout v = (cfg.ectype.StoryLayout)cfg.DataStream.create("cfg.ectype.StoryLayout", fs);
				storylayout.put(v.id, v);
			}
		}
	}
}