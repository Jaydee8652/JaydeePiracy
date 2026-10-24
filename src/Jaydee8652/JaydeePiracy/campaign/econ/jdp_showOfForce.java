package Jaydee8652.JaydeePiracy.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeePiracy.campaign.econ.industries.jdp_planetkillerLaunchSite.ALPHA_INTERFECTOR_KEY;

public class jdp_showOfForce extends BaseMarketConditionPlugin implements MarketImmigrationModifier {

	public static final float STAB_BONUS = 1f;
	public static final float ACCESS_BONUS = 0.1f;
	public static final float STAB_BONUS_ALPHA = 2f;
	public static final float ACCESS_BONUS_ALPHA = 0.15f;

	@Override
	public void apply(String id) {
		super.apply(id);
		if (isAlpha()) {
			market.getStability().modifyFlat(id, STAB_BONUS_ALPHA, "Show of Force");
			market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS_ALPHA, "Show of Force");
		} else {
			market.getStability().modifyFlat(id, STAB_BONUS, "Show of Force");
			market.getAccessibilityMod().modifyFlat(id, ACCESS_BONUS, "Show of Force");
		}
	}

	@Override
	public void unapply(String id) {
		super.unapply(id);
		market.getStability().unmodify(id);
		market.getAccessibilityMod().unmodifyFlat(id);
		market.removeTransientImmigrationModifier(this);
	}

	@Override
	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.POOR, 10f);
		incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
	}

	protected float getImmigrationBonus() {
		return market.getSize();
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);

		if (isAlpha()) {
			tooltip.addPara("The Alpha-level AI core currently assigned to internal and external propaganda regarding the \"functional\" Planetkiller Launch Site has emboldened the populace of " + market.getName() + ". Officials across the sector eager to avoid causing a diplomatic incident are fast-tracking all shipments to and from " + market.getName() + ". Pragmatic individuals living under other factions are actively pursuing immigration.", 10f);

			tooltip.addPara("%s stability",
					10f, Misc.getHighlightColor(),
					"+" + (int) STAB_BONUS_ALPHA);

			tooltip.addPara("%s accessibility",
					10f, Misc.getHighlightColor(),
					"+" + (int) Math.round(ACCESS_BONUS_ALPHA * 100f) + "%");

			tooltip.addPara("%s population growth (based on colony size)",
					10f, Misc.getHighlightColor(),
					"+" + (int) getImmigrationBonus());
		} else {
			tooltip.addPara("The \"functional\" Planetkiller Launch Site has emboldened the populace of " + market.getName() + ". Officials across the sector eager to avoid causing a diplomatic incident are fast-tracking all shipments to and from " + market.getName() + ". Pragmatic individuals living under other factions are actively pursuing immigration.", 10f);

			tooltip.addPara("%s stability",
					10f, Misc.getHighlightColor(),
					"+" + (int) STAB_BONUS);

			tooltip.addPara("%s accessibility",
					10f, Misc.getHighlightColor(),
					"+" + (int) Math.round(ACCESS_BONUS * 100f) + "%");

			tooltip.addPara("%s population growth (based on colony size)",
					10f, Misc.getHighlightColor(),
					"+" + (int) getImmigrationBonus());
		}
	}

	public Boolean isAlpha() {
		return !Global.CODEX_TOOLTIP_MODE && Global.getSector().getMemoryWithoutUpdate().contains(ALPHA_INTERFECTOR_KEY);
	}

	public String getIconName() {
		if (isAlpha()) return Global.getSettings().getSpriteName("misc", "jdp_showOfForceAlpha");
		return super.getIconName();
	}
}