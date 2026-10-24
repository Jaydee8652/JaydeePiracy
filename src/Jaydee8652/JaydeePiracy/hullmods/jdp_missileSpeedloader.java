package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class jdp_missileSpeedloader extends BaseHullMod {

	public static float SPEED_BONUS = 200f;
	public static float AMMO_MALICE = 50f;


	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileRoFMult().modifyPercent(id, SPEED_BONUS);
		stats.getMissileAmmoBonus().modifyMult(id, 1f - AMMO_MALICE * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SPEED_BONUS + "%";
		if (index == 1) return "" + (int) AMMO_MALICE + "%";
		return null;
	}

}
