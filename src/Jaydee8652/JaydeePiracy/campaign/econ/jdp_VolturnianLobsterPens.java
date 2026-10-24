package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;

public class jdp_VolturnianLobsterPens extends BaseMarketConditionPlugin {

	Integer MOD_LOBSTER = 1;

	public void apply(String id) {
		super.apply(id);

		Industry aquaculture = getIndustryByTag(market, "aquaculture");

		if ((aquaculture != null) && (aquaculture.isFunctional())) {
			aquaculture.getSupply(Commodities.LOBSTER).getQuantity().modifyFlat(id, MOD_LOBSTER);
		} else if ((aquaculture != null) && (!aquaculture.isFunctional())) {
			aquaculture.getSupply(Commodities.LOBSTER).getQuantity().unmodify(id);
		}
	}

	public void unapply(String id) {
		super.unapply(id);

		Industry aquaculture = getIndustryByTag(market, "aquaculture");

		if (aquaculture != null) {
			aquaculture.getSupply(Commodities.LOBSTER).getQuantity().unmodify(id);
		}
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		tooltip.addPara("%s volturnian lobster production (Aquaculture)",
				10f, Misc.getHighlightColor(),
				"+" + MOD_LOBSTER);
	}
}
