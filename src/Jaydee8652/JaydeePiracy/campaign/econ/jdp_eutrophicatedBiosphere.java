package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;

public class jdp_eutrophicatedBiosphere extends BaseMarketConditionPlugin {
	Integer HAZARD = 50;

	Integer DEMAND = 3;
	Integer MOD_ORGANIC = 0;

	public void apply(String id) {
		super.apply(id);
		market.getHazard().modifyFlat(id, (HAZARD / 100f), condition.getName());

		Industry farming = getIndustryByTag(market, "farming");

		if ((farming != null) && (farming.isFunctional())) {
			MOD_ORGANIC = Math.round(farming.getSupply(Commodities.FOOD).getQuantity().getModifiedValue() / 2);

			farming.getSupply(Commodities.ORGANICS).getQuantity().modifyFlat(id, MOD_ORGANIC);
			farming.getDemand(Commodities.HEAVY_MACHINERY).getQuantity().modifyFlat(id, DEMAND);
		} else if ((farming != null) && (!farming.isFunctional())) {
			farming.getSupply(Commodities.ORGANICS).getQuantity().unmodify(id);
			farming.getDemand(Commodities.HEAVY_MACHINERY).getQuantity().unmodify(id);
		}
	}

	public void unapply(String id) {
		super.unapply(id);
		market.getHazard().unmodify(id);

		Industry farming = getIndustryByTag(market, "farming");

		if (farming != null) {
			farming.getSupply(Commodities.ORGANICS).getQuantity().unmodify(id);
			farming.getDemand(Commodities.HEAVY_MACHINERY).getQuantity().unmodify(id);
		}
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+" + HAZARD + "%");
		tooltip.addPara("%s heavy machinery demand (Farming)",
				10f, Misc.getHighlightColor(),
				"+" + DEMAND);
		tooltip.addPara("Half of food production value is %s as organics production (Farming)",
				10f, Misc.getHighlightColor(),
				"also applied");
	}
}
