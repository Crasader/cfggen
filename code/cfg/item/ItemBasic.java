package cfg.item;
public abstract class ItemBasic  {
	public abstract int getTypeId();
	public final int id;
	public final String name;
	public final String owner;
	public final int itemtype;
	public final String icon;
	public final int level;
	public final int quality;
	public final int prize;
	public final cfg.cmd.condition.Gender gender;
	public final cfg.cmd.condition.ProfessionLimit professionlimit;
	public final cfg.cmd.action.BindType bindtype;
	public final String introduction;
	public final cfg.cmd.condition.MinMaxLevel levellimit;
	public final int maxpile;
	public final boolean batch;
	public final boolean cansell;
	public final cfg.cmd.condition.DayLimit daylimit;
	public ItemBasic(cfg.DataStream fs) {
		this.id = fs.getInt();
		this.name = fs.getString();
		this.owner = fs.getString();
		this.itemtype = fs.getInt();
		this.icon = fs.getString();
		this.level = fs.getInt();
		this.quality = fs.getInt();
		this.prize = fs.getInt();
		this.gender = (cfg.cmd.condition.Gender)cfg.DataStream.create("cfg.cmd.condition.Gender", fs);
		this.professionlimit = (cfg.cmd.condition.ProfessionLimit)cfg.DataStream.create("cfg.cmd.condition.ProfessionLimit", fs);
		this.bindtype = (cfg.cmd.action.BindType)cfg.DataStream.create("cfg.cmd.action.BindType", fs);
		this.introduction = fs.getString();
		this.levellimit = (cfg.cmd.condition.MinMaxLevel)cfg.DataStream.create("cfg.cmd.condition.MinMaxLevel", fs);
		this.maxpile = fs.getInt();
		this.batch = fs.getBool();
		this.cansell = fs.getBool();
		this.daylimit = (cfg.cmd.condition.DayLimit)cfg.DataStream.create("cfg.cmd.condition.DayLimit", fs);
	}
}