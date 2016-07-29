package edgeville;

import java.math.BigInteger;

import edgeville.model.Locations;
import edgeville.model.Tile;
import edgeville.model.uid.UIDProvider;
import edgeville.model.uid.providers.SimpleUIDProvider;
import edgeville.services.serializers.JSONFileSerializer;
import edgeville.services.serializers.PlayerSerializer;

/**
 * Created by Sky on 28-6-2016.
 */
public class Constants {
	
	// Crucial settings
	public static final boolean MYSQL_ENABLED = false;
	public static final boolean SAVE_PLAYERS = true;
	
	// Settings
	public static final boolean ALL_PVP = true;
	
	public static final String SERVER_NAME = "Edgeville";
	public static final int REVISION = 86;
	public static final boolean FORCE_REVISION = false;
	public static final String CACHE_DIR = "./data/filestore";
	public static final String MAP_KEYS_DIR = "./data/map/keys.bin";

	// Netty config
	public static final int PORT = 43594;
	public static final String IP_ADDRESS = "0.0.0.0";
	public static final int ACCEPT_THREADS = 1;
	public static final int IO_THREADS = 2;

	// UID Provider
	public static final Class UID_PROVIDER = SimpleUIDProvider.class;

	// Lazy load definitions
	public static final boolean LAZY_DEFINITIONS = true;
	public static final Tile SPAWN_TILE = Locations.EDGEVILLE.getTile();

	public static final int WORLD_ID = 1;
	public static final boolean WORLD_EMULATION = true;
	public static final int COMBAT_XP_RATE_MULTIPLIER = 1;
	public static final int SKILLING_XP_RATE_MULTIPLIER = 1;

	public static final boolean DROP_ITEMS_ON_DEATH = false;
	
	
	
	/**
	 * File directories
	 */
	public static final String BANNED_PLAYERS = "./saves/punishment/bannedplayers.txt";
	public static final String MUTED_PLAYERS = "./saves/punishment/mutedplayers.txt";
	public static final String BANNED_IPS = "./saves/punishment/bannedips.txt";
	public static final String MUTED_IPS = "./saves/punishment/mutedips.txt";
	
	/**
	 * Log dirs
	 */
	public static final String COMMAND_LOG_DIR = "./saves/logs/commands/";
	public static final String KILL_LOG_DIR = "./saves/logs/kills/";
	public static final String CHAT_LOG_DIR = "./saves/logs/chat/";
	public static final String DROP_LOG_DIR = "./saves/logs/drops/";
	
	
	/**
	 * Network
	 */
	public static final boolean DECODE_RSA = true;
	public static final BigInteger RSA_MODULUS = new BigInteger("94306533927366675756465748344550949689550982334568289470527341681445613288505954291473168510012417401156971344988779343797488043615702971738296505168869556915772193568338164756326915583511871429998053169912492097791139829802309908513249248934714848531624001166946082342750924060600795950241816621880914628143"), RSA_EXPONENT = new BigInteger("58942123322685908809689084302625256728774551587748168286651364002223076520293763732441711633712538400732268844501356343764421742749024359146319836858905124072353297696448255112361453630421295623429362610999525258756790291981270575779800669035081348981858658116089267888135561190976376091835832053427710797233");

}
