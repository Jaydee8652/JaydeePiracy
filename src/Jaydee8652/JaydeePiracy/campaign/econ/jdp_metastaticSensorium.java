package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class jdp_metastaticSensorium extends BaseMarketConditionPlugin {
	Integer HAZARD = 25;
	Float MAX_INDUSTRIES = -1f;
	Integer BONUS = 5;
	Integer SIZE = 0;
	Integer RANGE = 10;

	public void calculateSize(String id) {
		market.getIncomeMult().unmodify(id);
		SIZE = 0;

		List<MarketAPI> MARKETS = Global.getSector().getEconomy().getMarketsCopy();
		MARKETS.remove(market);

		if (MARKETS.isEmpty()) return;

		for (MarketAPI otherMarket : MARKETS) {
			if (market.getFaction() == null) return;

			if ((Misc.getDistanceLY(market.getLocationInHyperspace(), otherMarket.getLocationInHyperspace()) <= RANGE)
					&& (!otherMarket.getFaction().isHostileTo(market.getFaction()))) {
				SIZE += otherMarket.getSize();
			}
		}
		SIZE = SIZE * BONUS;
		market.getIncomeMult().modifyPercent(id, SIZE, condition.getName().toLowerCase());
	}

	public void apply(String id) {
		super.apply(id);

		market.getHazard().modifyFlat(id, (HAZARD / 100f), condition.getName());
		market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, MAX_INDUSTRIES);
		calculateSize(id);
	}

	public void unapply(String id) {
		super.unapply(id);

		market.getHazard().unmodify(id);
		market.getIncomeMult().unmodify(id);
		market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+" + HAZARD + "%");
		tooltip.addPara("%s maximum number of industries", 10f, Misc.getHighlightColor(), "-1");
		tooltip.addPara("%s market income per unit of allied market size within %s", 10f, Misc.getHighlightColor(), "+" + BONUS + "%", RANGE + "LY");
	}
}
