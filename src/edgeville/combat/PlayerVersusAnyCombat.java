package edgeville.combat;

import edgeville.Constants;
import edgeville.combat.ranged.BoltEffects;
import edgeville.event.Event;
import edgeville.event.EventContainer;
import edgeville.model.AttributeKey;
import edgeville.model.ChatMessage;
import edgeville.model.Entity;
import edgeville.model.Tile;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.Skulls;
import edgeville.model.entity.player.WeaponType;
import edgeville.model.item.Item;
import edgeville.script.TimerKey;
import edgeville.util.AccuracyFormula;
import edgeville.util.CombatFormula;
import edgeville.util.CombatStyle;
import edgeville.util.EquipmentInfo;
import edgeville.util.Varbit;
import edgeville.util.Varp;

public class PlayerVersusAnyCombat extends Combat {

	private Player player;
	private Entity target;

	public PlayerVersusAnyCombat(Entity entity, Entity target) {
		super(entity, target);
		player = (Player) entity;
		this.target = target;
	}

	@Override
	public void cycle(EventContainer container) {
		// Get weapon data.
		Item weapon = ((Player) getEntity()).getEquipment().get(EquipSlot.WEAPON);
		int weaponId = weapon == null ? -1 : weapon.getId();
		int weaponType = getEntity().world().equipmentInfo().weaponType(weaponId);
		Item ammo = ((Player) getEntity()).getEquipment().get(EquipSlot.AMMO);
		int ammoId = ammo == null ? -1 : ammo.getId();
		String ammoName = ammo == null ? "" : ammo.definition(getEntity().world()).name;

		// Check if players are in wilderness.
		if (target instanceof Player) {
			if (!CombatUtil.canAttack(player, target)) {
				container.stop();
				return;
			}
			// bounty interface for player.
			/*
			 * if (!player.timers().has(TimerKey.IN_COMBAT)) {
			 * player.interfaces().setBountyInterface(true); }
			 */
			player.timers().register(TimerKey.IN_COMBAT, 5);
			if (!player.inSafeArea()) {
				player.timers().register(TimerKey.AFTER_COMBAT, 5);
			}
			// bounty interface for target.
			/*
			 * if (!target.timers().has(TimerKey.IN_COMBAT)) { ((Player)
			 * target).interfaces().setBountyInterface(true); }
			 */
			target.timers().register(TimerKey.IN_COMBAT, 5);
			if (!((Player) target).inSafeArea()) {
				target.timers().register(TimerKey.AFTER_COMBAT, 5);
			}

			/*
			 * if (((Player) target).isAutoRetaliateEnabled()) {
			 * target.face(player); new PvPCombat((Player)target,
			 * player).start(); }
			 */

			// auto retaliate
			/*
			 * if (target instanceof Player) { Player targetP = (Player) target;
			 * if (targetP.isAutoRetaliateEnabled() &&
			 * !targetP.currentlyAttacking) { targetP.face(player); new
			 * PvPCombat(targetP, player).start(); } }
			 */
			///
		}

		// Combat type?
		if (weaponType == WeaponType.BOW || weaponType == WeaponType.CROSSBOW || weaponType == WeaponType.THROWN || weaponType == WeaponType.CHINCHOMPA) {
			handleRangeCombat(weaponId, ammoName, weaponType, container);
		} else {
			handleMeleeCombat(weaponId);
		}

		target.setLastDamagedMillis(System.currentTimeMillis());
	}

