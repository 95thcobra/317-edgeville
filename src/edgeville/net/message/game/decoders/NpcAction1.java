package edgeville.net.message.game.decoders;

import edgeville.aquickaccess.events.ClickNpcEvent;
import edgeville.io.RSBuffer;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Simon.
 */
@PacketInfo(size = 3)
public class NpcAction1 implements Action {

	private int size = -1;
	private int opcode = -1;
	private boolean run;
	private int index;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		index = buf.readULEShort();
		run = buf.readByteA() == 1;
	}

	@Override
	public void process(Player player) {
		player.stopActions(true);

		player.messageDebug("Npc index: %d",index);
		Npc other = player.world().npcs().get(index);
		int npcId = other.id();

		if (!player.locked() && !player.dead() && !other.dead()) {
			player.stepTowards(other, 20);
			player.face(other);
			player.world().getEventHandler().addEvent(player, new ClickNpcEvent(player, other));
		}
	}
}
