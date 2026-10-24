package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.MILITARY_TAGS;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;

public class jdp_encroachingBiofactory extends BaseMarketConditionPlugin {

	Integer HAZARD = 75;

	Integer DEMAND = 1;
	Integer MOD_ORGANS = 0;

public void apply(String id) {
	super.apply(id);
	DEMAND = market.getSize();

	market.getHazard().modifyFlat(id, (HAZARD / 100f), condition.getName());

	Industry military = getIndustryByTag(market, MILITARY_TAGS, Industries.TAG_PATROL);
	Industry population = getIndustryByTag(market, Industries.TAG_POPULATION);

	if ((military != null) && (military.isFunctional())) {
		MOD_ORGANS = Math.round(military.getSupply(Commodities.MARINES).getQuantity().getModifiedValue());

		military.getSupply(Commodities.ORGANS).getQuantity().modifyFlat(id, MOD_ORGANS);
	} else if ((military != null) && (!military.isFunctional())) {
		military.getSupply(Commodities.ORGANS).getQuantity().unmodify(id);
	}

	if ((population != null) && (population.isFunctional())) {
		population.getDemand(Commodities.FUEL).getQuantity().modifyFlat(id, DEMAND);
	} else if ((population != null) && (!population.isFunctional())) {
		population.getDemand(Commodities.FUEL).getQuantity().unmodify(id);
	}
}

public void unapply(String id) {
	super.unapply(id);

	market.getHazard().unmodify(id);

	Industry military = getIndustryByTag(market, MILITARY_TAGS, Industries.TAG_PATROL);
	Industry population = getIndustryByTag(market, Industries.TAG_POPULATION);

	if (military != null) {
		military.getSupply(Commodities.ORGANS).getQuantity().unmodify(id);
	}
	if (population != null) {
		population.getDemand(Commodities.FUEL).getQuantity().unmodify(id);
	}
}

protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
	super.createTooltipAfterDescription(tooltip, expanded);
	tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+" + HAZARD + "%");
	tooltip.addPara("%s fuel demand per size level (Population & Infrastructure)",
			10f, Misc.getHighlightColor(),
			"+1");
	tooltip.addPara("Marine production value is %s as harvested organ production (Military Base, High Command)",
			10f, Misc.getHighlightColor(),
			"also applied");
	}
}

