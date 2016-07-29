package edgeville.aquickaccess.actions;

import edgeville.combat.Projectile;
import edgeville.model.entity.Player;
import edgeville.model.item.Item;

public class ItemOnItemAction {
	
	private Player player;
	private int item1;
	private int item2;
	private int fromSlot;
	private int toSlot;
	private int interfaceId;
	
	public ItemOnItemAction(Player player, int interfaceId, int fromSlot, int item1,int toSlot, int item2) {
		this.player = player;
		this.item1 = item1;
		this.item2 = item2;
		this.fromSlot = fromSlot;
		this.toSlot = toSlot;
		this.interfaceId = interfaceId;
	}

	public void start() {
		if (interfaceId != 149)
			return;
		
		Item blowpipe = player.getInventory().get(fromSlot);
		//player.message("Item2: %d", blowpipe.getId());
		
		Item dart = player.getInventory().get(toSlot);
		//player.message("Item1: %d", dart.getId());
		
		final int BLOWPIPE = 12926;
		if (item2 == BLOWPIPE && dart.definition(player.world()).name.contains("dart")) {
			player.setBlowpipeAmmo(dart);
			player.message("You have loaded your blowpipe with %d darts.", dart.getAmount());
			player.getInventory().remove(dart, true);
		}
	}
}
