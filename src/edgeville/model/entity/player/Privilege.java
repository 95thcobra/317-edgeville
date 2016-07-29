package edgeville.model.entity.player;

/**
 * Created by Simon
 */
public enum Privilege {

    PLAYER(0),
    MODERATOR(1),
    ADMINISTRATOR(2),
    DEVELOPER(2);
	
	private int icon;

	Privilege(int icon) {
		this.icon = icon;
	}
	
    public boolean eligibleTo(Privilege p) {
        return ordinal() >= p.ordinal();
    }

	public int getIcon() {
		return icon;
	}
}
