package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class jdp_heavyEnergyIntegration extends BaseHullMod {
	
	private static final float WEAPON_MALFUNCTION_PROB = 0.0025f;
	private static final float ENGINE_MALFUNCTION_PROB = 0.00025f;
	public static final float COST_REDUCTION  = 10;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {		
		stats.getCriticalMalfunctionChance().modifyFlat(id, ENGINE_MALFUNCTION_PROB);
		stats.getWeaponMalfunctionChance().modifyFlat(id, WEAPON_MALFUNCTION_PROB);
		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) COST_REDUCTION + "";
		return null;
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}








