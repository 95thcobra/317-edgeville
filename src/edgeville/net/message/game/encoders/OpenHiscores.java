package edgeville.net.message.game.encoders;

import edgeville.io.RSBuffer;
import edgeville.model.entity.Player;
import edgeville.util.SettingsBuilder;
import io.netty.buffer.Unpooled;

/**
 * @author Simon
 */
public class OpenHiscores implements Command {

	//private String url;

	public OpenHiscores() {
		//this.url = url;
	}

	@Override
	public RSBuffer encode(Player player) {
		RSBuffer buffer = new RSBuffer(Unpooled.buffer());
		buffer.packet(201);
		return buffer;
	}
}