	@Override
	public void handleMeleeCombat(int weaponId) {
		Tile currentTile = getEntity().getTile();

		// dont attack if stunned
		if (player.stunned()) {
			return;
		}

		// Move closer if out of range.
		if (!player.touches(getTarget(), currentTile)) {
			if (!player.frozen()) {
				// player.pathQueue().clear();
				// currentTile = moveCloser();
				// player.stepTowards(target, 2);

				currentTile = moveCloser();
				// player.stepTowardsPlayerNotBugged(target, target.getTile(),
				// 2);
			} else {
				return;
			}
			// return;
		}

		if (player.getTile().distance(target.getTile()) > 3) {
			return;
		}

		// Check timer.
		if (!getEntity().timers().has(TimerKey.COMBAT_ATTACK)) {

			if (player.isSpecialAttackEnabled()) {
				doMeleeSpecial(player, weaponId);
				return;
			}

			int max = CombatFormula.maximumMeleeHit(((Player) getEntity()));
			int hit = 0;

			if (target instanceof Player) {
				// hit = AccuracyFormula.calcHitNEWMelee(player, (Player)
				// target, max);
				hit = AccuracyFormula.calculateHit(player, (Player) target, max);
			} else {
				boolean success = AccuracyFormula.doesHit(((Player) getEntity()), getTarget(), CombatStyle.MELEE);
				if (success) {
					hit = getEntity().world().random(max);
				}
				// hit = AccuracyFormula.calculateHit(player, (Player)target,
				// max);
			}

			// int hit = (target instanceof Player ?
			// AccuracyFormula.calculateHit(player, (Player)target, max) :
			// getEntity().world().random(max));
			player.getQuestTab().updateMaxHit(max);
			// int hit = getEntity().world().random(max);

			// getTarget().hit(getEntity(), success ? hit : 0,
			// CombatStyle.MELEE);
			// triggerVeng(success ? hit : 0);

			getTarget().hit(getEntity(), hit, CombatStyle.MELEE);
			if (hit > 0) {
				triggerVeng(hit);
			}

			//guthan effect
			if (CombatFormula.fullGuthan(player) && player.world().random(4) == 0) {
				handleGuthan(player, target, hit);
			}

			getEntity().animate(EquipmentInfo.attackAnimationFor(((Player) getEntity())));
			getEntity().timers().register(TimerKey.COMBAT_ATTACK, getEntity().world().equipmentInfo().weaponSpeed(weaponId));
		}
	}

	private void handleGuthan(Player attacker, Entity attacked, int damage) {
		attacker.heal(damage);
		attacked.graphic(398);
	}

	private void triggerVeng(int hit) {
		if (hit <= 0)
			return;
		if (target instanceof Player) {
			if (((Player) target).isVengOn()) {
				player.hit(target, (int) (0.75 * hit));
				((Player) target).shout("Taste Vengeance!");
				((Player) target).setVengOn(false);
			}
		}
	}

	public void doMeleeSpecial(Player player, int weaponId) {
		player.timers().register(TimerKey.COMBAT_ATTACK, 5);
		SpecialAttacks specialAttack = SpecialAttacks.getSpecialAttack(player, weaponId);
		if (specialAttack == null) {
			return;
		}
		if (!drainSpecialEnergy(player, specialAttack.getSpecialDrain())) {
			return;
		}
		player.animate(specialAttack.getAnimationId());
		if (specialAttack.getGfx() != null) {
			player.graphic(specialAttack.getGfx());
		}
		if (specialAttack.getOpponentGfx() != null) {
			target.graphic(specialAttack.getOpponentGfx());
		}

		double max = CombatFormula.maximumMeleeHit(player) * specialAttack.getMaxHitMultiplier();
		player.getQuestTab().updateMaxHit((int) Math.round(max));
		// int hit = player.world().random().nextInt((int) Math.round(max));
		int hit;

		/*
		 * if (target instanceof Player) { hit =
		 * AccuracyFormula.calculateHit(player, (Player) target, (int)
		 * Math.round(max)); } else { hit =
		 * player.world().random().nextInt((int) Math.round(max)); }
		 */

		hit = AccuracyFormula.calculateHit(player, target, (int) Math.round(max), specialAttack);

		if (specialAttack.isHits()) {
			// double max = CombatFormula.maximumMeleeHit(player) *
			// specialAttack.getMaxHitMultiplier();
			// int hit = player.world().random().nextInt((int) Math.round(max));

			target.hit(player, hit, CombatStyle.MELEE);
			triggerVeng(hit);

			if (specialAttack.isDoubleHit()) {
				// int hit2 = player.world().random().nextInt((int)
				// Math.round(max));
				int hit2;
				if (target instanceof Player) {
					hit2 = AccuracyFormula.calculateHit(player, (Player) target, (int) Math.round(max), specialAttack);
				} else {
					hit2 = player.world().random().nextInt((int) Math.round(max));
				}
				target.hit(player, hit2, CombatStyle.MELEE);
			}
		}

		// Do extra action
		specialAttack.action(player, target, hit);
	}

