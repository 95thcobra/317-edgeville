package edgeville.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edgeville.io.RSBuffer;
import edgeville.model.Tile;
import edgeville.model.World;
import edgeville.model.entity.Player;
import edgeville.model.entity.SyncInfo;
import edgeville.model.entity.player.PlayerSyncInfo;
import edgeville.net.message.game.encoders.UpdatePlayers;
import edgeville.stuff317.BitMask;
import edgeville.stuff317.ByteOrder;
import edgeville.stuff317.Flag;
import edgeville.stuff317.MessageBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Simon on 8/23/2014.
 */
public class PlayerSyncTask implements Task {

	private static final Logger logger = LogManager.getLogger(PlayerSyncTask.class);

	static class Job extends SubTask {

		/**
		 * Preallocated integer array to avoid continuous reallocation.
		 */
		private int[] rebuilt = new int[2048];
		private Player[] players;

		public Job(World world, Player... players) {
			super(world);
			this.players = players;
		}

		@Override
		public void execute() {
			for (Player player : players)
				sync(player);
		}
		
		private void updateFlags(Player player, RSBuffer buffer) {
			if (player.sync().hasFlag(PlayerSyncInfo.Flag.LOOKS.value) || player.sync().isNewlyAdded(player.index()))
				buffer.get().writeBytes(player.sync().looksBlock());
		}

		private void sync(Player player) {			
			RSBuffer buffer = new RSBuffer(player.channel().alloc().buffer(512));
			//RSBuffer block = new RSBuffer(player.channel().alloc().buffer(512));
			MessageBuilder block = MessageBuilder.create(8192);
			buffer.packet(81).writeSize(RSBuffer.SizeType.SHORT);

			buffer.startBitMode();
			updateMyPlayer(player, buffer);
			
			if (player.sync().dirty()) {
				//updateState(player, player, block, false, true);
			}
			
			updateOtherPlayers(player, buffer);
			updatePlayerList(player, buffer);
			
			
			//buffer.endBitMode();
			
			
			if (block.buffer().writerIndex() > 0) {
				buffer.writeBits(11, 2047);
				buffer.endBitMode();
				buffer.get().writeBytes(block.buffer());
			} else {
				buffer.endBitMode();
			}
			
			player.write(new UpdatePlayers(buffer));
		}
		
		private static void updateState(Player player, Player thisPlayer, MessageBuilder block, boolean forceAppearance, boolean noChat) {
			if (!player.sync().dirty() && !forceAppearance)
				return;
			/*if (player.getCachedUpdateBlock() != null && !player.equals(thisPlayer) && !forceAppearance && !noChat) {
				block.putBytes(player.getCachedUpdateBlock());
				return;
			}*/
			MessageBuilder cachedBuffer = MessageBuilder.create(300);
			BitMask mask = new BitMask();

			if (player.getFlags().get(Flag.FORCED_MOVEMENT)) {
				mask.set(0x400);
			}
			if (player.getFlags().get(Flag.GRAPHICS)) {
				mask.set(0x100);
			}
			if (player.getFlags().get(Flag.ANIMATION)) {
				mask.set(8);
			}
			if (player.getFlags().get(Flag.FORCED_CHAT)) {
				mask.set(4);
			}
			if (player.getFlags().get(Flag.CHAT) && !noChat) {
				mask.set(0x80);
			}
			if (player.getFlags().get(Flag.APPEARANCE) || forceAppearance) {
				mask.set(0x10);
			}
			if (player.getFlags().get(Flag.FACE_CHARACTER)) {
				mask.set(1);
			}
			if (player.getFlags().get(Flag.FACE_COORDINATE)) {
				mask.set(2);
			}
			if (player.getFlags().get(Flag.HIT)) {
				mask.set(0x20);
			}
			if (player.getFlags().get(Flag.HIT_2)) {
				mask.set(0x200);
			}
			if (mask.get() >= 0x100) {
				mask.set(0x40);
				cachedBuffer.putShort(mask.get(), ByteOrder.LITTLE);
			} else {
				cachedBuffer.put(mask.get());
			}

			if (player.getFlags().get(Flag.FORCED_MOVEMENT)) {
				// appendForcedMovement(player, cachedBuffer);
			}
			/*if (player.getFlags().get(Flag.GRAPHICS)) {
				appendGraphic(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.ANIMATION)) {
				appendAnimation(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.FORCED_CHAT)) {
				appendForcedChat(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.CHAT) && !noChat) {
				appendChat(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.FACE_CHARACTER)) {
				appendFaceCharacter(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.APPEARANCE) || forceAppearance) {
				appendAppearance(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.FACE_COORDINATE)) {
				appendFaceCoordinates(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.HIT)) {
				appendPrimaryHit(player, cachedBuffer);
			}
			if (player.getFlags().get(Flag.HIT_2)) {
				appendSecondaryHit(player, cachedBuffer);
			}
			if (!player.equals(thisPlayer) && !forceAppearance && !noChat) {
				player.setCachedUpdateBlock(cachedBuffer.buffer());
			}*/
			block.putBytes(cachedBuffer.buffer());
		}

