package cfg.cmd.action;
public abstract class AddExperience extends cfg.cmd.action.Action {
	public final int amount;
	public AddExperience(cfg.DataStream fs) {
		super(fs);
		this.amount = fs.getInt();
	}
}