	@Override
	public void handleRangeCombat(int weaponId, String ammoName, int weaponType, EventContainer container) {
		Tile currentTile = player.getTile();
		//player.messageDebug("ranging...");

		int maxDist = 7;
		if (weaponType == WeaponType.CHINCHOMPA) {
			maxDist = 4;
		}

		// Are we in range?
		if (currentTile.distance(target.getTile()) > maxDist) {
			if (!player.frozen() && !player.stunned()) {
				// currentTile = moveCloser();
				player.stepTowards(target, 25);
			}
			return;
		}

		// if (player.pathQueue().projectilesBlocked(player, target)) {
		// player.stepTowards(target, 2);
		// return;
		// }

		if (player.getTile().equals(target.getTile())) {
			if (!player.frozen()) {
				currentTile = moveCloser();
			}
			return;
		}
		// player.pathQueue().clear();

		// Can we shoot?
		if (player.timers().has(TimerKey.COMBAT_ATTACK)) {
			return;
		}
		
		final int BLOWPIPE_ID = 12926;
		final int CRYSTAL_BOW_ID = 4212;
		if (weaponId == BLOWPIPE_ID && player.getBlowpipeAmmo() == null) {
			player.message("You need to load the blowpipe with darts!");
			container.stop();
			return;
		}

		// Check if player tries to use dragon arrows with any other bow than dark bow.
		final int DARK_BOW = 11235;
		if (weaponType == WeaponType.BOW && weaponId != DARK_BOW && ammoName.contains("Dragon arrow") && weaponId != CRYSTAL_BOW_ID) {
			player.message("You can only use dragon arrows with dark bow.");
			container.stop();
			return;
		}

		// Do we have ammo?
		if (weaponType != WeaponType.THROWN && weaponType != WeaponType.CHINCHOMPA && ammoName.equals("") && weaponId != 4212) {
			player.message("There's no ammo left in your quiver.");
			container.stop();
			return;
		}

		// Check if ammo is of right type
		if (weaponType == WeaponType.CROSSBOW && !ammoName.contains(" bolts") && !ammoName.contains("Bolt rack") && weaponId != 4212) {
			player.message("You can't use that ammo with your crossbow.");
			container.stop();
			return;
		}
		if (weaponType == WeaponType.BOW && !ammoName.contains(" arrow") && weaponId != 4212) {
			player.message("You can't use that ammo with your bow.");
			container.stop();
			return;
		}

		// Remove the ammo
		Projectile projectile = null;

		Item ammo = player.getEquipment().get(EquipSlot.AMMO);

		// crystal bow doesnt use ammo
		if (weaponId != CRYSTAL_BOW_ID && weaponId != BLOWPIPE_ID) {

			if (weaponType != WeaponType.THROWN && weaponType != WeaponType.CHINCHOMPA) {
				//Item ammo = player.getEquipment().get(EquipSlot.AMMO);
				projectile = Projectile.getProjectileForAmmoName(ammo.definition(player.world()).name);
				player.getEquipment().set(EquipSlot.AMMO, new Item(ammo.getId(), ammo.getAmount() - 1));

				// If it is thrown
			} else if (weaponType == WeaponType.THROWN || weaponType == WeaponType.CHINCHOMPA) {
				Item item = player.getEquipment().get(EquipSlot.WEAPON);
				if (item != null) {
					// if
					// (item.definition(player.world()).name.contains("knife")
					// || item.definition(player.world()).name.contains("dart"))
					// {
					projectile = Projectile.getProjectileForAmmoName(item.definition(player.world()).name);
					player.getEquipment().set(EquipSlot.WEAPON, new Item(item.getId(), item.getAmount() - 1));
					// player.graphic(new Graphic(225, 92, 0));
					// }
				}
			}

		}

		if (player.isSpecialAttackEnabled()) {
			doRangeSpecial(player, weaponId);
			return;
		}

		if (projectile != null && projectile.getGfx() != null) {
			// if dark bow then another spawnprojec
			if (weaponId == 11235) {
				player.graphic(projectile.getDarkBowGfx());
				// crystal bow, other gfx
			} else {
				player.graphic(projectile.getGfx());
			}
		}

		player.animate(EquipmentInfo.attackAnimationFor(player));
		int distance = player.getTile().distance(target.getTile());
		int cyclesPerTile = 5;
		int baseDelay = 32;
		int startHeight = 35;
		int endHeight = 36;
		int curve = 15;
		int graphic = 228;

		if (weaponType == WeaponType.CROSSBOW) {
			cyclesPerTile = 3;
			baseDelay = 40;
			startHeight = 40;
			endHeight = 40;
			curve = 2;
			graphic = 27;
		}

		if (weaponType == WeaponType.BOW) {
			startHeight = 50;
			endHeight = 50;
		}

		if (weaponId == BLOWPIPE_ID) {
			projectile = Projectile.getProjectileForAmmoName(player.getBlowpipeAmmo().definition(player.world()).name);

		}

		if (projectile != null) {
			graphic = projectile.getProjectileId();
			startHeight = 50;
			endHeight = 50;
		}

		if (weaponType == WeaponType.THROWN) {
			startHeight = 48;
			endHeight = 40;
			baseDelay = 40;
			curve = 10;
		}

		if (weaponId == 6522) {
			cyclesPerTile = 5;
			baseDelay = 60;
			startHeight = 30;
			endHeight = 30;
			curve = 0;
		}

		if (weaponType == WeaponType.CHINCHOMPA) {
			baseDelay = 20;
			startHeight = 25;
			endHeight = 30;
		}

		if (weaponId == BLOWPIPE_ID) {
			startHeight = 35;
			endHeight = 33;
			curve = 5;
		}

		// crystal bow projectileand gfx
		if (weaponId == 4212) {
			int tileDist = player.getTile().distance(target.getTile());
			player.world().spawnProjectile(player.getTile(), target, 249, 50, 36, 30, 5 * tileDist, 15, 105);
			player.graphic(new Graphic(256, 92, 10));
		} else {
			player.world().spawnProjectile(player.getTile(), target, graphic, startHeight, endHeight, baseDelay, cyclesPerTile * distance, curve, 105);
		}
		// if dark bow then another spawnprojec
		if (weaponId == 11235) {
			player.world().spawnProjectile(player.getTile(), target, graphic, startHeight, endHeight, baseDelay + 7, cyclesPerTile * distance, curve, 105);
		}

		long delay = Math.round(Math.floor(baseDelay / 30.0) + (distance * (cyclesPerTile * 0.020) / 0.6));

		// boolean success = AccuracyFormula.doesHit(player, target,
		// CombatStyle.RANGED);

		int maxHit = CombatFormula.maximumRangedHit(player);
		player.getQuestTab().updateMaxHit(maxHit);
		int hit = AccuracyFormula.calcRangeHit(player, target, maxHit);// player.world().random(maxHit);

		// Handle enchanted bolts.
		if (hit > 0 && weaponType == WeaponType.CROSSBOW) {
			for (BoltEffects boltEffect : BoltEffects.values()) {
				if (boltEffect.getBoltId() == ammo.getId()) {

					// random(3) = 25% chance (0,1,2,3)
					int randomNumber = player.world().random(4);
					player.messageDebug("Bolt special roll: %d", randomNumber);
					if (randomNumber == 0) {
						int extraDamage = (int)Math.round(hit * (boltEffect.getPercentageExtraDamage() / 100.0));
						hit += extraDamage;
						maxHit += (int)Math.round(boltEffect.getPercentageMaxHitIncrease() / 100.0);
						player.messageDebug("Extra damage: %d", extraDamage);
						boltEffect.doSpecialAction(player, target, hit);
						target.graphic(boltEffect.getEnemyGraphic());
					}
					
				}
			}
		}
		
		// Trigger vengeance
		triggerVeng(hit);

		// Hit target
		target.hit(player, hit, (int) delay, CombatStyle.RANGED);
		
		// if dark bow then another hit
		if (weaponId == 11235) {
			int hit2 = AccuracyFormula.calcRangeHit(player, target, maxHit);// player.world().random(maxHit);
			target.hit(player, hit2, (int) delay, CombatStyle.RANGED);
		}

		// explode chin on target
		if (weaponType == WeaponType.CHINCHOMPA) {
			target.graphic(new Graphic(157, 92, 50));
		}

		// Timer is downtime.
		int weaponSpeed = player.world().equipmentInfo().weaponSpeed(weaponId);
		if (player.world().equipmentInfo().rapid(player))
			weaponSpeed--;
		player.timers().register(TimerKey.COMBAT_ATTACK, weaponSpeed);

		// After every attack, reset special.
		player.getVarps().setVarp(Varp.SPECIAL_ENABLED, 0);
	}

