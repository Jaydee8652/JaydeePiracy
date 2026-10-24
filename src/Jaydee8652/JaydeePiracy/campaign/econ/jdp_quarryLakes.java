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


public class jdp_quarryLakes extends BaseMarketConditionPlugin {
	Integer HAZARD = 50;
	Integer DEMAND = 2;

	public void apply(String id) {
		super.apply(id);
		market.getHazard().modifyFlat(id, (HAZARD / 100f), condition.getName());

		Industry refining = getIndustryByTag(market, Industries.REFINING);

		if ((refining != null) && (refining.isFunctional())) {
			refining.getDemand(Commodities.ORE).getQuantity().modifyMult(id, 0);
			refining.getDemand(Commodities.RARE_ORE).getQuantity().modifyMult(id, 0);
			refining.getDemand(Commodities.HEAVY_MACHINERY).getQuantity().modifyFlat(id, DEMAND);
		} else if ((refining != null) && (!refining.isFunctional())) {
			refining.getDemand(Commodities.ORE).getQuantity().unmodify(id);
			refining.getDemand(Commodities.RARE_ORE).getQuantity().unmodify(id);
			refining.getDemand(Commodities.HEAVY_MACHINERY).getQuantity().unmodify(id);
		}
	}

	public void unapply(String id) {
		super.unapply(id);

		market.getHazard().unmodify(id);

		Industry refining = getIndustryByTag(market, Industries.REFINING);

		if (refining != null) {
			refining.getDemand(Commodities.ORE).getQuantity().unmodify(id);
			refining.getDemand(Commodities.RARE_ORE).getQuantity().unmodify(id);
			refining.getDemand(Commodities.HEAVY_MACHINERY).getQuantity().unmodify(id);
		}
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+" + HAZARD + "%");
		tooltip.addPara("%s heavy machinery demand (Refining)",
				10f, Misc.getHighlightColor(),
				"+" + DEMAND);
		tooltip.addPara("%s ore and transplutonic ore demand (Refining)",
				10f, Misc.getHighlightColor(),
				"Nullfies");
	}
}