		private void updateMyPlayer(Player player, RSBuffer buffer) {
			boolean needsUpdate = player.sync().dirty();

			if (needsUpdate) {
				buffer.writeBits(1, 1);

				int primaryStep = player.sync().primaryStep();
				int secondaryStep = player.sync().secondaryStep();

				if (player.sync().teleported()) {
					buffer.writeBits(2, 3); // Teleport

					int mapx = player.activeMap().x;
					int mapz = player.activeMap().z;
					int dx = player.getTile().x - mapx;
					int dz = player.getTile().z - mapz;

					buffer.writeBits(7, dz);
					buffer.writeBits(1, 1); // Reset tile queue
					buffer.writeBits(7, dx);
					buffer.writeBits(1, player.sync().calculatedFlag() != 0 ? 1 : 0);
					buffer.writeBits(2, player.getTile().level);

					if (player.sync().calculatedFlag() != 0) {
						player.sync().playerUpdateRequests()[player.sync().playerUpdateReqPtr()] = player.index();
						player.sync().playerUpdateReqPtr(player.sync().playerUpdateReqPtr() + 1);
					}
				} else if (primaryStep >= 0) {
					boolean run = secondaryStep >= 0;
					buffer.writeBits(2, run ? 2 : 1); // Step up your game

					buffer.writeBits(3, primaryStep);
					if (run)
						buffer.writeBits(3, secondaryStep);

					buffer.writeBits(1, player.sync().calculatedFlag() != 0 ? 1 : 0);

					if (player.sync().calculatedFlag() != 0) {
						player.sync().playerUpdateRequests()[player.sync().playerUpdateReqPtr()] = player.index();
						player.sync().playerUpdateReqPtr(player.sync().playerUpdateReqPtr() + 1);
					}
				} else {
					buffer.writeBits(2, 0); // No movement
					player.sync().playerUpdateRequests()[player.sync().playerUpdateReqPtr()] = player.index();
					player.sync().playerUpdateReqPtr(player.sync().playerUpdateReqPtr() + 1);
				}
			} else {
				buffer.writeBits(1, 0); // No updates at all
			}
		}

