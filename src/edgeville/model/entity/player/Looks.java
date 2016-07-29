package edgeville.model.entity.player;

import edgeville.model.entity.Player;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.InterfaceText;
import edgeville.util.EquipmentInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Simon on 8/23/2014.
 */
public class Looks {

	private byte[] calculated;

	private Player player;
	private int transmog = -1;
	private ByteBuf calcBuffer;
	private int[] renderpairOverride;

	private int[] colors;

	public void setClothes(int[] clothes) {
		this.looks[8] = clothes[0]; // hair
		this.looks[11] = clothes[1]; // jaw
		this.looks[4] = clothes[2];// torso
		this.looks[6] = clothes[3]; // arms
		this.looks[9] = clothes[4]; // hands
		this.looks[7] = clothes[5]; // legs
		this.looks[10] = clothes[6]; // feet

	}

	public void setColors(int[] colors) {
		this.colors = colors;
	}

	public int[] getColors() {
		return colors;
	}

	private int[] looks = { 0, 0, 0, 0, 18/* torso */, 0, 27/* arms */, 37/* legs */, 8/* hair */, 33/* hands */,
			42/* boots */, 10/* beard */ };

	public int[] getLooks() {
		return looks;
	}

	private Gender gender;

	public enum Gender {
		MALE(0), FEMALE(1);

		private int id;

		Gender(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	public Looks(Player player) {
		this.player = player;
		calcBuffer = Unpooled.buffer(128);
		setGender(Gender.MALE);

		this.colors = new int[5];
	}

	public void update() {
		calcBuffer.readerIndex(0);
		calcBuffer.writerIndex(0); // Start at 0

		calcBuffer.writeByte(gender.getId()); // Gender
		calcBuffer.writeByte(player.getSkullHeadIcon()); // Skull
		calcBuffer.writeByte(player.getPrayerHeadIcon()); // Prayer

		if (transmog >= 0) {
			calcBuffer.writeShort(0xFFFF).writeShort(transmog);
		} else {

			// int[] looks = {0, 0, 0, 0, 18/*torso*/, 0, 27/*arms*/,
			// 37/*legs*/, 8/*hair*/, 33/*hands*/, 42/*boots*/, 10/*beard*/};

			// 6 = arms
			// 8 = hair
			// 9 = hands
			// 10 = boots
			// 11 = beard
			EquipmentInfo equipInfo = player.world().equipmentInfo();

			boolean hideHead = false;
			boolean hideBeard = false;

			if (player.getEquipment().hasAt(EquipSlot.HEAD)) {
				Item helm = player.getEquipment().get(EquipSlot.HEAD);
				int helmType = equipInfo.typeFor(helm.getId());
				if (helmType == 8) {
					hideHead = true;
				}

				//player.messageDebug("HelmId: %d", helm.getId());
				//player.messageDebug("Helmtype: %d", helmType);

				int[] itemsThatHideBeards = { 4753, 11665, 11664 };
				for (int itemBeard : itemsThatHideBeards) {
					if (helm.getId() == itemBeard) {
						hideBeard = true;
					}
				}
			}

			for (int i = 0; i < 12; i++) {
				if (i == 6 && player.getEquipment().hasAt(4)
						&& equipInfo.typeFor(player.getEquipment().get(4).getId()) == 6) {
					calcBuffer.writeByte(0);
					continue;
				}

				if (i == 8 && hideHead) {
					calcBuffer.writeByte(0);
					continue;
				}

				if (i == 11 && (hideBeard || gender == Gender.FEMALE)) {
					calcBuffer.writeByte(0);
					continue;
				}

				if (player.getEquipment().hasAt(i)) {
					calcBuffer.writeShort(0x200 + player.getEquipment().get(i).getId());
				} else {
					if (looks[i] != 0 || (i == 8 && gender == Gender.MALE)) {
						calcBuffer.writeShort(0x100 + looks[i]);
					} else {
						calcBuffer.writeByte(0);
					}
				}
			}
		}
		// Colors
		for (int i = 0; i < 5; i++) {
			calcBuffer.writeByte(colors[i]);
		}

		int weapon = player.getEquipment().hasAt(EquipSlot.WEAPON) ? player.getEquipment().get(EquipSlot.WEAPON).getId()
				: -1;

		int[] renderpair = renderpairOverride != null ? renderpairOverride
				: player.world().equipmentInfo().renderPair(weapon);
		for (int renderAnim : renderpair) {
			calcBuffer.writeShort(renderAnim); // Renderanim
			//System.out.println(renderAnim + " ->> renderanim");
		}

		/* Str idgaf */
		calcBuffer.writeBytes(player.name().getBytes()).writeByte(0);// with
																		// terminator
																		// 0

		calcBuffer.writeByte(player.skills().combatLevel());
		calcBuffer.writeShort(0);
		calcBuffer.writeByte(0);

		calculated = new byte[calcBuffer.writerIndex()];
		calcBuffer.readerIndex(0);
		calcBuffer.readBytes(calculated);

		player.sync().calculateLooks();
	}

	public void transmog(int id) {
		transmog = id;
		update();
	}

	public void render(int... pair) {
		renderpairOverride = pair;
		update();
	}

	public void resetRender() {
		renderpairOverride = null;
		update();
	}

	public byte[] get() {
		/* Just in case... */
		if (calculated == null)
			update();

		return calculated;
	}

	private static int[] renderFor(int id) {
		return new int[] {};
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public void setGender(int genderNumber) {
		for (Gender gender : Gender.values()) {
			if (gender.getId() == genderNumber) {
				this.gender = gender;
			}
		}
	}
}
