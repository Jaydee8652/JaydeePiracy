package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class jdp_banisherCore extends BaseHullMod {
	public static float DAMAGE_BONUS = 100f;
	public static float DAMAGE_MALICE = 75f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDamageToFighters().modifyPercent(id, DAMAGE_BONUS);
		stats.getDamageToDestroyers().modifyPercent(id, DAMAGE_BONUS);

		stats.getDamageToCruisers().modifyMult(id, 1f - DAMAGE_MALICE * 0.01f);
		stats.getDamageToCapital().modifyMult(id, 1f - DAMAGE_MALICE * 0.01f);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return Math.round(DAMAGE_BONUS) + "%";
        if (index == 1) return Math.round(100f - DAMAGE_MALICE) + "%";
        return null;
    }
}
