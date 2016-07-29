package edgeville.combat;

import edgeville.model.Entity;
import edgeville.model.entity.Player;
import edgeville.model.entity.player.EquipSlot;
import edgeville.model.item.Item;

public enum SpecialAttacks {
	
	// ITEMID - ANIMATION - MYGFX - OPPGFX - SPECIALDRAIN - MULTIPLIER - DOUBLEHIT? - HITSATALL?    (projectile, doubleprojectile)
	
	DDS(5698, 1062, new Graphic(252, 92, 0), 25, 1.15, 1.25, true),
	ABYSSAL_DAGGER(13265, 1062, new Graphic(1283, 92, 0), 50, 1.15, 1.25, true),
	ABYSSAL_BLUDGEON(13263, 7010, null, new Graphic(1284), 50, 1.2, 1.5, false),
	AGS(11802, 7061, new Graphic(1211), 50, 1.25,2, false),
	BGS(11804, 7060, new Graphic(1212), 65, 1.21,2, false),
	SGS(11806, 7058, new Graphic(1209), 50, 1.10,2, false),
	ZGS(11808, 7057, new Graphic(1210), 60, 1.10,2, false),
	DRAGON_SCIMITAR(4587, 1872, new Graphic(347, 100, 0), 55, 1,1, false),
	DRAGON_SPEAR(1249, 1064, new Graphic(253, 100, 0), null, 25, 1,1, false, false),
	DRAGON_HALBERD(3204, 1203, new Graphic(1232, 100, 0), null, 25, 1,1, false, false),
	DRAGON_LONGSWORD(1305, 1058, new Graphic(248, 92, 0), 25, 1.15,1, false),
	ABYSSAL_WHIP(4151, 1658, null, new Graphic(341, 100, 0), 25, 1.15,1.25, false),
	TENTACLE_WHIP(12006, 1658, null, new Graphic(341, 100, 0), 50, 1.15,1.25, false),
	DRAGON_MACE(1434, 1060, new Graphic(251, 100, 0), null, 25, 1.5,1.25, false),
	DRAGON_2H(7158, 3157, null, null, 60, 1,1, false),
	
	MAGIC_SHORTBOW(861, 1074, new Graphic(256, 92, 0), null, 55, 1,1, true, 249, true),//projectile id, doubleprojectile
	
	DARK_BOW(11235, 426, null, new Graphic(1100, 92, 60), 55, 1.5,1, true, 1099, true),//projectile id, doubleprojectile
	
	ARMADYL_CROSSBOW(11785, 4230, null, null, 40, 1,2, false, 301, false),
	
	TOXIC_BLOWPIPE(12926, 5061, null, null, 50, 1.4, 1.5, false, 1043, false)
	;

	private int weaponId;
	private int animationId;
	private Graphic gfx;
	private Graphic opponentGfx;
	private int specialDrain;
	private double maxHitMultiplier;
	private double accuracyMultiplier;
	private boolean doubleHit;
	private boolean hits;
	
	private int projectileId;
	private boolean doubleProjectile;

	SpecialAttacks(int weaponId, int animationId, Graphic gfx, Graphic opponentGfx, int specialDrain, double maxHitMultiplier, double accuracyMultiplier, boolean doubleHit) {
		this(weaponId, animationId, gfx, opponentGfx, specialDrain, maxHitMultiplier,accuracyMultiplier, doubleHit, true);
	}
	
	SpecialAttacks(int weaponId, int animationId, Graphic gfx, int specialDrain, double maxHitMultiplier, double accuracyMultiplier, boolean doubleHit) {
		this(weaponId, animationId, gfx, null, specialDrain, maxHitMultiplier, accuracyMultiplier, doubleHit, true);
	}
	
	// this is for ranged.
	SpecialAttacks(int weaponId, int animationId, Graphic gfx, Graphic opponentGfx, int specialDrain, double maxHitMultiplier, double accuracyMultiplier,boolean doubleHit, int projectileId, boolean doubleProjectile) {
		this.weaponId = weaponId;
		this.animationId = animationId;
		this.gfx = gfx;
		this.opponentGfx = opponentGfx;
		this.specialDrain = specialDrain;
		this.maxHitMultiplier = maxHitMultiplier;
		this.accuracyMultiplier = accuracyMultiplier;
		this.doubleHit = doubleHit;
		this.hits = true;
		
		this.projectileId = projectileId;
		this.doubleProjectile = doubleProjectile;
	}

