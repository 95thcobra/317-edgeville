package edgeville.net.message.game.decoders;

import edgeville.aquickaccess.actions.ButtonClickAction;
import edgeville.io.RSBuffer;
import edgeville.model.AttributeKey;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Looks.Gender;
import edgeville.net.message.game.encoders.Action;
import edgeville.net.message.game.encoders.PacketInfo;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Simon on 5-2-2015.
 */
@PacketInfo(size = 18)
public class MakeOverDecoder implements Action {

	private int gender;
	private int[] clothes;
	private int[] colors;

	@Override
	public void decode(RSBuffer buf, ChannelHandlerContext ctx, int opcode, int size) {
		gender = buf.readByte();

		clothes = new int[7];
		colors = new int[5];

		for (int i = 0; i < 7; i++) {
			int cloth = buf.readByte();
			
			///////// disable the invisible heads
			if (cloth < 0 && i == 0) {
				int defaultMaleHead = 0;
				int defaultFemaleHead = 45;
				if (gender == Gender.MALE.getId()) {
					cloth = defaultMaleHead;
				} else {
					cloth = defaultFemaleHead;
				}
			}
			////////
			
			clothes[i] = cloth;
		}

		for (int i = 0; i < 5; i++) {
			int color = buf.readByte();
			colors[i] = color;
		}
	}

	@Override
	public void process(Player player) {
		player.looks().setGender(gender);
		player.looks().setClothes(clothes);
		player.looks().setColors(colors);
		player.looks().update();
		player.interfaces().closeMain();
	}
}