package edgeville.net;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.Constants;
import edgeville.GameServer;
import edgeville.crypto.IsaacRand;
import edgeville.model.Locations;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.net.message.*;
import edgeville.services.login.LoginService;
import edgeville.stuff317.ISAACCipher;
import edgeville.stuff317.LoginDetailsMessage;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Simon on 8/4/2014.
 */
@ChannelHandler.Sharable
public class LoginHandler extends ChannelInboundHandlerAdapter {

	/**
	 * The logger instance for this class.
	 */
	private static final Logger logger = LogManager.getLogger(LoginHandler.class);

	/**
	 * A reference to the server instance.
	 */
	private GameServer server;

	/**
	 * The spawn tile of new players, as defined in the configuration file.
	 */
	private Tile spawnTile;

	/**
	 * The login service we are going to use when our friends and family log in.
	 * If there's none, all requests are blindly accepted. Easy for development,
	 * but gives you no saving.
	 */
	private LoginService loginService;

	public LoginHandler(GameServer server) {
		this.server = server;

		//spawnTile = new Tile(server.config().getInt("game.spawnpos.x"), server.config().getInt("game.spawnpos.z"));
		spawnTile = new Tile(Constants.SPAWN_TILE);

		// Load our service, if any. Null is a service too :-)
		Optional<LoginService> service = server.service(LoginService.class, false);
		loginService = service.orElse(null);

		if (service.isPresent())
			logger.info("Using {} to process logins.", loginService.getClass().getSimpleName());
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);

		if (msg instanceof PreloginResponseMessage) {
			PreloginResponseMessage pre = (PreloginResponseMessage) msg;
			ctx.writeAndFlush(HandshakeResponse.ALL_OK);
		} else if (msg instanceof LoginDetailsMessage) {
			LoginDetailsMessage message = ((LoginDetailsMessage) msg);

			if (loginService != null) {
				loginService.enqueue(message);
				return;
			}

			String username = message.getUsername();
			String password = message.getPassword();

			logger.trace("Player logged in: {} (password {}).", username, password);

			ISAACCipher encryptor = message.getEncryptor();
			ISAACCipher decryptor = message.getDecryptor();
			
			Player player = new Player(message.getCtx().channel(), message.getUsername(), message.getPassword(), server.world(), Locations.SPAWN_LOCATION.getTile(), encryptor, decryptor);
			player.id(player.getUsername());
			LoginService.complete(player, message);
		}
	}
}