		private void updateOtherPlayers(Player player, RSBuffer buffer) {
			//System.out.println(player.sync().localPlayerPtr());
			buffer.writeBits(8, player.sync().localPlayerPtr()); // Local player count

			int rebuiltptr = 0;
			for (int i=0; i<player.sync().localPlayerPtr(); i++) {
				int index = player.sync().localPlayerIndices()[i];
				Player p = player.world().players().get(index);

				// See if the player either logged out, or is out of our viewport
				if (p == null || player.getTile().distance(p.getTile()) > 14 || player.getTile().level != p.getTile().level) {
					buffer.writeBits(1, 1); // Yes, we need an update
					buffer.writeBits(2, 3); // Type 3: remove
					continue;
				}

				boolean needsUpdate = p.sync().dirty();

				if (needsUpdate) {
					buffer.writeBits(1, 1);

					int primaryStep = p.sync().primaryStep();
					int secondaryStep = p.sync().secondaryStep();

					if (p.sync().teleported()) {
						buffer.writeBits(2, 3); // Teleport (don't add to rebuilt, respawn after adding)
					} else if (primaryStep >= 0) {
						boolean run = secondaryStep >= 0;

						buffer.writeBits(2, run ? 2 : 1); // Step up your game

						buffer.writeBits(3, primaryStep);
						if (run)
							buffer.writeBits(3, secondaryStep);

						buffer.writeBits(1, p.sync().calculatedFlag() != 0 ? 1 : 0);

						rebuilt[rebuiltptr++] = index;
						if (p.sync().calculatedFlag() != 0) {
							player.sync().playerUpdateRequests()[player.sync().playerUpdateReqPtr()] = p.index();
							player.sync().playerUpdateReqPtr(player.sync().playerUpdateReqPtr() + 1);
						}
					} else {
						buffer.writeBits(2, 0); // No movement
						rebuilt[rebuiltptr++] = index;

						player.sync().playerUpdateRequests()[player.sync().playerUpdateReqPtr()] = p.index();
						player.sync().playerUpdateReqPtr(player.sync().playerUpdateReqPtr() + 1);
					}
				} else {
					buffer.writeBits(1, 0); // No updates at all
					rebuilt[rebuiltptr++] = index;
				}
			}

			System.arraycopy(rebuilt, 0, player.sync().localPlayerIndices(), 0, rebuiltptr);
			player.sync().localPlayerPtr(rebuiltptr);
		}

		private void updatePlayerList(Player player, RSBuffer buffer) {
			int[] lp = player.sync().localPlayerIndices();
			final int[] lpp = {player.sync().localPlayerPtr()};

			for (int idx = 0; idx < 2048; idx++) {
				Player p = player.world().players().get(idx);
				if (p == null || player.sync().hasInView(p.index()) || p == player || player.getTile().distance(p.getTile()) > 14 || p.getTile().level != player.getTile().level)
					continue;

				// Limit addition to 25 per cycle, and 255 local.
				if (player.sync().newlyAddedPtr() >= 25 || lpp[0] >= 254) {
					break;
				}

				buffer.writeBits(11, p.index());
				buffer.writeBits(5, p.getTile().z - player.getTile().z);
				buffer.writeBits(5, p.getTile().x - player.getTile().x);
				buffer.writeBits(3, 6); // Direction to face
				buffer.writeBits(1, 1); // Clear tile queue
				buffer.writeBits(1, 1); // Update

				PlayerSyncInfo sync = player.sync();
				sync.playerUpdateRequests()[sync.playerUpdateReqPtr()] = p.index();
				sync.playerUpdateReqPtr(sync.playerUpdateReqPtr() + 1);
				sync.newlyAdded()[sync.newlyAddedPtr()] = p.index();
				sync.newlyAddedPtr(sync.newlyAddedPtr() + 1);

				lp[lpp[0]++] = p.index();
			}

			if (player.sync().playerUpdateReqPtr() > 0)
				buffer.writeBits(11, -1); // No more adding

			player.sync().localPlayerPtr(lpp[0]);
		}

	}

	@Override
	public void execute(World world) {

	}

	@Override
	public Collection<SubTask> createJobs(World world) {
		int numjobs = world.players().size() / 25 + 1;
		ArrayList<SubTask> tasks = new ArrayList<>(numjobs);
		List<Player> work = new ArrayList<>(5);

		// Create jobs which will cover 5 players per job
		world.players().forEach(p -> {
			work.add(p);

			if (work.size() == 100) {
				tasks.add(new Job(world, work.toArray(new Player[work.size()])));
				work.clear();
			}
		});

		// Remainders?
		if (!work.isEmpty()) {
			tasks.add(new Job(world, work.toArray(new Player[work.size()])));
		}

		return tasks;
	}

	@Override
	public boolean isAsyncSafe() {
		return true;
	}

}