	SpecialAttacks(int weaponId, int animationId, Graphic gfx, Graphic opponentGfx, int specialDrain, double maxHitMultiplier, double accuracyMultiplier, boolean doubleHit, boolean hits) {
		this.weaponId = weaponId;
		this.animationId = animationId;
		this.gfx = gfx;
		this.opponentGfx = opponentGfx;
		this.specialDrain = specialDrain;
		this.maxHitMultiplier = maxHitMultiplier;
		this.accuracyMultiplier = accuracyMultiplier;
		this.doubleHit = doubleHit;
		this.hits = hits;
	}
	
	public int getProjectileId() {
		return projectileId;
	}

	public boolean isDoubleProjectile() {
		return doubleProjectile;
	}


	public boolean isHits() {
		return hits;
	}
	
	public Graphic getOpponentGfx() {
		return opponentGfx;
	}

	public Graphic getGfx() {
		return gfx;
	}

	public static SpecialAttacks getSpecialAttack(Player player, int weaponId) {
		for (SpecialAttacks specialAttack : SpecialAttacks.values()) {
			player.messageDebug(specialAttack.weaponId + " == " + weaponId);
			if (specialAttack.weaponId == weaponId) {
				return specialAttack;
			}
		}
		return null;
	}

	public int getWeaponId() {
		return weaponId;
	}

	public int getAnimationId() {
		return animationId;
	}

	public int getSpecialDrain() {
		return specialDrain;
	}

	public double getMaxHitMultiplier() {
		return maxHitMultiplier;
	}
	
	public double getAccuracyMultiplier() {
		return accuracyMultiplier;
	}

	public boolean isDoubleHit() {
		return doubleHit;
	}

	public void action(Player player, Entity target, int damage) {
		if (this == SpecialAttacks.BGS && target instanceof Player) {
			// Drain stuff
		}
		if (this == SpecialAttacks.SGS && target instanceof Player) {
			player.heal((int)(damage * 0.5));
			// TODO restore prayer points by 2.5% dmg dealt
		}
		if (this == SpecialAttacks.DRAGON_SPEAR) {
			target.stun(5);
		}
		if (this == SpecialAttacks.ABYSSAL_WHIP || this == SpecialAttacks.TENTACLE_WHIP) {
			//TODO drain 10% run
		}
		if (this == SpecialAttacks.TOXIC_BLOWPIPE) {
			player.heal((int)(damage * 0.5));
		}
		if (this == SpecialAttacks.DARK_BOW) {
			int tileDist = player.getTile().distance(target.getTile());
			// d bow spec dragon heads
			//player.world().spawnProjectile(player.getTile(), target, 1099, 40, 36, 52, 5 * tileDist, 15, 105);
			
			
			// player shoot gfx
			// double dragon arrow shoot 1113, todo more
			player.graphic(new Graphic(1113, 92, 0));
			
			// arrow projectiles twice
			Item ammo = player.getEquipment().get(EquipSlot.AMMO);
			int graphic = Projectile.getProjectileForAmmoId(ammo.getId()).getGfx().getId(); // get more, depending on arrow
			int cyclesPerTile = 3;
			int startHeight = 40;
			int endHeight = 30;
			int curve = 2;
			//graphic = 27;
			player.world().spawnProjectile(player.getTile(), target, graphic, startHeight, endHeight, 32, cyclesPerTile * tileDist, curve, 105);
			player.world().spawnProjectile(player.getTile(), target, graphic, startHeight, endHeight, 52, cyclesPerTile * tileDist, curve, 105);
			
			// cool smoke trails - look like poo
			//player.world().spawnProjectile(player.getTile(), target, 1101, 40, 36, 40, 5 * tileDist, 15, 105);	
			//player.world().spawnProjectile(player.getTile(), target, 1101, 40, 36, 60, 5 * tileDist, 15, 105);
		}
	}
}
