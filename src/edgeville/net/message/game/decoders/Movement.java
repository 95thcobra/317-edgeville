package edgeville.net.message.game.decoders;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.io.RSBuffer;
import edgeville.model.Tile;
import edgeville.model.entity.PathQueue;
import edgeville.model.entity.PathQueue.StepType;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Privilege;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import edgeville.script.TimerKey;
import edgeville.stuff317.MessageBuilder;

/**
 * @author Simon
 */
@PacketInfo(size = -1)
public class Movement implements Action {

	private static final Logger logger = LogManager.getLogger();

	private int x;
	private int z;
	private int mode;

	private int steps;
	private int firstStepX;
	private int firstStepY;
	private int[][] path;
	private boolean isRunning;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		//mode = buf.readByteN();
		//z = buf.readUShortA();
		//x = buf.readULEShortA();

		steps = (size - 5) / 2;
		path = new int[steps][2];
		firstStepX = buf.readULEShortA();
		for (int i = 0; i < steps; i++) {
			path[i][0] = buf.readByte();
			path[i][1] = buf.readByte();
		}
		firstStepY = buf.readULEShort();

		isRunning = (buf.readByteC() == 1);

	}

	@Override
	public void process(Player player) {	
		player.message("Current: x:%d, y:%d", player.getTile().x, player.getTile().z);
		player.message("firststepx: %d, firststepy: %d", firstStepX, firstStepY);	
		
		player.pathQueue().clear();
		player.pathQueue().running(isRunning);
		//player.pathQueue().step(firstStepX, firstStepY);
		//player.move(new Tile(firstStepX, firstStepY));
		
		for (int i = 0;i < steps; i++) {
			path[i][0] += firstStepX;
			path[i][1] += firstStepY;
			//player.pathQueue().step(path[i][0], path[i][1]);
			player.message("number:%d-> pathx: %d, pathy: %d",i, path[i][0], path[i][1]);	
			//player.move(new Tile(path[i][0], path[i][1]));
		}
	}
}
