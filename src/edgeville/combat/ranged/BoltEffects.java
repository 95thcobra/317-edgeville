package edgeville.combat.ranged;

import edgeville.combat.Graphic;
import edgeville.model.Entity;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.Skills;

public enum BoltEffects {
	OPAL_BOLTS(9236, new Graphic(749), 25), 
	SAPPHIRE_BOLTS(9240, new Graphic(750), 0), 
	EMERALD_BOLTS(9241, new Graphic(752), 0), 
	RUBY_BOLTS(9242, new Graphic(754), 0), 
	DIAMOND_BOLTS(9243, new Graphic(758), 0, 15), 
	DRAGON_BOLTS(9244, new Graphic(756), 45), 
	ONYX_BOLTS(9245, new Graphic(753), 20);

	private int boltId;
	private Graphic enemyGraphic;
	private int percentageExtraDamage;
	private int percentageMaxHitIncrease;

	public int getPercentageMaxHitIncrease() {
		return percentageMaxHitIncrease;
	}

	BoltEffects(int boltId, Graphic enemyGraphic, int percentageExtraDamage) {
		this(boltId, enemyGraphic, percentageExtraDamage, 0);
	}

	BoltEffects(int boltId, Graphic enemyGraphic, int percentageExtraDamage, int percentageMaxHitIncrease) {
		this.boltId = boltId;
		this.enemyGraphic = enemyGraphic;
		this.percentageExtraDamage = percentageExtraDamage;
	}

	public void doSpecialAction(Player player, Entity target, int hit) {
		switch (this) {
		// opal has none

		case SAPPHIRE_BOLTS:
			if (target instanceof Player) {
				Player targetP = (Player) target;
				int prayerToDecrease = (int) Math.round(player.skills().level(Skills.RANGED) * (1.0 / 20.0));
				player.messageDebug("Prayer to decrease: %d", prayerToDecrease);
				targetP.skills().alterSkill(Skills.PRAYER, -prayerToDecrease, true);
				player.skills().alterSkillUnder99(Skills.PRAYER, (int) Math.round(prayerToDecrease / 2.0), true);
			}
			break;

		case EMERALD_BOLTS:
			//TODO POISON TARGET: extra strong poison 5 dmg.
			break;

		case RUBY_BOLTS:
			// Hit the player for 10%.
			int playerHp = player.hp();
			int playerHit = (int) Math.round(0.1 * playerHp);
			player.hit(target, playerHit);

			// Hit the target for 20%.
			int targetHp = target.hp();
			int targetHit = (int) Math.round(0.2 * targetHp);
			target.hit(player, targetHit);
			break;
			
		case ONYX_BOLTS:
			player.heal((int)Math.round(hit / 4.0)); // Heal for 25%
			break;
		}
	}

	public int getPercentageExtraDamage() {
		return percentageExtraDamage;
	}

	public int getBoltId() {
		return boltId;
	}

	public Graphic getEnemyGraphic() {
		return enemyGraphic;
	}
}
