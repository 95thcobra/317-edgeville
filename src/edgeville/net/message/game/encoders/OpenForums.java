package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.util.SettingsBuilder;
import io.netty.buffer.Unpooled;

/**
 * @author Simon
 */
public class OpenForums implements Command {

	//private String url;

	public OpenForums() {
		//this.url = url;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(Unpooled.buffer());
		buffer.packet(200);
		return buffer;
	}
}