	private void doRangeSpecial(Player player, int weaponId) {
		player.timers().register(TimerKey.COMBAT_ATTACK, 5);
		SpecialAttacks specialAttack = SpecialAttacks.getSpecialAttack(player, weaponId);
		if (specialAttack == null) {
			return;
		}
		if (!drainSpecialEnergy(player, specialAttack.getSpecialDrain())) {
			return;
		}
		player.animate(specialAttack.getAnimationId());
		if (specialAttack.getGfx() != null) {
			player.graphic(specialAttack.getGfx());
		}
		if (specialAttack.getOpponentGfx() != null) {
			target.graphic(specialAttack.getOpponentGfx());
		}

		int tileDist = player.getTile().distance(target.getTile());
		if (specialAttack.getProjectileId() != -1) {
			player.world().spawnProjectile(player.getTile(), target, specialAttack.getProjectileId(), 40, 36, 32, 5 * tileDist, 15, 105);

			if (specialAttack.isDoubleProjectile()) {
				player.world().spawnProjectile(player.getTile(), target, specialAttack.getProjectileId(), 40, 36, 52, 5 * tileDist, 15, 105);
			}
		}

		double max = CombatFormula.maximumRangedHit(player) * specialAttack.getMaxHitMultiplier();
		player.getQuestTab().updateMaxHit((int) max);
		int hit = AccuracyFormula.calcRangeHit(player, target, (int) Math.round(max), specialAttack);// player.world().random().nextInt((int)
																										// Math.round(max));
		triggerVeng(hit);

		if (specialAttack.isHits()) {
			int delay = (int) Math.round(Math.floor(32 / 30.0) + (tileDist * (5 * 0.020) / 0.6));
			target.hit(player, hit, delay, CombatStyle.RANGED);

			if (specialAttack.isDoubleHit()) {
				// int hit2 = player.world().random().nextInt((int)
				// Math.round(max));
				int hit2 = AccuracyFormula.calcRangeHit(player, target, (int) Math.round(max), specialAttack);// player.world().random().nextInt((int)
																												// Math.round(max));

				target.hit(player, hit2, delay, CombatStyle.RANGED);
			}
		}

		// Do extra action
		specialAttack.action(player, target, hit);
	}

	public static void handleGraniteMaul(Player player, Entity target) {
		/*
		 * if (player.dead() || target.dead()) { return; }
		 */

		if (!CombatUtil.canAttack(player, target)) {
			return;
		}

		if (player.getSpecialEnergyAmount() < 50 * 10) {
			player.message("You do not have enough special energy.");
			return;
		}

		// if (!player.touches(target, player.getTile())) {
		// return;
		// }
		if (player.getTile().distance(target.getTile()) > 3) {
			return;
		}

		// player.message("Gets here");

		player.setSpecialEnergyAmount(player.getSpecialEnergyAmount() - (50 * 10));

		player.animate(1667);
		player.graphic(340, 92, 0);

		int max = (int) Math.round(CombatFormula.maximumMeleeHit(player));
		// int hit = player.world().random().nextInt((int) Math.round(max));
		int hit = AccuracyFormula.calculateHit(player, target, max);
		target.hit(player, hit);
		player.timers().register(TimerKey.COMBAT_ATTACK, target.world().equipmentInfo().weaponSpeed(4153));
	}

	private boolean drainSpecialEnergy(Player player, int amount) {
		return player.drainSpecialEnergy(amount);
	}
}
