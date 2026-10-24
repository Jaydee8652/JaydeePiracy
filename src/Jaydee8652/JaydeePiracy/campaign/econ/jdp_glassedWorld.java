package Jaydee8652.JaydeePiracy.campaign.econ;

import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedActionScript;
import com.fs.starfarer.D.M;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.updateMarket;


public class jdp_glassedWorld extends BaseMarketConditionPlugin {
	Integer HAZARD = 100;
	Float MAX_INDUSTRIES = 1f;

	public void apply(String id) {
		super.apply(id);
		market.getHazard().modifyFlat(id, (HAZARD / 100f), condition.getName());
		market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, MAX_INDUSTRIES);

		if (!market.hasTag(id)) {
			var script = new jdp_DelayedActionScript(1f, market, id, 20, 15) {
				@Override
				public void action() {
					List<Industry> industries = market.getIndustries();
					Collections.shuffle(industries);
					Industry industry = industries.get(0);

					while (industry.isFunctional()) {
						Collections.shuffle(industries);
						industry = industries.get(0);
					}

					var days = (int) Math.floor(MathUtils.clamp((float) (Math.random() * 16f), 5f, 16f));
					industry.setDisrupted(days);

					updateMarket(market);
					market.removeTag(id);
				}

				@Override
				public Boolean confirmation() {
					return market.hasCondition(id);
				}
			};
			Global.getSector().addScript(script);
		}
	}

	public void unapply(String id) {
		super.unapply(id);
		market.getHazard().unmodify(id);
		market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);

	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		tooltip.addPara("%s hazard rating", 10f, Misc.getHighlightColor(), "+" + HAZARD + "%");
		tooltip.addPara("%s maximum number of industries", 10f, Misc.getHighlightColor(), "+1");
		tooltip.addPara("Approximately once a month an industry will %s be disrupted for a few days", 10f, Misc.getHighlightColor(), "randomly");

	}
}
