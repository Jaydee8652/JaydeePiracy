package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class jdp_dumbfireConversion extends BaseHullMod {

	public final int SpeedBonus = 30;
	public final int MTurn = 100;
	public final int HullBonus = 5;
	public final int DamageBonus = 15;

	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMissileMaxSpeedBonus().modifyPercent(id, SpeedBonus);
		stats.getMissileAccelerationBonus().modifyPercent(id, SpeedBonus);
		stats.getMissileMaxTurnRateBonus().modifyPercent(id, -MTurn);
		stats.getMissileTurnAccelerationBonus().modifyPercent(id, -MTurn);
		stats.getMissileHealthBonus().modifyPercent(id, HullBonus);
		stats.getMissileWeaponDamageMult().modifyPercent(id, DamageBonus);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return SpeedBonus + "%";
		if (index == 1) return HullBonus + "%";
        if (index == 2) return MTurn + "%";
		if (index == 3) return DamageBonus + "%";
        return null;
    }
}

			
			