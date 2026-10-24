package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.impl.FuelProduction;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;
import static com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo.SYNCHROTRON_FUEL_BONUS;

public class jdp_domainMeshugatron extends BaseMarketConditionPlugin {
	public static int BURN_BONUS = 2;
	public static int DEMAND = 3;

	public void apply(String id) {
		super.apply(id);

		Industry fuelproduction = getIndustryByTag(market,"fuelprod");

		if ((fuelproduction != null) && (fuelproduction.isFunctional())) {
			applyBurnBonus();
			fuelproduction.getSupplyBonus().modifyFlat(id, SYNCHROTRON_FUEL_BONUS);
			fuelproduction.getDemand(Commodities.VOLATILES).getQuantity().modifyFlat(id, DEMAND);
		} else if ((fuelproduction != null) && (!fuelproduction.isFunctional())) {
			fuelproduction.getDemand(Commodities.VOLATILES).getQuantity().unmodify(id);
			fuelproduction.getSupplyBonus().unmodify(id);
		}
	}

	public void unapply(String id) {
		super.unapply(id);

		Industry fuelproduction = getIndustryByTag(market, "fuelprod");

		if (fuelproduction != null) {
			fuelproduction.getDemand(Commodities.VOLATILES).getQuantity().unmodify(id);
			fuelproduction.getSupplyBonus().unmodify(id);
		}
	}

	public void applyBurnBonus () {
		for (CampaignFleetAPI fleet : Global.getSector().getHyperspace().getFleets()) {
			if (fleet.isInHyperspaceTransition()) continue;

			if (!fleet.getFaction().isHostileTo(market.getFaction())) {
				String desc = "High Grade Fuel";

				MutableStat.StatMod curr = fleet.getStats().getFleetwideMaxBurnMod().getFlatBonus("jdp_supplyStation");
				if (curr == null || curr.value <= BURN_BONUS) {
					fleet.getStats().addTemporaryModFlat(0.1f, "jdp_supplyStation",
							desc, BURN_BONUS,
							fleet.getStats().getFleetwideMaxBurnMod());
				}
			}
		}
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);
		tooltip.addPara("%s volatiles demand (Fuel Production)",
				10f, Misc.getHighlightColor(),
				"+" + DEMAND);
		tooltip.addPara("%s maximum burn for all allied fleets in Hyperspace (Fuel Production)", 10f, Misc.getHighlightColor(), "+" + BURN_BONUS);
	}
}
