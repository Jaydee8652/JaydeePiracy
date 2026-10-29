package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.WeaponRangeModifier;

import static Jaydee8652.JaydeeProficiencies.utils.combat.jdp_WeaponMutationUtils.getClonedProjectileSpec;
import static Jaydee8652.JaydeeProficiencies.utils.combat.jdp_WeaponMutationUtils.setProjectileSpec;

public class jdp_magneticAccelerator extends BaseHullMod {

	public static float SPEED_BONUS = 100f;
	public static float RANGE_BONUS = 100f;

	public static class jdp_standoffTargetingCore_Range implements WeaponRangeModifier {
		@Override
		public float getWeaponRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
			if (weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.BALLISTIC || weapon.getSlot().getSlotSize() != WeaponAPI.WeaponSize.LARGE || !weapon.getSlot().isHardpoint()) return 1;
			return RANGE_BONUS;
		}

		@Override
		public float getWeaponRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
			return 1;
		}

		@Override
		public float getWeaponRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
			return 0;
		}
	}

	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		super.applyEffectsAfterShipCreation(ship, id);
		if(ship == null) return;
		if (!ship.hasListenerOfClass(jdp_standoffTargetingCore_Range.class)) ship.addListener(new jdp_standoffTargetingCore_Range());

		for (WeaponAPI weapon : ship.getAllWeapons()) {
			if (weapon.getSlot().getWeaponType() != WeaponAPI.WeaponType.BALLISTIC || weapon.getSlot().getSlotSize() != WeaponAPI.WeaponSize.LARGE || !weapon.getSlot().isHardpoint()) continue;
			var spec = getClonedProjectileSpec(weapon);
			if (spec == null) return;

			spec.setMoveSpeed(spec.getMoveSpeed(null, weapon) + (spec.getMoveSpeed(null, weapon) * (SPEED_BONUS / 100)));
			spec.setCollisionClass(spec.getCollisionClassIfByFighter());
			setProjectileSpec(weapon, spec);
		}
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return (int) SPEED_BONUS + "%";
		if (index == 1) return "fire over friendly vessels";
		return null;
	}
}




