package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.io.RSBuffer.SizeType;
import edgeville.model.entity.Player;
import edgeville.stuff317.MessageBuilder;
import edgeville.stuff317.inputmessage.InputMessageListener;
import io.netty.buffer.Unpooled;

public class Message implements InputMessageListener {

	private String message;

	public Message(String message) {
		this.message = message;
	}

	@Override
	public void handleMessage(Player player, int opcode, int size, MessageBuilder payload) {
		MessageBuilder msg = MessageBuilder.create();
		msg.newVarMessage(253);
		msg.putString(message);
		msg.endVarMessage();
		player.queue(msg);
	}
}