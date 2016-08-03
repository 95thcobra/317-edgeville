package edgeville.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import edgeville.model.World;
import edgeville.stuff317.inputmessage.InputMessage;
import edgeville.stuff317.inputmessage.InputMessageListener;
import edgeville.stuff317.inputmessage.NetworkConstants;

import java.util.Collection;
import java.util.Queue;

/**
 * @author Simon on 8/23/2014.
 *
 * Processes the scheduled actions for the players.
 */
public class PacketProcessingTask implements Task {

	private static final Logger logger = LogManager.getLogger(PacketProcessingTask.class);

	@Override
	public void execute(World world) {
		world.players().forEachShuffled(player -> {
			player.pendingActions().forEach(packet -> {
				try {
					packet.process(player);
				} catch (Exception e) {
					logger.error("Error processing message {} for player {}.", packet.getClass().getSimpleName(), player.name());
					logger.error("Caused by: ", e);
				}
			});
			
			player.messageQueue().forEach(packet -> {
				InputMessage msg;
		        while ((msg = player.messageQueue().poll()) != null) {
		            try {
		                InputMessageListener listener = NetworkConstants.MESSAGES[msg.getOpcode()];
		                listener.handleMessage(player, msg.getOpcode(), msg.getSize(), msg.getPayload());
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
			});

			// Remove actions
			player.pendingActions().clear();
			
			player.messageQueue().clear();
		});
	}

	@Override
	public Collection<SubTask> createJobs(World world) {
		return null;
	}

	@Override
	public boolean isAsyncSafe() {
		return false;
	}

}
