package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedDepletionScript;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonAdmin;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.addTerraformingDisplay;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_ATMOSPHAGEDISPERSAL;

public class jdp_atmosphagedispersal {
	public static int FOOD_COST = 2;
	public static String NAME = Global.getSettings().getSkillSpec(JDP_ATMOSPHAGEDISPERSAL).getName();

	private static final Logger log = Global.getLogger(JaydeePiracyPlugin.class);
	private static final List<TerraformingProcessData> terraformingProcessMap = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> primaryProcessLines = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> secondaryProcessLines = new ArrayList<>();

	static {
		List<TerraformingProcessData> line;

		List<String> ORGANICS = new ArrayList<>();
		ORGANICS.add(Conditions.ORGANICS_TRACE);
		ORGANICS.add(Conditions.ORGANICS_COMMON);
		ORGANICS.add(Conditions.ORGANICS_ABUNDANT);
		ORGANICS.add(Conditions.ORGANICS_PLENTIFUL);

		List<String> TEMPERATURE_FIRST = new ArrayList<>();
		TEMPERATURE_FIRST.add(Conditions.VERY_COLD);
		TEMPERATURE_FIRST.add(Conditions.COLD);
		TEMPERATURE_FIRST.add(Conditions.VERY_HOT);

		List<String> TEMPERATURE_SECOND = new ArrayList<>();
		TEMPERATURE_SECOND.add(Conditions.VERY_COLD);
		TEMPERATURE_SECOND.add(Conditions.HOT);
		TEMPERATURE_SECOND.add(Conditions.VERY_HOT);

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(null, Conditions.NO_ATMOSPHERE, Conditions.THIN_ATMOSPHERE, null, null, Conditions.EXTREME_WEATHER));
		line.add(new TerraformingProcessData(null, Conditions.THIN_ATMOSPHERE, jdp_Conditions.JDP_DEPLETEDATMOSPHERE));
		line.add(new TerraformingProcessData(null, jdp_Conditions.JDP_DEPLETEDATMOSPHERE, Conditions.DENSE_ATMOSPHERE));
		line.add(new TerraformingProcessData(null, Conditions.DENSE_ATMOSPHERE, Conditions.TOXIC_ATMOSPHERE));
		primaryProcessLines.add(line);

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(3f, Conditions.METEOR_IMPACTS, null));

		line.add(new TerraformingProcessData(1f, Conditions.VERY_COLD, Conditions.COLD));
		line.add(new TerraformingProcessData(3f, Conditions.COLD, null, TEMPERATURE_SECOND, null, null));
		line.add(new TerraformingProcessData(5f, null, Conditions.HOT, TEMPERATURE_FIRST, null, null));
		line.add(new TerraformingProcessData(5f, Conditions.HOT, Conditions.VERY_HOT));
		secondaryProcessLines.add(line);

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(1f, null, null));

		line.add(new TerraformingProcessData(1f, Conditions.ORGANICS_PLENTIFUL, Conditions.ORGANICS_ABUNDANT));
		line.add(new TerraformingProcessData(2f, Conditions.ORGANICS_ABUNDANT, Conditions.ORGANICS_COMMON));
		line.add(new TerraformingProcessData(2f, Conditions.ORGANICS_COMMON, Conditions.ORGANICS_TRACE));
		line.add(new TerraformingProcessData(2f, Conditions.ORGANICS_TRACE, null, ORGANICS, null, null));
		secondaryProcessLines.add(line);

		for(List<TerraformingProcessData> processLine: primaryProcessLines) terraformingProcessMap.addAll(processLine);
		for(List<TerraformingProcessData> processLine: secondaryProcessLines) terraformingProcessMap.addAll(processLine);
	}

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			var population = getIndustryByTag(market, "population");

			if ((population != null) && (population.isFunctional() && market.getPlanetEntity() != null && !market.getPlanetEntity().isGasGiant())) {
				if (!market.hasTag(id) && terraformingProcessMap.stream().anyMatch(p -> p.getWeight() == null && p.isAllowed(market))) Global.getSector().addScript(new jdp_DelayedDepletionScript(0.5f, market, id, 80, 15, terraformingProcessMap));
				population.getDemand(Commodities.FOOD).getQuantity().modifyFlat(id, FOOD_COST);
			} else if ((population != null) && (!population.isFunctional())) {
				unapply(market, id);
			}
		}

		public void unapply(MarketAPI market, String id) {
			var population = getIndustryByTag(market, "population");

			if ((population != null)) {
				if (population.getDemand(Commodities.FOOD) != null) {
					population.getDemand(Commodities.FOOD).getQuantity().unmodify(id);
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
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
			float pad = 3f;
			float opad = 10f;
			Color high = Misc.getHighlightColor();
			Color bad = Misc.getNegativeHighlightColor();
			Color neutral = Misc.getGrayColor();

			info.addPara("%s food demand (Population & Infrastructure)",
					3f, Misc.getHighlightColor(),
					"+" + FOOD_COST);
			info.addSpacer(10f);

			info.addPara("Approximately once every three months, the thickness of the atmosphere will permanently decrease", 3f, Misc.getHighlightColor(), "permanently decrease");
			info.addSpacer(10f);

			info.addPara("Repeated reductions have an increasing chance to cause side effects.", 3f, high, "side effects");

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

				info.addPara("Primary process:", Misc.getBasePlayerColor(),3f);
				info.addSpacer(3f);
				for(List<TerraformingProcessData> line: primaryProcessLines) addTerraformingDisplay(market, line, Misc.getBasePlayerColor(), info, width, 40f, 6f);
				info.addSpacer(10f);

				info.addPara("Side effects (will not always occur):", Misc.getBasePlayerColor(), 3f);
				info.addSpacer(3f);
				for(List<TerraformingProcessData> line: secondaryProcessLines) addTerraformingDisplay(market, line, Misc.getBasePlayerColor(), info, width, 40f, 6f);
				info.addSpacer(3f);
			} else {
				info.addPara("*Press and hold [F1] for a detailed breakdown", 3f, neutral, Misc.getHighlightColor(), "F1");
			}
		}

        public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}