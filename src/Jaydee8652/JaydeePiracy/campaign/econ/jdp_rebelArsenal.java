package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;


public class jdp_rebelArsenal extends BaseMarketConditionPlugin {
	Integer MOD_MARINES = 0;

	public void apply(String id) {
		super.apply(id);

		Industry population = getIndustryByTag(market, Industries.TAG_POPULATION);
		Industry military = getIndustryByTag(market, Industries.TAG_GROUNDDEFENSES);

		if ((military != null) && (military.isFunctional())) {
			military.getDemand(Commodities.HAND_WEAPONS).getQuantity().modifyMult(id, 0);
		} else if ((military != null) && (!military.isFunctional())) {
			military.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
		}

		if ((population != null) && (population.isFunctional())) {
			MOD_MARINES = Math.round(population.getSupply(Commodities.CREW).getQuantity().getModifiedValue() * 2);

			population.getSupply(Commodities.MARINES).getQuantity().modifyFlat(id, MOD_MARINES);
		} else if ((population != null) && (!population.isFunctional())) {
			population.getSupply(Commodities.MARINES).getQuantity().unmodify(id);
		}
	}

	public void unapply(String id) {
		super.unapply(id);

		Industry population = getIndustryByTag(market, Industries.TAG_POPULATION);
		Industry military = getIndustryByTag(market, Industries.TAG_GROUNDDEFENSES);

		if (military != null) {
			military.getDemand(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id);
		}
		if (population != null) {
			population.getSupply(Commodities.MARINES).getQuantity().unmodify(id);
		}
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		tooltip.addPara("%s heavy armaments demand (Ground Defences and Heavy Batteries)",
				10f, Misc.getHighlightColor(),
				"Nullfies");
		tooltip.addPara("Double crew production value is %s as marine production (Population and Infrastructure)",
				10f, Misc.getHighlightColor(),
				"also applied");
	}
}
