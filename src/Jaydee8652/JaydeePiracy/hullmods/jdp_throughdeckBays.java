package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class jdp_throughdeckBays extends BaseHullMod {
	public static float REFIT_BONUS = 10f;
	public static boolean AlLOW_CONVERTED_HANGAR = true;



	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		if (!stats.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR)) {
			stats.getFighterRefitTimeMult().modifyMult(id, (REFIT_BONUS / 100));
		}

		if (AlLOW_CONVERTED_HANGAR) {
			stats.getDynamic().getMod(Stats.FORCE_ALLOW_CONVERTED_HANGAR).modifyFlat(id, 1f);

			//More bays is disproportionally better on a ship with Reserve Deployment, so keep all the malus'

			//stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_CREW_INCREASE).modifyFlat(id, 1f);
			//stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REARM_INCREASE).modifyFlat(id, 1f);
			//stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_REFIT_PENALTY).modifyFlat(id, 1f);
			//stats.getDynamic().getMod(Stats.CONVERTED_HANGAR_NO_DP_INCREASE).modifyFlat(id, 1f);
		}
	}

	@Override
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		float pad = 3f;
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();
		Color good = Misc.getPositiveHighlightColor();
		float HEIGHT = 50f;
		float PAD = 10f;

		if (ship == null) return;

		if (ship.getVariant().hasHullMod(HullMods.CONVERTED_HANGAR)) {
			tooltip.addPara("Converted Hangar has been installed on this ship, nullifying the refit time bonus.", opad, bad, bad, "Converted Hangar has been installed on this ship, nullifying the refit time bonus.");
		}
	}


	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return Math.round(100 - REFIT_BONUS) + "%";
		if (index == 1) return "Converted Hangar";
		if (index == 2) return "" + (int)Math.round(3);
        return null;
    }
}
