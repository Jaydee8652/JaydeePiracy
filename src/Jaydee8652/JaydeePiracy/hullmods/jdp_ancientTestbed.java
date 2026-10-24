package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class jdp_ancientTestbed extends BaseHullMod {

	public static float MISSILE_ROF_MULT = 0.5f;

	public static final float REFIT_TIME_PERCENT = 30f;
	public static float BALLISTIC_WEAPON_FLUX_INCREASE = 100f;
	public static float ENERGY_WEAPON_FLUX_INCREASE = 200f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_WEAPON_FLUX_INCREASE);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id, BALLISTIC_WEAPON_FLUX_INCREASE);


		stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_TIME_PERCENT);

		stats.getMissileRoFMult().modifyMult(id, MISSILE_ROF_MULT);
	}


	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)Math.round(REFIT_TIME_PERCENT) + "%";
		if (index == 1) return "" + (int)Math.round(BALLISTIC_WEAPON_FLUX_INCREASE) + "%";
		if (index == 2) return "" + (int)Math.round(ENERGY_WEAPON_FLUX_INCREASE) + "%";
		if (index == 3) return "" + (int)Math.round(MISSILE_ROF_MULT * 100f) + "%";
		return null;
	}

}


