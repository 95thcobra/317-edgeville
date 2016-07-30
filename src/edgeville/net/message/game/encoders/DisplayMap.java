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

	private int x;
	private int z;
	private int localX;
	private int localZ;
	private int level;
	private int[][] xteaKeys;

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

		this.x = base_x;
		this.z = base_z;
		this.localX = x - botleft_x;
		this.localZ = z - botleft_z;
		level = tile.level;

		// Update last map
		if (setActive) {
			player.activeMap(new Tile(botleft_x, botleft_z));
		}
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buf = new RSBuffer(player.channel().alloc().buffer(1 + 2 + 2));

		buf.packet(73);

		//buf.writeShortA(3333);
		//buf.writeShort(3333);
		
		buf.writeShortA(100);
		buf.writeShort(100);

		return buf;
	}
}
