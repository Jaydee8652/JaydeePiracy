package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class jdp_supportFighter extends BaseHullMod {
	public static Map<String, Integer> GROUND_SUPPRT_FIGHTER_BONUS = new HashMap<String, Integer>();
	static {
		GROUND_SUPPRT_FIGHTER_BONUS.put("jdp_manjusaka_wing", 30);
	}

	public static List<String> getSupportWings(MutableShipStatsAPI stats) {
		List<String> wings = stats.getVariant().getWings();
		List<String> supportWings = new ArrayList<String>();

		for (String wing : wings) {
			if (!wing.isEmpty()) {
				if (Global.getSettings().getFighterWingSpec(wing).hasTag("jdp_groundSupportFighter")) {
					supportWings.add(wing);
				}
			}
		}
		return supportWings;
	}

	public static Integer getBonus(MutableShipStatsAPI stats) {
		List<String> supportWings = getSupportWings(stats);
		Integer totalBonus = 0;

		for (String wing : supportWings) {
			totalBonus += GROUND_SUPPRT_FIGHTER_BONUS.get(wing);
		}
		return totalBonus;
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.FLEET_GROUND_SUPPORT).modifyFlat(id, getBonus(stats));
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();

		Color c = Misc.getGrayColor();
		Color c2 = Misc.getGrayColor();

		if (ship == null) {
			tooltip.addPara("No wings currently installed on this ship. No bonus applied.", opad, Misc.getGrayColor(), Misc.getGrayColor(), "Not currently installed on a ship. No bonus applied.");
		} else if (ship != null) {
			List<String> supportWings = getSupportWings(ship.getMutableStats());
			String bonus = getBonus(ship.getMutableStats()).toString();
			Integer numberWings = supportWings.size();
			String wings = numberWings.toString();

			String plurality = " wings are ";
			if (numberWings == 1) plurality = " wing is ";

			tooltip.addPara(wings + plurality + "increasing the effective strength of planetary raids by " + bonus + ", up to the total number of marines in the fleet.", opad, Misc.getTextColor(), Misc.getHighlightColor(), new String[]{wings, bonus});

			float modW = 100f;
			float nameW = width - modW - 5f;
			tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
					20f, true, true,
					new Object [] {"Fighter", nameW, "Modifier", modW});



			for (String wing : supportWings) {
				String[] splitStr = Global.getSettings().getFighterWingSpec(wing).getWingName().split("\\s+");
				String name = splitStr[0];
				if (name.isEmpty()) name = splitStr[1];

				tooltip.addRow(Misc.getTextColor(), name + " " + Global.getSettings().getFighterWingSpec(wing).getVariant().getDisplayName(), Misc.getHighlightColor(), GROUND_SUPPRT_FIGHTER_BONUS.get(wing).toString());
			}
			tooltip.addTable("", 0, opad);
		}
	}
}

