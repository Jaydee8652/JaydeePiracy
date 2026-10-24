package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class jdp_slatArmor extends BaseHullMod {

	public static float ARMOR_MULT = 0.8f;
	public static float DAMAGE_REDUCTION = 0.8f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEffectiveArmorBonus().modifyMult(id, ARMOR_MULT);
		stats.getMinArmorFraction().modifyMult(id, ARMOR_MULT);

		stats.getHighExplosiveDamageTakenMult().modifyPercent(id, DAMAGE_REDUCTION);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(ARMOR_MULT * 100f) + "%";
		if (index == 1) return "" + (int) Math.round((1 - DAMAGE_REDUCTION) * 100f) + "%";
		return null;
	}

}
