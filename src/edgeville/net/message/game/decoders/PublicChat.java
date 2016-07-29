package edgeville.net.message.game.decoders;

import edgeville.Constants;
import edgeville.io.RSBuffer;
import edgeville.model.ChatMessage;
import edgeville.model.entity.Player;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import edgeville.util.HuffmanCodec;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 8/23/2014.
 */
@PacketInfo(size = -1)
public class PublicChat implements Action {

	private int effect;
	private int color;
	private int len;
	private byte[] data;

	@Override
	public void process(Player player) {
		// Decode huffman data
		byte[] stringData = new byte[256];
		HuffmanCodec codec = player.world().server().huffman();
		codec.decode(data, stringData, 0, 0, len);
		String message = new String(stringData, 0, len);
		ChatMessage chatMessage = new ChatMessage(message, effect, color);

		// Check mute
		if (player.isMuted()) {
			player.message("You are muted!");
			return;
		}

		// Check if message is clan message.
		if (chatMessage.text().startsWith("/")) {
			player.getClanChat().message(player, message.substring(1));
			return;
		}

		player.sync().publicChatMessage(chatMessage);
		String log = chatMessage.text();
		player.world().getLogsHandler().appendLog(Constants.CHAT_LOG_DIR + player.getUsername() + ".txt", log);
	}

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		int unid = buf.readByte();
		color = buf.readByte();
		effect = buf.readByte();

		len = buf.readCompact();
		data = new byte[buf.get().readableBytes()];
		buf.get().readBytes(data);
	}

}
