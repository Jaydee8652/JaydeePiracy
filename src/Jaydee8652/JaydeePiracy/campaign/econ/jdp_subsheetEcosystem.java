package Jaydee8652.JaydeePiracy.campaign.econ;

import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeePiracy.utils.jdp_Planets;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.fs.starfarer.api.impl.campaign.econ.impl.Farming.AQUA_PLANETS;

public class jdp_subsheetEcosystem extends ResourceDepositsCondition {
	//Exists to allow Fabrique Orbitale to fish.
	static Integer MOD_FOOD = 2;

	static {
		//Just adds a new entry to the base ResourceDepositsCondition
		COMMODITY.put(jdp_Conditions.JDP_SUBSHEETECOSYSTEM, Commodities.FOOD);
		MODIFIER.put(jdp_Conditions.JDP_SUBSHEETECOSYSTEM, -MOD_FOOD);

		//The planet type determines it being Aquaculture over Farming, not the condition.
		AQUA_PLANETS.add(jdp_Planets.JDP_GLASSED);
		AQUA_PLANETS.add(jdp_Planets.JDP_BEGUILING);
		AQUA_PLANETS.add(Planets.FROZEN);
	}

	@Override
	public void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		tooltip.addPara("%s food production (Aquaculture)",
				10f, Misc.getHighlightColor(),
				"-" + MOD_FOOD);
	}
}
