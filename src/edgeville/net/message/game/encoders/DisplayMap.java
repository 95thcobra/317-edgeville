package edgeville.net.message.game.encoders;

import java.util.LinkedList;
import java.util.List;

import edgeville.io.RSBuffer;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.util.map.MapDecryptionKeys;

/**
 * @author Simon on 8/22/2014.
 */
public class DisplayMap implements Command { // Aka dipsleemap

	public DisplayMap(Player player) {
		this(player, player.getTile(), true);
	}

	public DisplayMap(Player player, Tile tile, boolean setActive) {
		int x = tile.x;
		int z = tile.z;

		int base_x = x / 8;
		int base_z = z / 8;

		int botleft_x = (base_x - 6) * 8;
		int botleft_z = (base_z - 6) * 8;

		// Update last map
		if (setActive) {
			player.activeMap(new Tile(botleft_x, botleft_z));
		}
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buf = new RSBuffer(player.channel().alloc().buffer(1 + 2 + 2));

		buf.packet(73);
		
		Tile tile = player.getTile();
		System.out.println(tile.getRegionX() + 6+":"+tile.getRegionY() + 6);
		
		buf.writeShortA(tile.getRegionX() + 6);
		buf.writeShort(tile.getRegionY() + 6);
		
		//buf.writeShortA(386);
		//buf.writeShort(405);
		
		

		return buf;
	}
}
