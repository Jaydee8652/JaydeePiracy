package Jaydee8652.JaydeePiracy.hullmods;

import Jaydee8652.JaydeePiracy.utils.jdp_Items;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.combat.ShipAPI;

import java.util.HashMap;
import java.util.Map;

import static Jaydee8652.JaydeePiracy.utils.rulecmd.jdp_VariableNameManager.nmcInventionCycle;

public class jdp_emulatedFrictionlessHull extends BaseHullMod {
	public static boolean MODIFIED = false;

	public static float ARMOR_MULT = 0.6f;
	private static final float SUPPLY_USE_MULT = 2f;
	private static final float PEAK_MULT = 0.5f;
	public static float CORONA_EFFECT_MULT = 0f;
	public static Map<ShipAPI.HullSize, Float> TIME = new HashMap<>();
	static {
		TIME.put(HullSize.FIGHTER, 0.4f);
		TIME.put(HullSize.FRIGATE, 0.40f);//1
		TIME.put(HullSize.DESTROYER, 0.35f);//0.8
		TIME.put(HullSize.CRUISER, 0.30f);//0.6
		TIME.put(HullSize.CAPITAL_SHIP, 0.25f);//0.4
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		MODIFIED = false;
		//That's good!
		stats.getTimeMult().modifyMult(id , 1f + TIME.get(hullSize));

		float mult = CORONA_EFFECT_MULT;
		stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, mult);

		//That's bad!
		stats.getEffectiveArmorBonus().modifyMult(id, ARMOR_MULT);
		stats.getMinArmorFraction().modifyMult(id, ARMOR_MULT);
		stats.getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);
	}

	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		for (ShipAPI module : ship.getChildModulesCopy()) {
			//That's good!
			module.getMutableStats().getTimeMult().modifyMult(id , 1f + TIME.get(ship.getHullSize()));
			float mult = CORONA_EFFECT_MULT;
			module.getMutableStats().getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, mult);

			//That's bad!
			module.getMutableStats().getEffectiveArmorBonus().modifyMult(id, ARMOR_MULT);
			module.getMutableStats().getMinArmorFraction().modifyMult(id, ARMOR_MULT);
			module.getMutableStats().getSuppliesPerMonth().modifyMult(id, SUPPLY_USE_MULT);
			module.getMutableStats().getPeakCRDuration().modifyMult(id, PEAK_MULT);
		}
	}

	public void advanceInCombat(ShipAPI ship, float amount) {
		String id = "jdp_frictionlesshull_" + ship.getId();

		var player = (ship == Global.getCombatEngine().getPlayerShip());
		if (player && !MODIFIED) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / (1f + TIME.get(ship.getHullSize())));
			MODIFIED = true;
		} else if (!player) {
			Global.getCombatEngine().getTimeMult().unmodify(id);
			MODIFIED = false;
		}
	}


	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "Frictionless NMC-" + nmcInventionCycle();
		if (index == 1) return "" + (int) Math.round((TIME.get(HullSize.FRIGATE) * 100)) + "%";
		if (index == 2) return "" + (int) Math.round((TIME.get(HullSize.DESTROYER) * 100)) + "%";
		if (index == 3) return "" + (int) Math.round((TIME.get(HullSize.CRUISER) * 100)) + "%";
		if (index == 4) return "" + (int) Math.round((TIME.get(HullSize.CAPITAL_SHIP) * 100)) + "%";
		if (index == 5) return "" + (int) Math.round((1f - CORONA_EFFECT_MULT) * 100f) + "%";
		if (index == 6) return "" + (int) Math.round(ARMOR_MULT * 100f) + "%";
		if (index == 7) return "" + (int) Math.round((SUPPLY_USE_MULT - 1f) * 100f) + "%";
		if (index == 8) return "2";
		return null;
	}

	public CargoStackAPI getRequiredItem() {
		return Global.getSettings().createCargoStack(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(jdp_Items.JDP_FRICTIONLESSMETAL, null), null);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship != null && ship.getHullSpec().isPhase()) {
			return false;
		}
		if (ship != null && ship.getVariant().getHullMods().contains("jdp_frictionlessHull")) {
			return false;
		}
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains("jdp_frictionlessHull")) {
			return "Incompatible, the hull of this vessel is already frictionless.";
		}
		if (ship.getHullSpec().isPhase()) {
			return "The material is incompatible with phase ships.";
		}
		return null;
	}
}




