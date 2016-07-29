package edgeville.util;

import java.security.SecureRandom;
import java.util.Random;

import edgeville.combat.SpecialAttacks;
import edgeville.model.Entity;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;
import edgeville.model.entity.player.skills.Prayers;

/**
 * @author Simon on 8/22/2015.
 */
public class AccuracyFormula {

	public static final SecureRandom srand = new SecureRandom();

	public static void main(String[] args) {
	}

	public static boolean doesHit(Player player, Entity enemy, CombatStyle style) {
		EquipmentInfo.Bonuses playerBonuses = CombatFormula.totalBonuses(player, player.world().equipmentInfo());
		EquipmentInfo.Bonuses targetBonuses = CombatFormula.totalBonuses(enemy, player.world().equipmentInfo());

		/*
		 * S E T T I N G S
		 * 
		 * S T A R T
		 */

		// attack stances
		int off_stance_bonus = 0; // accurate, aggressive, controlled, defensive
		int def_stance_bonus = 0; // accurate, aggressive, controlled, defensive

		// requirements
		int off_weapon_requirement = 1; // weapon attack level requirement
		int off_spell_requirement = 1; // spell magic level requirement

		// base levels
		int off_base_attack_level = (int) (player.skills().xpLevel(Skills.ATTACK) * 1.5);
		int off_base_ranged_level = player.skills().xpLevel(Skills.RANGED);
		int off_base_magic_level = player.skills().xpLevel(Skills.MAGIC);

		// current levels
		double off_current_attack_level = player.skills().level(Skills.ATTACK) * 1.5;
		double off_current_ranged_level = player.skills().level(Skills.RANGED);
		double off_current_magic_level = player.skills().level(Skills.MAGIC);

		double def_current_defence_level = 1, def_current_magic_level = 1;
		if (enemy instanceof Player) {
			Player enemenyPlayer = (Player) enemy;

			def_current_defence_level = enemenyPlayer.skills().level(Skills.DEFENCE);
			def_current_magic_level = enemenyPlayer.skills().level(Skills.MAGIC);
		}

		// prayer bonuses
		double off_attack_prayer_bonus = 1.0;
		double off_ranged_prayer_bonus = 1.0;
		double off_magic_prayer_bonus = 1.0;
		double def_defence_prayer_bonus = 1.0;

		// additional bonus
		double off_additional_bonus = 1.0;

		// equipment bonuses
		int off_equipment_stab_attack = playerBonuses.stab;
		int off_equipment_slash_attack = playerBonuses.slash;
		int off_equipment_crush_attack = playerBonuses.crush;
		int off_equipment_ranged_attack = playerBonuses.range;
		int off_equipment_magic_attack = playerBonuses.mage;

		int def_equipment_stab_defence = targetBonuses.stabdef;
		int def_equipment_slash_defence = targetBonuses.slashdef;
		int def_equipment_crush_defence = targetBonuses.crushdef;
		int def_equipment_ranged_defence = targetBonuses.rangedef;
		int def_equipment_magic_defence = targetBonuses.magedef;

		// protect from * prayers
		boolean def_protect_from_melee = player.getVarps().getVarbit(Varbit.PROTECT_FROM_MELEE) == 1;
		boolean def_protect_from_ranged = player.getVarps().getVarbit(Varbit.PROTECT_FROM_MISSILES) == 1;
		boolean def_protect_from_magic = player.getVarps().getVarbit(Varbit.PROTECT_FROM_MAGIC) == 1;

		// chance bonuses
		double off_special_attack_bonus = 1.0;
		double off_void_bonus = 1.0;

		/*
		 * S E T T I N G S
		 * 
		 * E N D
		 */

		/*
		 * C A L C U L A T E D V A R I A B L E S
		 * 
		 * S T A R T
		 */

		// experience bonuses
		double off_spell_bonus = 0;
		double off_weapon_bonus = 0;

		// effective levels
		double effective_attack = 0;
		double effective_magic = 0;
		double effective_defence = 0;

		// relevent equipment bonuses
		int off_equipment_bonus = 0;
		int def_equipment_bonus = 0;

		// augmented levels
		double augmented_attack = 0;
		double augmented_defence = 0;

		// hit chances
		double hit_chance = 0;
		double off_hit_chance = 0;
		double def_block_chance = 0;

		/*
		 * C A L C U L A T E D V A R I A B L E S
		 * 
		 * E N D
		 */

		// determine effective attack
		switch (style) {
		case MELEE:
			if (off_base_attack_level > off_weapon_requirement) {
				off_weapon_bonus = (off_base_attack_level - off_weapon_requirement) * .3;
			}

			effective_attack = Math.floor(((off_current_attack_level * off_attack_prayer_bonus) * off_additional_bonus)
					+ off_stance_bonus + off_weapon_bonus);
			effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) + def_stance_bonus);

