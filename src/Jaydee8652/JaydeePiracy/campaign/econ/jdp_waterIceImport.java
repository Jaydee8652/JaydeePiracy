package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
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

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;


public class jdp_waterIceImport extends BaseMarketConditionPlugin {
	public static int FARMING_BONUS = 1;

	public void apply(String id) {
		Industry farming = getIndustryByTag(market, "farming");
		if (farming != null) {
			farming.getSupplyBonusFromOther().modifyFlat(id, FARMING_BONUS, condition.getName() + " (Accelerator Technician)");
		}
	}

	public void unapply(String id) {
		Industry farming = getIndustryByTag(market, "farming");
		if (farming != null) {
			farming.getSupplyBonusFromOther().unmodifyFlat(id);
		}
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		float opad = 10f;

		tooltip.addPara("Increases food production by %s (Farming).", opad, Misc.getHighlightColor(), "" + FARMING_BONUS);
	}
}

