package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;

public class jdp_externalMissileRacks extends BaseHullMod {
	public static float AMMO_BONUS = 100f;
	public static float ROF_MALUS = 0.8f;
	public static float AMMO_LOSS = 0.5f;

	public static String DESTROYED_KEY = "jdp_destroyedCargo";

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileAmmoBonus().modifyPercent(id, AMMO_BONUS);
		stats.getMissileRoFMult().modifyPercent(id, ROF_MALUS);
	}

	public void advanceInCombat(ShipAPI ship, float amount) {
		if(!ship.getCustomData().containsKey(DESTROYED_KEY)) {
			for (ShipAPI module : ship.getChildModulesCopy()) {
				if (!module.isAlive()) {
					ship.getCustomData().put(DESTROYED_KEY, true);
					var missileWeapons = ship.getAllWeapons().stream().filter(w -> w.getType().equals(WeaponAPI.WeaponType.MISSILE)).toList();
					for (WeaponAPI weapon : missileWeapons) {
						weapon.setAmmo(Math.round(weapon.getAmmo() * AMMO_LOSS));
						weapon.disable();
					}
				}
			}
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AMMO_BONUS + "%";
		if (index == 1) return "" + (int) Math.round((1 - ROF_MALUS) * 100) + "%";
		if (index == 2) return "" + (int) Math.round((1 - AMMO_LOSS) * 100) + "%";
		return null;
	}
}
