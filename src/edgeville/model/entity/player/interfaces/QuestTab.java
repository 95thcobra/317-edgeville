package edgeville.model.entity.player.interfaces;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import edgeville.Constants;
import edgeville.combat.Potions;
import edgeville.database.ForumIntegration;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.Loadout;
import edgeville.model.item.Item;
import edgeville.net.message.game.encoders.InterfaceText;
import edgeville.net.message.game.encoders.OpenForums;
import edgeville.net.message.game.encoders.OpenHiscores;
import edgeville.util.CombatFormula;
import edgeville.util.TextUtil;
import edgeville.util.Varbit;

public class QuestTab {

	private Player player;

	public QuestTab(Player player) {
		this.player = player;
	}

	public void updatePlayersOnline() {
		player.interfaces().sendInterfaceString(274, 10,
				"Players online: <col=00ff00>" + player.world().getPlayersOnline());
	}

	public void updateServerUptime() {
		player.interfaces().sendInterfaceString(274, 14,
				"<col=EB981F>Uptime: " + player.world().getUptime().toString());
	}

	public void updateTimePlayed() {
		player.interfaces().sendInterfaceString(274, 20, "<col=ffffff>Time played: " + player.getPlayTime().toString());
	}

	public void updateKills() {
		player.interfaces().sendInterfaceString(274, 21, "<col=ffffff>Kills: <col=00ff00>" + player.getKills());
	}

	public void updateDeaths() {
		player.interfaces().sendInterfaceString(274, 22, "<col=ffffff>Deaths: <col=00ff00>" + player.getDeaths());
	}

	public void updateMaxHit(int maxHit) {
		player.interfaces().sendInterfaceString(274, 23, "<col=ffffff>Max hit: <col=00ff00>" + maxHit);
	}

	public void prepareQuestTab() {
		final int questTabInterfaceId = 274;

		updatePlayersOnline();
		updateServerUptime();
		// big
		// string

		player.interfaces().sendInterfaceString(questTabInterfaceId, 15, "<col=ffffff>Save loadout");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 16, "<col=ffffff>Load loadout");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 17, "<col=ffffff>View forum");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 18, "<col=ffffff>View hiscores");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 19, "<col=ffffff>Update hiscores");

		updateTimePlayed();// 20
		updateKills();// 21
		updateDeaths();// 22
		updateMaxHit(-1);// 23

		// BLACK("000000"), BLUE("0066ff"), RED("FF0000");

		// Small strings start. COLORS <col=00AEDB>
		player.interfaces().sendInterfaceString(questTabInterfaceId, 24, "");

		player.interfaces().sendInterfaceString(questTabInterfaceId, 25, "<col=ffffff>Food");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 26, "<col=ffffff>Potions");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 27, "<col=ffffff>Vengeance");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 28, "<col=ffffff>Melee gear");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 29, "<col=ffffff>Range gear");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 30, "<col=ffffff>Hybrid gear");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 31, "<col=ffffff>Pure gear");
		player.interfaces().sendInterfaceString(questTabInterfaceId, 32, "<col=ffffff>Dharok's gear");

		for (int child = 33; child < 143; child++) {
			player.write(new InterfaceText(questTabInterfaceId, child, ""));
		}

		// makeLinesYellow();
	}

	// NOT USED SINCE I COLOR THEM MANUALLY
	private void makeLinesYellow() {
		int[] varps = { 29, 31, 62, 71, 107, 122, 130, 144, 222, 273, 176, 32 };
		for (int varp : varps) {
			player.getVarps().setVarp(varp, 1);
		}
	}

	public void clickButton(int buttonId) {
		switch (buttonId) {

		// Save loadout
		case 15:
			player.getLoadout().save(player);
			player.message("Saved loadout.");
			break;

		// Load loadout
		case 16:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}

			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}
			
			player.getPrayer().deactivateAllPrayers();
			player.getLoadout().load(player);
			player.message("Loaded loadout.");
			break;

		// view forum
		case 17:
			player.write(new OpenForums());
			break;

		// view hiscores
		case 18:
			player.write(new OpenHiscores());
			break;

		// update hiscores
		case 19:
			if (!Constants.MYSQL_ENABLED) {
				return;
			}
			if (!ForumIntegration.updateHiscores(player)) {
				int minutesLeft = (int) (10 - ((System.currentTimeMillis() - player.getLastHiscoresUpdate()) / 60000));
				player.message("You can update the hiscores in %d minutes!", minutesLeft);
				return;
			}
			player.message("You have updated the hiscores!");
			break;

		// Food
		case 25:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			player.getInventory().add(391, 28);
			player.message("You have spawned some food.");
			break;

		// Potions
		case 26:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			player.getInventory().add(6685, 1); // sara brew
			player.getInventory().add(3024, 2); // super restore
			player.getInventory().add(12695, 1); // super combat
			player.message("You have spawned a set of potions.");
			break;

		// Barrage
		/*
		 * case 27: if (player.inCombat()) { player.message(
		 * "You cannot do this in combat!"); return; } if (!player.inSafeArea())
		 * { player.message("You cannot do this in a PVP area!"); return; }
		 * 
		 * player.getInventory().add(new Item(555, 1000)); player.
		 * getInventory().add(new Item(560, 1000)); player.
		 * getInventory().add(new Item(565, 1000)); player.message(
		 * "You have spawned barrage runes."); break;
		 */

		// Veng
		case 27:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			player.getInventory().add(557, 1000); // earth
			player.getInventory().add(9075, 1000); // astral
			player.getInventory().add(560, 1000); // death
			player.message("You have spawned veng runes.");
			break;

		// Melee
		case 28:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			player.getPrayer().deactivateAllPrayers();
			player.spawnMelee();
			player.message("You have spawned some melee gear.");
			break;

		// Range
		case 29:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			player.getPrayer().deactivateAllPrayers();
			player.spawnRanged();
			player.message("You have spawned some ranged gear.");
			break;

		// Hybrid
		case 30:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			player.getPrayer().deactivateAllPrayers();
			player.spawnHybrid();
			player.message("You have spawned some hybrid gear.");
			break;

		// Pure
		case 31:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			/*
			 * if (!player.getEquipment().isEmpty()) { player.message(
			 * "Unequip your equipment before spawning!"); return; }
			 */

			player.getPrayer().deactivateAllPrayers();
			player.spawnPure();
			player.message("You have spawned some pure gear.");
			break;

		// dharoks
		case 32:
			if (player.inCombat()) {
				player.message("You cannot do this in combat!");
				return;
			}
			if (!player.inSafeArea()) {
				player.message("You cannot do this in a PVP area!");
				return;
			}

			player.getPrayer().deactivateAllPrayers();
			player.spawnDharoks();
			player.message("You have spawned dharok's gear.");
			break;
		}
	}
}