			/*
			 * switch(off_style) { case "stab": off_equipment_bonus =
			 * off_equipment_stab_attack; def_equipment_bonus =
			 * def_equipment_stab_defence; break; case "slash":
			 * off_equipment_bonus = off_equipment_slash_attack;
			 * def_equipment_bonus = def_equipment_slash_defence; break; case
			 * "crush": off_equipment_bonus = off_equipment_crush_attack;
			 * def_equipment_bonus = def_equipment_crush_defence; break; }
			 */

			off_equipment_bonus = Math.max(Math.max(off_equipment_stab_attack, off_equipment_slash_attack),
					off_equipment_crush_attack);
			def_equipment_bonus = Math.max(Math.max(def_equipment_stab_defence, def_equipment_slash_defence),
					def_equipment_crush_defence);
			break;
		case RANGED:
			if (off_base_ranged_level > off_weapon_requirement) {
				off_weapon_bonus = (off_base_ranged_level - off_weapon_requirement) * .3;
			}
			effective_attack = Math.floor(((off_current_ranged_level * off_ranged_prayer_bonus) * off_additional_bonus)
					+ off_stance_bonus + off_weapon_bonus);
			effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) + def_stance_bonus);
			off_equipment_bonus = off_equipment_ranged_attack;
			def_equipment_bonus = def_equipment_ranged_defence;

			if (CombatFormula.wearingVoidRange(player)) {
				effective_attack *= 1.1;
			}
			break;
		case MAGIC:
			if (off_base_magic_level > off_spell_requirement) {
				off_spell_bonus = (off_base_magic_level - off_spell_requirement) * .3;
			}
			effective_attack = Math.floor(
					((off_current_magic_level * off_magic_prayer_bonus) * off_additional_bonus) + off_spell_bonus);
			effective_magic = Math.floor(def_current_magic_level * .7);
			effective_defence = Math.floor((def_current_defence_level * def_defence_prayer_bonus) * .3);
			effective_defence = effective_defence + effective_magic;
			off_equipment_bonus = off_equipment_magic_attack;
			def_equipment_bonus = def_equipment_magic_defence;
			break;
		}

		// determine augmented levels
		augmented_attack = Math.floor(((effective_attack + 8) * (off_equipment_bonus + 64.)) / 10.);
		augmented_defence = Math.floor(((effective_defence + 8) * (def_equipment_bonus + 64.)) / 10.);

		// determine hit chance
		if (augmented_attack < augmented_defence) {
			hit_chance = (augmented_attack - 1) / (augmented_defence * 2);
		} else {
			hit_chance = 1 - ((augmented_defence + 1) / (augmented_attack * 2));
		}

		switch (style) {
		case MELEE:
			if (def_protect_from_melee) {
				off_hit_chance = Math.floor((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.);
				def_block_chance = Math
						.floor(101 - ((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.));
			} else {
				off_hit_chance = Math.floor(((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.);
				def_block_chance = Math
						.floor(101 - (((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.));
			}
			break;
		case RANGED:
			if (def_protect_from_ranged) {
				off_hit_chance = Math.floor((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.);
				def_block_chance = Math
						.floor(101 - ((((hit_chance * off_special_attack_bonus) * off_void_bonus) * .6) * 100.));
			} else {
				off_hit_chance = Math.floor(((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.);
				def_block_chance = Math
						.floor(101 - (((hit_chance * off_special_attack_bonus) * off_void_bonus) * 100.));
			}
			break;
		case MAGIC:
			if (def_protect_from_magic) {
				off_hit_chance = Math.floor(((hit_chance * off_void_bonus) * .6) * 100.);
				def_block_chance = Math.floor(101 - (((hit_chance * off_void_bonus) * .6) * 100.));
			} else {
				off_hit_chance = Math.floor((hit_chance * off_void_bonus) * 100.);
				def_block_chance = Math.floor(101 - ((hit_chance * off_void_bonus) * 100.));
				off_hit_chance *= 1.06;
			}
			break;
		}

		// print hit chance
		// System.out.println("\nYour chance to hit is: " + off_hit_chance +
		// "%");
		// System.out.println("Your opponents chance to block is: " +
		// def_block_chance + "%");

		// roll dice
		if (off_hit_chance <= 0)
			off_hit_chance = 2;

		off_hit_chance = srand.nextInt((int) off_hit_chance);
		off_hit_chance *= 1.45;
		def_block_chance = srand.nextInt((int) def_block_chance);

		// print roll
		// System.out.println("\nYou rolled: " + (int) off_hit_chance);
		// System.out.println("Your opponent rolled: " + (int)
		// def_block_chance);

		// determine hit
		return off_hit_chance > def_block_chance;
	} // end main

	public static int calculateHitAgainstNPC(Player player, Entity target, int maxHit, CombatStyle combatStyle) {
		boolean success = AccuracyFormula.doesHit(player, target, combatStyle);
		return success ? player.world().random(maxHit) : 0;
	}

	public static int calcHitNEWMage(Player player, Player target, int maxHit) {
		int magicLevel = player.skills().level(Skills.MAGIC);

		int magicAttack = magicLevel + CombatFormula.totalBonuses(player, player.world().equipmentInfo()).mage;
		int magicDefence = CombatFormula.totalBonuses(target, target.world().equipmentInfo()).magedef;

		// EquipmentInfo.Bonuses playerBonuses =
		// CombatFormula.totalBonuses(player, player.world().equipmentInfo());

		// TODO TODO PRAYERS
		// magicAttack *= prayerMagicAccuracyMultiplier(player);

		// player.message("MA: %d, MD: %d", magicAttack, magicDefence);

		// int maxReducer = 0;//maxHit;

		// maxReducer -= maxHit * ;

		int EA = magicAttack + 8;
		int a = (int) ((EA * (64 + 0)) / 10.0);

		int ED = magicDefence + 8;
		int d = (int) ((ED * (64 + 0)) / 10.0);

		// a*=100;
		// d*=100;

		//player.messageDebug("MA: %d, MD: %d", a, d);

		double accuracy = 0;

		if (a > d) {
			accuracy = (double) 1 - (d + 1) / (2.0 * a);
		} else {
			accuracy = (double) (a - 1) / (2.0 * d);
		}

		accuracy *= 100;

		magicAttack *= prayerMagicAccuracyMultiplier(player);
		if (CombatFormula.wearingVoidMage(player))
			magicAttack *= 1.3;
		
		magicAttack *= 1.5; // random buff.
		
		player.messageDebug("Accuracy: %d", (int) accuracy);

		int maxReducer = (int) (100 - (accuracy));

		if (maxReducer <= 0)
			maxReducer = 1;

		//player.message("Max hit reduce: %d", maxReducer);

		int randomHit = player.world().random((int) maxHit + 1);
		int randomReducer = player.world().random(maxReducer);

		int finalHit = randomHit - randomReducer;

		if (finalHit < 0)
			finalHit = 0;
		if (finalHit > maxHit)
			finalHit = (int) maxHit;

		return finalHit;
	}
	
	public static int calculateHit(Player player, Entity target, int maxHit) {
		if (target instanceof Player) {
			return calculateHit(player, (Player) target, maxHit, null);
		} else {
			return player.world().random().nextInt((int) Math.round(maxHit));
		}
	}

	public static int calculateHit(Player player, Entity target, int maxHit, SpecialAttacks specialAttack) {
		if (target instanceof Player) {
			return calculateHit(player, (Player) target, maxHit, specialAttack);
		} else {
			return player.world().random().nextInt((int) Math.round(maxHit));
		}
	}
	
	/*public static int calculateHit(Player player, Player target, int maxHit) {
		return calculateHit(player, target, maxHit, null);
	}*/

	public static int calculateHit(Player player, Player target, int maxHit,SpecialAttacks specialAttack) {
		int playerAttackLevel = player.skills().level(Skills.ATTACK);
		int targetDefenceLevel = target.skills().level(Skills.DEFENCE);

		int playerAttackBonus = player.world().equipmentInfo().getAttackBonus(player);
		int targetDefenceBonus = target.world().equipmentInfo().getDefenceBonus(target, player);

		/*
		 * Generate attackers effective accuracy.
		 */
		int EA = playerAttackLevel + playerAttackBonus + 8;
		int a = (int) ((EA * (64 + 0)) / 10.0);

		//player.message("attackbonus:%d", a);

		/*
		 * Generate defenders effective defense.
		 */
		int ED = targetDefenceLevel + targetDefenceBonus + 8;
		int d = (int) ((ED * (64 + 0)) / 10.0);

		//player.message("a:%d, d:%d", a, d);

		/*
		 * generate final accuracy rating between 0-1 and then multiply by 100
		 * to generate a more meaningful number.
		 */
		double accuracy = 0;

		if (a > d) {
			accuracy = (double) 1 - (d + 1) / (2.0 * a);
		} else {
			accuracy = (double) (a - 1) / (2.0 * d);
		}

		accuracy *= 100;

		accuracy *= prayerMeleeAccuracyMultiplier(player);
		accuracy /= prayerMeleeDefenceMultiplier(target);
		
		if (specialAttack != null) 
			accuracy *= specialAttack.getAccuracyMultiplier();
		
		if (CombatFormula.wearingVoidMelee(player))
			accuracy *= 1.1;

		accuracy += 5;//15 !!!!
		player.messageDebug("Accuracy: %d", (int) accuracy);

		/*
		 * Using the accuracy value we can then generate a reducing value which
		 * indicates the maximum damage the defense/accuracy will soak up.
		 */
		int maxReducer = (int) (maxHit - ((maxHit / 100.0) * accuracy));
		if (maxReducer <= 0)
			maxReducer = 1;

		//player.message("Max reducer: %d", maxReducer);

		/*
		 * Generate a random hit using the max hit and a random defense/accuracy
		 * reducer from the maxReducer value.
		 * 
		 * NOTE - // + 1 because value specified is exclusive on nextInt.
		 * 
		 */
		// Random rand = new Random();
		int randomHit = player.world().random((int) maxHit + 1);
		int randomReducer = player.world().random(maxReducer + 1);

		/*
		 * Subtract our random reducer from our final hit.
		 */
		int finalHit = randomHit - randomReducer;

		if (finalHit < 0)
			finalHit = 0;
		if (finalHit > maxHit)
			finalHit = (int) maxHit;

		//System.out.println(finalHit + " " + maxReducer);
		// player.shout("AttackLevel:"+playerAttackLevel+" Maxhit:" + maxHit + "
		// Final hit:" + finalHit + " Attackbonus:"+playerAttackBonus + "
		// TargetDefencedbonus:"+targetDefenceBonus);

		return finalHit;
	}

	private static double prayerMagicAccuracyMultiplier(Player player) {
		double base = 1.00;
		// Prayer multipliers
		if (player.getPrayer().isPrayerOn(Prayers.MYSTIC_WILL)) {
			base *= 1.05;
		}
		if (player.getPrayer().isPrayerOn(Prayers.MYSTIC_LORE)) {
			base *= 1.10;
		}
		if (player.getPrayer().isPrayerOn(Prayers.MYSTIC_MIGHT)) {
			base *= 1.15;
		}
		return base;
	}
	
	private static double prayerRangedAccuracyMultiplier(Player player) {
		double base = 1.00;
		// Prayer multipliers
		if (player.getPrayer().isPrayerOn(Prayers.SHARP_EYE)) {
			base *= 1.05;
		}
		if (player.getPrayer().isPrayerOn(Prayers.HAWK_EYE)) {
			base *= 1.10;
		}
		if (player.getPrayer().isPrayerOn(Prayers.EAGLE_EYE)) {
			base *= 1.15;
		}
		return base;
	}

	private static double prayerMeleeAccuracyMultiplier(Player player) {
		double base = 1.00;
		// Prayer multipliers
		if (player.getPrayer().isPrayerOn(Prayers.CLARITY_OF_THOUGHT)) {
			base *= 1.05;
		}
		if (player.getPrayer().isPrayerOn(Prayers.IMPROVED_REFLEXES)) {
			base *= 1.10;
		}
		if (player.getPrayer().isPrayerOn(Prayers.INCREDIBLE_REFLEXES)) {
			base *= 1.15;
		}
		if (player.getPrayer().isPrayerOn(Prayers.CHILVAlRY)) {
			base *= 1.15;
		}
		if (player.getPrayer().isPrayerOn(Prayers.PIETY)) {
			base *= 1.20;
		}
		return base;
	}

	private static double prayerMeleeDefenceMultiplier(Player player) {
		double base = 1.00;
		// Prayer multipliers
		if (player.getPrayer().isPrayerOn(Prayers.THICK_SKIN)) {
			base *= 1.05;
		}
		if (player.getPrayer().isPrayerOn(Prayers.ROCK_SKIN)) {
			base *= 1.10;
		}
		if (player.getPrayer().isPrayerOn(Prayers.STEEL_SKIN)) {
			base *= 1.15;
		}
		if (player.getPrayer().isPrayerOn(Prayers.CHILVAlRY)) {
			base *= 1.20;
		}
		if (player.getPrayer().isPrayerOn(Prayers.PIETY)) {
			base *= 1.25;
		}
		return base;
	}

	// NOT USED
	public static int CALCHITBACKUP(Player player, Player target, int maxHit) {
		int playerAttackLevel = player.skills().level(Skills.ATTACK);
		int targetDefenceLevel = target.skills().level(Skills.DEFENCE);

		int playerAttackBonus = player.world().equipmentInfo().getAttackBonus(player);
		int targetDefenceBonus = target.world().equipmentInfo().getDefenceBonus(target, player);

		/*
		 * Generate attackers effective accuracy.
		 */
		int EA = playerAttackLevel + playerAttackBonus + 8;
		int a = (EA * (64 + 0)) / 10;

		/*
		 * Generate defenders effective defense.
		 */
		int ED = targetDefenceLevel + targetDefenceBonus + 8;
		int d = (ED * (64 + 0)) / 10;

		/*
		 * generate final accuracy rating between 0-1 and then multiply by 100
		 * to generate a more meaningful number.
		 */
		double accuracy = 0;

		if (a > d) {
			accuracy = (double) 1 - (d + 1) / (2.0 * a);
		} else {
			accuracy = (double) (a - 1) / (2.0 * d);
		}

		accuracy *= 100;

		player.messageDebug("Accuracy: %d", (int) accuracy);

		/*
		 * Generate the attackers max hit.
		 */
		// double maxHit = 32;

		/*
		 * Using the accuracy value we can then generate a reducing value which
		 * indicates the maximum damage the defense/accuracy will soak up.
		 */
		int maxReducer = (int) (maxHit - (maxHit / 100.0) * accuracy);
		if (maxReducer <= 0)
			maxReducer = 1;

		//player.message("Max reducer: %d", maxReducer);

		/*
		 * Generate a random hit using the max hit and a random defense/accuracy
		 * reducer from the maxReducer value.
		 * 
		 * NOTE - // + 1 because value specified is exclusive on nextInt.
		 * 
		 */
		// Random rand = new Random();
		int randomHit = player.world().random((int) maxHit + 1);
		int randomReducer = player.world().random(maxReducer + 1);

		/*
		 * Subtract our random reducer from our final hit.
		 */
		int finalHit = randomHit - randomReducer;

		if (finalHit < 0)
			finalHit = 0;
		if (finalHit > maxHit)
			finalHit = (int) maxHit;

		//System.out.println(finalHit + " " + maxReducer);

		return finalHit;
	}
	
	public static int calcRangeHit(Player player, Entity target, int maxHit) {
		return calcRangeHit(player, target, maxHit, null);
	}
	
	public static int calcRangeHit(Player player, Entity target, int maxHit, SpecialAttacks specialAttack) {
		if (target instanceof Player) {
			return calcRangeHitPlayer(player, (Player)target, maxHit, specialAttack);
		} else {
			boolean success = AccuracyFormula.doesHit(player, target, CombatStyle.RANGED);
			return success ? player.world().random(maxHit) : 0;
		}
	}

	public static int calcRangeHitPlayer(Player player, Player target, int maxHit, SpecialAttacks specialAttack) {
		int playerRangeLevel = player.skills().level(Skills.RANGED);
		int targetDefenceLevel = target.skills().level(Skills.DEFENCE);

		int playerRangeBonus = player.world().equipmentInfo().getRangedBonus(player);
		int targetDefenceBonus = target.world().equipmentInfo().getRangedDefenceBonus(target);

		/*
		 * Generate attackers effective accuracy.
		 */
		int EA = playerRangeLevel + playerRangeBonus + 8;
		int a = (int) ((EA * (64 + 0)) / 10.0);

		//player.message("attackbonus:%d", a);

		/*
		 * Generate defenders effective defense.
		 */
		int ED = targetDefenceLevel + targetDefenceBonus + 8;
		int d = (int) ((ED * (64 + 0)) / 10.0);

		//player.message("a:%d, d:%d", a, d);

		/*
		 * generate final accuracy rating between 0-1 and then multiply by 100
		 * to generate a more meaningful number.
		 */
		double accuracy = 0;

		if (a > d) {
			accuracy = (double) 1 - (d + 1) / (2.0 * a);
		} else {
			accuracy = (double) (a - 1) / (2.0 * d);
		}

		accuracy *= 100;

		// TODO
		accuracy *= prayerRangedAccuracyMultiplier(player);
		accuracy /= prayerMeleeDefenceMultiplier(target);
		
		if (specialAttack != null) 
			accuracy *= specialAttack.getAccuracyMultiplier();
		
		if (CombatFormula.wearingVoidRange(player))
			accuracy *= 1.1;

		//accuracy += 30;//15 !!!!
		accuracy *= 1.2;
		
		player.messageDebug("Accuracy: %d", (int) accuracy);

		/*
		 * Using the accuracy value we can then generate a reducing value which
		 * indicates the maximum damage the defense/accuracy will soak up.
		 */
		int maxReducer = (int) (maxHit - ((maxHit / 100.0) * accuracy));
		if (maxReducer <= 0)
			maxReducer = 1;

		// Random rand = new Random();
		int randomHit = player.world().random((int) maxHit + 1);
		int randomReducer = player.world().random(maxReducer + 1);

		/*
		 * Subtract our random reducer from our final hit.
		 */
		int finalHit = randomHit - randomReducer;

		if (finalHit < 0)
			finalHit = 0;
		if (finalHit > maxHit)
			finalHit = (int) maxHit;

		return finalHit;
	}

}
