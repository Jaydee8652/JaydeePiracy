package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class jdp_erroneousOrdinance extends BaseHullMod {
	public static float CASUALTIES_PERCENT = 25f;
	public static float REPAIR_PERCENT = 10f;
	public static final float EXPLOSION_MULT = 3;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCrewLossMult().modifyPercent(id, CASUALTIES_PERCENT);

		stats.getCombatEngineRepairTimeMult().modifyPercent(id, REPAIR_PERCENT);
		stats.getCombatWeaponRepairTimeMult().modifyPercent(id, REPAIR_PERCENT);

		stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, EXPLOSION_MULT);
		stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, EXPLOSION_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) CASUALTIES_PERCENT + "%";
		if (index == 1) return "" + (int) REPAIR_PERCENT + "%";
		if (index == 2) return "" + (int) EXPLOSION_MULT;

		return null;
	}
}








