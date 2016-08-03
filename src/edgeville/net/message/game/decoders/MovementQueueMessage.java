package edgeville.net.message.game.decoders;

import edgeville.model.entity.Player;
import edgeville.stuff317.ByteOrder;
import edgeville.stuff317.MessageBuilder;
import edgeville.stuff317.ValueType;
import edgeville.stuff317.inputmessage.InputMessageListener;

/**
 * The message sent from the client when a player makes a yellow {@code X} click,
 * a red {@code X} click, or when they click the minimap.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class MovementQueueMessage implements InputMessageListener {

    @Override
    public void handleMessage(Player player, int opcode, int size, MessageBuilder payload) {
    	
    	System.out.println("GOT HERE LOL");
    	
        int steps = (size - 5) / 2;
        int[][] path = new int[steps][2];
        int firstStepX = payload.getShort(ValueType.A, ByteOrder.LITTLE);

        for (int i = 0; i < steps; i++) {
            path[i][0] = payload.get();
            path[i][1] = payload.get();
        }
        int firstStepY = payload.getShort(ByteOrder.LITTLE);
        player.pathQueue().clear();
        player.pathQueue().running(payload.get(ValueType.C) == 1);
        player.pathQueue().step(firstStepX, firstStepY);

        for (int i = 0; i < steps; i++) {
            path[i][0] += firstStepX;
            path[i][1] += firstStepY;
            player.pathQueue().step(path[i][0], path[i][1]);
        }
        //player.getMovementQueue().finish();

        //if (Server.DEBUG)
          //player.getMessages().sendMessage("DEBUG[walking= " + player.getPosition().getRegion() + "]");
    }
}
