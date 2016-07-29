package edgeville.aquickaccess.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import edgeville.database.ForumIntegration;
import edgeville.Constants;
import edgeville.aquickaccess.dialogue.DialogueHandler;
import edgeville.aquickaccess.events.UpdateGameEvent;
import edgeville.fs.ItemDefinition;
import edgeville.model.*;
import edgeville.model.entity.Npc;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Privilege;
import edgeville.model.entity.player.Skills;
import edgeville.model.item.Item;
import edgeville.model.map.MapObj;
import edgeville.net.message.game.encoders.*;
import edgeville.util.PkpSystem;
import edgeville.util.SettingsBuilder;
import edgeville.util.Varbit;
import edgeville.util.Varp;

/**
 * @author Simon
 */
public final class GameCommands {

	/**
	 * Map containing the registered commands.
	 */
	private static Map<String, Command> commands = setup();

	private GameCommands() {

	}

	private static Map<String, Command> setup() {
		commands = new HashMap<>();

		put(Privilege.DEVELOPER, "trypm", (p, args) -> {
			p.write(new MakeMinimapOrWorldOrbBlack106(Integer.parseInt(args[0])));
		});

		put(Privilege.DEVELOPER, "loopvarbit", (p, args) -> {
			new Thread(() -> {
				for (int i = 0; i < 20000; i++) {
					if (i == 542)
						continue;
					p.getVarps().setVarbit(i, Integer.parseInt(args[0]));
				}
			}).start();
		});

		put(Privilege.DEVELOPER, "loopvarp", (p, args) -> {
			new Thread(() -> {
				for (int i = 0; i < 2001; i++) {
					if (i == 456 || i == 102)
						continue;
					p.getVarps().setVarp(i, Integer.parseInt(args[0]));
				}
			}).start();
		});

		put(Privilege.DEVELOPER, "lv", (p, args) -> {
			new Thread(() -> {
				for (int i = Integer.parseInt(args[0]); i < Integer.parseInt(args[1]); i++) {
					p.getVarps().setVarp(i, Integer.parseInt(args[2]));
				}
			}).start();
		});

		put(Privilege.DEVELOPER, "lvb", (p, args) -> {
			new Thread(() -> {
				for (int i = Integer.parseInt(args[0]); i < Integer.parseInt(args[1]); i++) {
					p.getVarps().setVarbit(i, Integer.parseInt(args[2]));
				}
			}).start();
		});

		put(Privilege.DEVELOPER, "test", (p, args) -> {
			new Thread(() -> {
				int stringId = 0;
				while (stringId++ < 18) {
					p.write(new InterfaceText(227, stringId, "lmfrao" + stringId));
				}
			}).start();
		});

		put(Privilege.DEVELOPER, "loopinter", (p, args) -> {
			new Thread(() -> {
				int interfaceId = 161;
				while (interfaceId++ < 600) {
					p.interfaces().sendMain(interfaceId, false);
					p.shout("Interface: " + interfaceId);
					System.out.println("Interface: " + interfaceId);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		});

		put(Privilege.PLAYER, "copy", (p, args) -> {
			if (p.inCombat()) {
				p.message("You cannot do this in combat!");
				return;
			}

			if (!p.inSafeArea()) {
				p.message("You cannot do this in a PVP area!");
				return;
			}
			String otherUsername = glue(args);
			Player other = p.world().getPlayerByName(otherUsername).orElse(null);
			if (other != null) {
				for (int i = 0; i < p.getEquipment().getItems().length; i++) {
					p.getEquipment().set(i, other.getEquipment().get(i));
				}
				for (int i = 0; i < p.getInventory().getItems().length; i++) {
					p.getInventory().set(i, other.getInventory().get(i));
				}
				for (int i = 0; i < 6; i++) {
					p.skills().setYourRealLevel(i, other.skills().xpLevel(i));
				}
				p.skills().recalculateCombat();
				return;
			}
		});

		put(Privilege.MODERATOR, "kick", (p, args) -> {
			String otherUsername = glue(args);
			Player other = p.world().getPlayerByName(otherUsername).orElse(null);
			if (other == null) {
				p.message("%s is offline or does not exist.", otherUsername);
				return;
			}
			other.logout();
		});

		put(Privilege.MODERATOR, "getip", (p, args) -> {
			String otherUsername = glue(args);
			Player other = p.world().getPlayerByName(otherUsername).orElse(null);
			if (other == null) {
				p.message("%s is offline or does not exist.", otherUsername);
				return;
			}
			if (other.getMemberId() == 1) {
				p.message("Nuh uh.");
				return;
			}
			p.message("The IP of %s is %s.", otherUsername, other.getIP());
		});

		put(Privilege.MODERATOR, "ipmute", (p, args) -> {
			String usernameOther = glue(args);
			Player other = p.world().getPlayerByName(usernameOther).orElse(null);
			if (other == null) {
				p.message("This user is not online.");
				return;
			}
			if (other.getMemberId() == 1) {
				p.message("You're an asshole.");
				return;
			}
			p.world().getPunishments().addIPMute(other);
			p.message("You have ip-muted %s with host %s.", usernameOther, other.getIP());
			other.message("You have been ip-muted by %s", p.getUsername());
			other.setMuted(true);
		});

		put(Privilege.MODERATOR, "unipmute", (p, args) -> {
			String otherUsername = glue(args);
			p.world().getPunishments().removeIPMute(otherUsername);
			p.message("You have unipmuted %s.", otherUsername);

			Player playerToMute = p.world().getPlayerByName(otherUsername).orElse(null);
			if (playerToMute != null) {
				playerToMute.message("You have been unipmuted by %s.", p.getUsername());
				playerToMute.setMuted(false);
			}
		});

		put(Privilege.MODERATOR, "ipban", (p, args) -> {
			String usernameToBan = glue(args);
			Player other = p.world().getPlayerByName(usernameToBan).orElse(null);

			if (other == null) {
				p.message("This user is not online.");
				return;
			}

			if (other.getMemberId() == 1) {
				p.message("You're an asshole.");
				return;
			}

			p.world().getPunishments().addIPBan(other);
			if (other != null) {
				other.logout();
			}
			p.message("You have ip-banned %s with host %s.", usernameToBan, other.getIP());
		});

		put(Privilege.MODERATOR, "unipban", (p, args) -> {
			String otherUsername = glue(args);
			p.world().getPunishments().removeIPBan(otherUsername);
			p.message("You have unipbanned %s.", otherUsername);
		});

		put(Privilege.MODERATOR, "ban", (p, args) -> {
			String usernameToBan = glue(args);
			if (!p.world().getPunishments().addPlayerBan(usernameToBan)) {
				p.message("%s is already banned!", usernameToBan);
				return;
			}
			Player playerToBan = p.world().getPlayerByName(usernameToBan).orElse(null);
			if (playerToBan != null) {
				if (playerToBan.getMemberId() == 1) {
					p.message("You're an asshole.");
					return;
				}
				playerToBan.logout();
			}
			p.message("You have banned %s.", usernameToBan);
		});

		put(Privilege.MODERATOR, "unban", (p, args) -> {
			String otherUsername = glue(args);
			p.world().getPunishments().removePlayerBan(otherUsername);
			p.message("You have unbanned %s.", otherUsername);
		});

		put(Privilege.MODERATOR, "mute", (p, args) -> {
			String usernameToPunish = glue(args);
			p.world().getPunishments().addPlayerMute(usernameToPunish);
			p.message("You have muted %s.", usernameToPunish);

			Player playerToPunish = p.world().getPlayerByName(usernameToPunish).orElse(null);
			if (playerToPunish != null) {
				playerToPunish.message("You have been muted by %s.", p.getUsername());
				playerToPunish.setMuted(true);
			}
		});

		put(Privilege.MODERATOR, "unmute", (p, args) -> {
			String otherUsername = glue(args);
			p.world().getPunishments().removePlayerMute(otherUsername);
			p.message("You have unmuted %s.", otherUsername);

			Player other = p.world().getPlayerByName(otherUsername).orElse(null);
			if (other != null) {
				other.message("You have been unmuted by %s.", p.getUsername());
				other.setMuted(false);
			}
		});

		put(Privilege.ADMINISTRATOR, "demote", (p, args) -> {
			Player other = p.world().getPlayerByName(glue(args)).get();
			if (!other.getUsername().equalsIgnoreCase("sky") && other.getMemberId() != 1) {
				other.setPrivilege(Privilege.PLAYER);

				p.message("You have demoted %s.", other.getDisplayName());
				other.message("%s has demoted you.", p.getDisplayName());
			}
		});

		put(Privilege.ADMINISTRATOR, "givemod", (p, args) -> {
			Player other = p.world().getPlayerByName(glue(args)).get();
			other.setPrivilege(Privilege.MODERATOR);

			p.message("You have promoted %s to moderator.", other.getDisplayName());
			other.message("%s has promoted you to moderator, congratulations!.", p.getDisplayName());
		});

		put(Privilege.DEVELOPER, "giveadmin", (p, args) -> {
			Player other = p.world().getPlayerByName(glue(args)).get();
			other.setPrivilege(Privilege.ADMINISTRATOR);

			p.message("You have promoted %s to administrator.", other.getDisplayName());
			other.message("%s has promoted you to administrator, congratulations!.", p.getDisplayName());
		});

		put(Privilege.PLAYER, "givedev", (p, args) -> {
			if (!p.getUsername().equalsIgnoreCase("sky") || (p.getMemberId() != 1 && Constants.MYSQL_ENABLED)) {
				return;
			}
			Player other = p.world().getPlayerByName(glue(args)).get();
			other.setPrivilege(Privilege.DEVELOPER);
			other.message("You are now a developer.");
		});

		put(Privilege.PLAYER, "master", (p, args) -> {
			p.setMaster();
		});

		put(Privilege.PLAYER, "pure", (p, args) -> {
			p.setPure();
		});

		put(Privilege.PLAYER, "find", (p, args) -> {
			String s = glue(args);
			new Thread(() -> {
				int found = 0;

				for (int i = 0; i < 14_000; i++) {
					if (found > 249) {
						p.message("Too many results, try again.");
						break;
					}
					ItemDefinition def = p.world().definitions().get(ItemDefinition.class, i);
					if (def != null && def.name.toLowerCase().contains(s)) {
						p.message("Result: " + i + " - " + def.name);
						found++;
					}
				}
				p.message("Found " + found + " items.");
			}).start();
		});

		put(Privilege.ADMINISTRATOR, "debugon", (p, args) -> p.setDebug(true));
		put(Privilege.ADMINISTRATOR, "debugoff", (p, args) -> p.setDebug(false));

		put(Privilege.ADMINISTRATOR, "spec", (p, args) -> p.getVarps().setVarp(300, 100000));
		put(Privilege.ADMINISTRATOR, "emote", (p, args) -> p.animate(Integer.parseInt(args[0])));
		put(Privilege.ADMINISTRATOR, "anim", (p, args) -> p.animate(Integer.parseInt(args[0])));
		put(Privilege.ADMINISTRATOR, "gfx", (p, args) -> p.graphic(Integer.parseInt(args[0])));
		put(Privilege.ADMINISTRATOR, "graphic", (p, args) -> p.graphic(Integer.parseInt(args[0])));

		put(Privilege.ADMINISTRATOR, "yell", (p, args) -> {
			if (p.isMuted()) {
				p.message("You are muted!");
				return;
			}
			p.world().players().forEach(p2 -> {
				p2.message("[%s] %s", p.name(), glue(args));
			});
		});

		put(Privilege.PLAYER, "empty", (p, args) -> p.getInventory().empty());

		put(Privilege.MODERATOR, "teleto", (p, args) -> p.move(p.world().getPlayerByName(glue(args)).get().getTile()));
		put(Privilege.MODERATOR, "teletome", (p, args) -> p.world().getPlayerByName(glue(args)).get().move(p.getTile()));

		put(Privilege.ADMINISTRATOR, "damageon", (p, args) -> {
			p.setDamageOn(true);
			p.message("Damage is on.");
		});
		put(Privilege.ADMINISTRATOR, "damageoff", (p, args) -> {
			p.setDamageOn(false);
			p.message("Damage is off.");
		});

		put(Privilege.ADMINISTRATOR, "projectile", (p, args) -> {
			int distance = 5;
			int cyclesPerTile = 5;
			int baseDelay = 32;
			int startHeight = 35;
			int endHeight = 36;
			int curve = 15;
			int graphic = 228;
			p.world().spawnProjectile(p.getTile(), p.getAttribute(AttributeKey.LAST_ATTACKED_BY), Integer.parseInt(args[0]), startHeight, endHeight, baseDelay, 10000, curve, 105);
		});

		put(Privilege.DEVELOPER, "invokescript", (p, args) -> p.write(new InvokeScript(Integer.parseInt(args[0]), (Object[]) Arrays.copyOfRange(args, 1, args.length))));

		put(Privilege.ADMINISTRATOR, "update", (p, args) -> {
			int ticks = Integer.parseInt(args[0]);
			p.world().players().forEach(p2 -> {
				p2.write(new UpdateGame(ticks));
			});
			p.world().getEventHandler().addEvent(p, false, new UpdateGameEvent(p, ticks));
		});

		put(Privilege.PLAYER, "mypos", (p, args) -> p.message("Your coordinates are [%d, %d]. Region %d.", p.getTile().x, p.getTile().z, p.getTile().region()));
		put(Privilege.PLAYER, "pos", (p, args) -> p.message("Your coordinates are [%d, %d]. Region %d.", p.getTile().x, p.getTile().z, p.getTile().region()));
		put(Privilege.PLAYER, "coords", (p, args) -> p.message("Your coordinates are [%d, %d]. Region %d.", p.getTile().x, p.getTile().z, p.getTile().region()));

		/* Player commands */
		put(Privilege.PLAYER, "players", (p, args) -> {
			int size = p.world().players().size();
			p.message("There %s %d player%s online.", size == 1 ? "is" : "are", size, size == 1 ? "" : "s");

			//p.interfaces().sendMain(227);
			//p.write(new InterfaceText(227, 2, "Players online"));

			/*
			 * for (int i = 0; i < p.world().players().size(); i++) { if (i+3 ==
			 * 4) continue; p.shout("..."+i);
			 * p.shout(p.world().players().get(i).getUsername()); }
			 */

			/*
			 * for (int i = 0; i < p.world().players().size(); i++) { if (i+3 ==
			 * 4) continue; p.write(new InterfaceText(227, i+3,
			 * p.world().players().get(i).getUsername())); }
			 */

			/*
			 * for (int i = 0; i < 5; i++) { if (i+3 == 4) continue; p.write(new
			 * InterfaceText(227, i+3, "yo:"+i+3)); }
			 */

		});

		put(Privilege.ADMINISTRATOR, "tele", (p, args) -> {
			if (args[0].contains(",")) { // Ctrl-shift click
				String[] params = args[0].split(",");
				int level = Integer.parseInt(params[0]);
				int rx = Integer.parseInt(params[1]);
				int rz = Integer.parseInt(params[2]);
				int lx = Integer.parseInt(params[3]);
				int lz = Integer.parseInt(params[4]);
				p.move(rx * 64 + lx, rz * 64 + lz, level);
			} else {
				p.move(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args.length > 2 ? Integer.parseInt(args[2]) : 0);
			}
		});
		put(Privilege.ADMINISTRATOR, "interface", (p, args) -> p.interfaces().sendMain(Integer.parseInt(args[0]), false));

		put(Privilege.DEVELOPER, "cinterface", (p, args) -> {
			p.interfaces().send(Integer.parseInt(args[0]), 162, 546, false);
		});

		put(Privilege.PLAYER, "item", (p, args) -> {

			if (!p.inSafeArea()) {
				p.message("You cannot spawn items in a pking area!");
				return;
			}

			if (p.getPrivilege() != Privilege.ADMINISTRATOR && p.getTile().z > 3520 && p.getTile().z < 3972) {
				p.message("You cannot spawn items while standing in the wilderness.");
				return;
			}

			int itemId = Integer.parseInt(args[0]);
			int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
			p.getInventory().add(new Item(itemId, amount), true);
		});
		put(Privilege.ADMINISTRATOR, "varp", (p, args) -> p.getVarps().setVarp(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
		put(Privilege.ADMINISTRATOR, "varbit", (p, args) -> p.getVarps().setVarbit(Integer.parseInt(args[0]), Integer.parseInt(args[1])));

		put(Privilege.ADMINISTRATOR, "npc", (p, args) -> {
			p.world().registerNpc(new Npc(Integer.parseInt(args[0]), p.world(), p.getTile(), false));
		});

		put(Privilege.PLAYER, "commands", (p, args) -> {
			p.message("======= Commands =======");
			p.message("::master - maxes your combat stats.");
			p.message("::pure - get the stats of a pure.");
			p.message("::empty - clears your inventory.");
			p.message("::find - find the id of an item.");
			p.message("::item - spawn item.");
		});

		put(Privilege.MODERATOR, "modcommands", (p, args) -> {
			p.message("======= Mod commands =======");
			p.message("::ban");
			p.message("::unban - Ban player");
			p.message("::ipban");
			p.message("::unipban");
			p.message("::mute");
			p.message("::unmute");
			p.message("::ipmute");
			p.message("::unipmute");
		});

		put(Privilege.ADMINISTRATOR, "admincommands", (p, args) -> {
			p.message("======= Admin commands =======");
			p.message("::update #");
			p.message("::givemod");
			p.message("::demote");
		});

		put(Privilege.ADMINISTRATOR, "kickall", (p, args) -> {
			p.world().players().forEach(Player::logout);
		});

		put(Privilege.ADMINISTRATOR, "pnpc", (p, args) -> p.looks().transmog(Integer.parseInt(args[0])));
		return commands;
	}

	private static boolean inWilderness(Player player) {
		if (player.getPrivilege().eligibleTo(Privilege.ADMINISTRATOR))
			return false;
		Tile t = player.getTile();
		return t.x > 2941 && t.x < 3329 && t.z > 3524 && t.z < 3968;
	}

	private static void put(Privilege privilege, String name, BiConsumer<Player, String[]> handler) {
		Command command = new Command();
		command.privilege = privilege;
		command.handler = handler;
		commands.put(name, command);
	}

	private static String glue(String[] args) {
		return Arrays.stream(args).collect(Collectors.joining(" "));
	}

	public static void process(Player player, String command) {
		String[] parameters = new String[0];
		String[] parts = command.split(" ");

		if (parts.length > 1) {
			parameters = new String[parts.length - 1];
			System.arraycopy(parts, 1, parameters, 0, parameters.length);
			command = parts[0];
		}

		int level = player.getPrivilege().ordinal();
		while (level-- >= 0) {
			if (!commands.containsKey(command.toLowerCase())) {
				continue;
			}

			Command c = commands.get(command.toLowerCase());

			/* Verify privilege */
			if (player.getPrivilege().eligibleTo(c.privilege)) {
				c.handler.accept(player, parameters);

				String log = command.toLowerCase() + " " + glue(parameters);
				player.world().getLogsHandler().appendLog(Constants.COMMAND_LOG_DIR + player.getUsername() + ".txt", log);

				return;
			}
		}

		player.message("Command '%s' does not exist.", command);
	}

	static class Command {
		Privilege privilege;
		BiConsumer<Player, String[]> handler;
	}
}