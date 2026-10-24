package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class jdp_sixthBattlegroup extends BaseHullMod {

	public static final float HANDLING_MULT = 1.1f;
	public static final float ARMOUR_MULT = 0.95f;
	public static final float FLUX_MULT = 1.05f;

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyMult(id, HANDLING_MULT);
		stats.getAcceleration().modifyMult(id, HANDLING_MULT);
		stats.getDeceleration().modifyMult(id, HANDLING_MULT);
		stats.getMaxTurnRate().modifyMult(id, HANDLING_MULT);
		stats.getTurnAcceleration().modifyMult(id, HANDLING_MULT);
		stats.getArmorBonus().modifyMult(id, ARMOUR_MULT);
		stats.getFluxCapacity().modifyMult(id, FLUX_MULT);
		stats.getFluxDissipation().modifyMult(id, FLUX_MULT);

	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round((1f - ARMOUR_MULT) * 100f) + "%";
		if (index == 1) return "" + (int) Math.round((HANDLING_MULT- 1f) * 100f) + "%";
		if (index == 2) return "" + (int) Math.round((FLUX_MULT - 1f) * 100f) + "%"; 
		return null;
	}

}
