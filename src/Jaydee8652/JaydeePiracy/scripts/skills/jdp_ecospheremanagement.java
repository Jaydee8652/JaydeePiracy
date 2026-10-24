package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedDepletionScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.*;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_ECOSPHEREMANAGEMENT;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonAdmin;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.addTerraformingDisplay;

public class jdp_ecospheremanagement {
	private static final Logger log = Global.getLogger(JaydeePiracyPlugin.class);
	public static String NAME = Global.getSettings().getSkillSpec(JDP_ECOSPHEREMANAGEMENT).getName();

	public static float HAZARD = 0.85f;
	public static float WATER_BONUS = 0.10f;

	private static final List<TerraformingProcessData> terraformingProcessMap = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> primaryProcessLines = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> secondaryProcessLines = new ArrayList<>();



	static {
		List<TerraformingProcessData> line;

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(null, null, Conditions.EXTREME_WEATHER));
		line.add(new TerraformingProcessData(null, null, Conditions.INIMICAL_BIOSPHERE));
		line.add(new TerraformingProcessData(null, null, Conditions.POLLUTION));
		line.add(new TerraformingProcessData(null, Conditions.TECTONIC_ACTIVITY, Conditions.EXTREME_TECTONIC_ACTIVITY));
		primaryProcessLines.add(line);

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(2f, null, null));
		line.add(new TerraformingProcessData(1f, Conditions.MILD_CLIMATE, null, new ArrayList<>(Collections.singleton(Conditions.EXTREME_WEATHER)), new ArrayList<>(Collections.singleton(Conditions.HABITABLE)), null));
		secondaryProcessLines.add(line);

		for(List<TerraformingProcessData> processLine: primaryProcessLines) terraformingProcessMap.addAll(processLine);
		for(List<TerraformingProcessData> processLine: secondaryProcessLines) terraformingProcessMap.addAll(processLine);
	}

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			var population = getIndustryByTag(market, "population");

			if ((population != null) && (population.isFunctional())) {
				if(market.hasCondition(jdp_Conditions.JDP_WATERICEIMPORT)) {
					market.getHazard().modifyMult(id, HAZARD - WATER_BONUS, NAME);
				} else {
					market.getHazard().modifyMult(id, HAZARD, NAME);
				}

				if (!market.hasTag(id) && terraformingProcessMap.stream().anyMatch(p -> p.getWeight() == null && p.isAllowed(market))) Global.getSector().addScript(new jdp_DelayedDepletionScript(0f, market, id, 110, 15, terraformingProcessMap));

				population.getDemand(Commodities.VOLATILES).getQuantity().modifyFlat(id, market.getSize());
			} else if ((population != null) && (!population.isFunctional())) {
				unapply(market, id);
			}
		}

		public void unapply(MarketAPI market, String id) {
			market.getHazard().unmodify(id);
			var population = getIndustryByTag(market, "population");

			if ((population != null)) {
				if (population.getDemand(Commodities.VOLATILES) != null) {
					population.getDemand(Commodities.VOLATILES).getQuantity().unmodify(id);
				} else {
					log.warn("JDP_DEBUG: " + NAME + " encountered a null food demand on [" + market.getName() + " " + market.getId() + "] in ["  + market.getPrimaryEntity().getContainingLocation().getId() + " " + market.getPrimaryEntity().getContainingLocation().getId() + "]");
					log.warn("JDP_DEBUG: Admin has the following skills [" + market.getAdmin().getStats().getSkillsCopy().toString() + "]");
				}
			}
		}

		private static class tooltipClock {
			protected static boolean open = false;
			protected static int clock = 0;
		}

		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {
			float pad = 3f;
			float opad = 10f;
			Color high = Misc.getHighlightColor();
			Color bad = Misc.getNegativeHighlightColor();
			Color good = Misc.getPositiveHighlightColor();

			Color neutral = Misc.getGrayColor();

			info.addPara("%s volatiles demand per size level (Population & Infrastructure)", 3f, high, "+1");
			info.addPara("%s hazard rating (%s with Water Ice Imports)", 3f, Misc.getHighlightColor(), "x" + HAZARD, "x" + (HAZARD - WATER_BONUS), "Water Ice Imports");
			info.addSpacer(10f);

			info.addPara("Approximately once every four months, the stability of the local environment will permanently increase", 3f, Misc.getHighlightColor(), "permanently increase");
			if(Keyboard.isKeyDown(Keyboard.KEY_F1)) {
				tooltipClock.clock = tooltipClock.clock + 1;
				if (tooltipClock.clock >= 3) tooltipClock.open = true;
			} else {
				if (tooltipClock.clock < 3 && tooltipClock.clock > 0) tooltipClock.open = false;
				tooltipClock.clock = 0;
			}

			if(tooltipClock.open || Global.CODEX_TOOLTIP_MODE) {
				MarketAPI market = null;

				if (!Global.CODEX_TOOLTIP_MODE) {
					info.addPara("*Press [F1] to close detailed breakdown", 3f, neutral, Misc.getHighlightColor(), "F1");
					var admin = getPersonAdmin(stats);
					if (admin != null) market = admin.getMarket();
					if (Global.getSector().getCurrentlyOpenMarket() != null) market = Global.getSector().getCurrentlyOpenMarket();
				}
				info.addSpacer(10f);

				info.addPara("Primary processes:", Misc.getBasePlayerColor(),3f);
				info.addSpacer(3f);
				for(List<TerraformingProcessData> line: primaryProcessLines) addTerraformingDisplay(market, line, Misc.getBasePlayerColor(), info, width, 40f, 6f);
				info.addSpacer(10f);

				info.addPara("Side effect (will not always occur):", Misc.getBasePlayerColor(), 3f);
				info.addSpacer(3f);
				for(List<TerraformingProcessData> line: secondaryProcessLines) addTerraformingDisplay(market, line, Misc.getBasePlayerColor(), info, width, 40f, 6f);
				info.addSpacer(3f);
			} else {
				info.addPara("*Press and hold [F1] for a detailed breakdown of modified conditions", 3f, neutral, Misc.getHighlightColor(), "F1");
			}
		}

        public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.CUSTOM;
		}
	}
}