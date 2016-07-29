package edgeville.services.login;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.Constants;
import edgeville.crypto.IsaacRand;
import edgeville.database.ForumIntegration;
import edgeville.model.Locations;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.net.future.ClosingChannelFuture;
import edgeville.net.message.LoginRequestMessage;
import edgeville.services.serializers.PlayerLoadResult;
import edgeville.stuff317.ISAACCipher;
import edgeville.stuff317.LoginDetailsMessage;

import java.util.Arrays;

/**
 * @author Simon
 */
public class LoginWorker implements Runnable {

	private static final Logger logger = LogManager.getLogger(LoginWorker.class);

	private LoginService service;
	
	private ForumIntegration forumIntegration = new ForumIntegration();

	public LoginWorker(LoginService service) {
		this.service = service;
	}

	@Override
	public void run() {
		while (true) {
			try {
				LoginDetailsMessage message = service.messages().take();

				if (message == null) {
					continue;
				}
				
				System.out.println("gets here?");
				
				// TODO
				/*if (forumIntegration.checkUser(message.username(), message.password()) != 2) {
					return;
				}*/
				
				logger.info("Attempting to process login request for {}.", message.getUsername());

				ISAACCipher encryptor = message.getEncryptor();
				ISAACCipher decryptor = message.getDecryptor();
				
				Tile startTile = Locations.SPAWN_LOCATION.getTile();
				Player player = new Player(message.channel(), message.getUsername(), message.getPassword(), service.server().world(), startTile, encryptor, decryptor);
				
				boolean success = service.serializer().loadPlayer(player, null, message.getPassword(), result -> {
				System.out.println("Success!");
					
					
					// Convert pipeline
					service.server().initializer().initForGame(message.channel());

					// Was the result faulty?
					if (result != PlayerLoadResult.OK) {
						ByteBuf resp = message.channel().alloc().buffer(1).writeByte(result.code());
						message.channel().writeAndFlush(resp).addListener(new ClosingChannelFuture());
						return;
					}

					// Pass this bit of logic to the server processor
					service.server().processor().submitLogic(() -> {
						
						System.out.println("Here too!");
						
						// Check if we aren't logged in yet :doge:
						if (service.server().world().getPlayerByName(player.name()).isPresent()) {
							ByteBuf resp = message.channel().alloc().buffer(1).writeByte(PlayerLoadResult.ALREADY_ONLINE.code());
							message.channel().writeAndFlush(resp).addListener(new ClosingChannelFuture());
							return;
						}

						// See if we may be registered (world full??)
						if (!service.server().world().registerPlayer(player)) {
							ByteBuf resp = message.channel().alloc().buffer(1).writeByte(PlayerLoadResult.WORLD_FULL.code());
							message.channel().writeAndFlush(resp).addListener(new ClosingChannelFuture());
							return;
						}

						/*ByteBuf temp = message.channel().alloc().buffer(11);
						temp.writeByte(2);

						temp.writeByte(0); // Something trigger bla?
						temp.writeInt(0); // idk this is 4 bytes of isaac ciphered keys

						temp.writeByte(player.getPrivilege() == null ? 0 : player.getPrivilege().ordinal()); // Rights
						temp.writeBoolean(true); // Member
						temp.writeShort(player.index()); // Index
						temp.writeBoolean(true); // Member

						message.channel().writeAndFlush(temp);*/
						ByteBuf resp = Unpooled.buffer(3);
						resp.writeByte(2);
						resp.writeByte(player.getPrivilege() == null ? 0 : player.getPrivilege().ordinal());
						resp.writeByte(0);
						message.channel().writeAndFlush(resp);
						
						LoginService.complete(player, message);
					});
				});

				// Did everything work nicely?
				if (!success) {
					service.enqueue(message); // Let us retry soon :-)
					Thread.sleep(100); // Avoid overloading the login service. Be gentle.
				}
			} catch (Exception e) {
				logger.error("Error processing login worker job!", e);
			}
		}
	}

}
