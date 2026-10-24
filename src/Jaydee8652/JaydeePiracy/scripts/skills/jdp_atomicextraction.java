package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedDepletionScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.*;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_ATOMICEXTRACTION;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonAdmin;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.addTerraformingDisplay;

public class jdp_atomicextraction {
	public static int MINING_BONUS = 2;
	public static String NAME = Global.getSettings().getSkillSpec(JDP_ATOMICEXTRACTION).getName();

	private static final Logger log = Global.getLogger(JaydeePiracyPlugin.class);
	private static final List<TerraformingProcessData> terraformingProcessMap = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> primaryProcessLines = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> secondaryProcessLines = new ArrayList<>();

	static {
		List<String> TECTONIC = new ArrayList<>();
		TECTONIC.add(Conditions.EXTREME_TECTONIC_ACTIVITY);

		List<TerraformingProcessData> line;

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(null, null, Conditions.ORGANICS_TRACE));
		line.add(new TerraformingProcessData(null, Conditions.ORGANICS_TRACE, Conditions.ORGANICS_COMMON));
		line.add(new TerraformingProcessData(null, Conditions.ORGANICS_COMMON, Conditions.ORGANICS_ABUNDANT));
		line.add(new TerraformingProcessData(null, Conditions.ORGANICS_ABUNDANT, Conditions.ORGANICS_PLENTIFUL));

		line.add(new TerraformingProcessData(null, null, Conditions.ORE_SPARSE));
		line.add(new TerraformingProcessData(null, Conditions.ORE_SPARSE, Conditions.ORE_MODERATE));
		line.add(new TerraformingProcessData(null, Conditions.ORE_MODERATE, Conditions.ORE_ABUNDANT));
		line.add(new TerraformingProcessData(null, Conditions.ORE_ABUNDANT, Conditions.ORE_RICH));
		line.add(new TerraformingProcessData(null, Conditions.ORE_RICH, Conditions.ORE_ULTRARICH));
		primaryProcessLines.add(line);

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(null, null, Conditions.VOLATILES_TRACE));
		line.add(new TerraformingProcessData(null, Conditions.VOLATILES_TRACE, Conditions.VOLATILES_DIFFUSE));
		line.add(new TerraformingProcessData(null, Conditions.VOLATILES_DIFFUSE, Conditions.VOLATILES_ABUNDANT));
		line.add(new TerraformingProcessData(null, Conditions.VOLATILES_ABUNDANT, Conditions.VOLATILES_PLENTIFUL));

		line.add(new TerraformingProcessData(null, null, Conditions.RARE_ORE_SPARSE));
		line.add(new TerraformingProcessData(null, Conditions.RARE_ORE_SPARSE, Conditions.RARE_ORE_MODERATE));
		line.add(new TerraformingProcessData(null, Conditions.RARE_ORE_MODERATE, Conditions.RARE_ORE_ABUNDANT));
		line.add(new TerraformingProcessData(null, Conditions.RARE_ORE_ABUNDANT, Conditions.RARE_ORE_RICH));
		line.add(new TerraformingProcessData(null, Conditions.RARE_ORE_RICH, Conditions.RARE_ORE_ULTRARICH));
		primaryProcessLines.add(line);

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(20f, null, null));

		line.add(new TerraformingProcessData(5f, Conditions.POLLUTION, null, null, new ArrayList<>(Collections.singleton(Conditions.HABITABLE)), null));
		line.add(new TerraformingProcessData(1f, jdp_Conditions.JDP_QUARRYLAKES, null, null, new ArrayList<>(Collections.singleton(Conditions.HABITABLE)), null));

		line.add(new TerraformingProcessData(1f, Conditions.EXTREME_TECTONIC_ACTIVITY, Conditions.TECTONIC_ACTIVITY));
		line.add(new TerraformingProcessData(3f, Conditions.TECTONIC_ACTIVITY, null, TECTONIC, null, null));
		secondaryProcessLines.add(line);

		for(List<TerraformingProcessData> processLine: primaryProcessLines) terraformingProcessMap.addAll(processLine);
		for(List<TerraformingProcessData> processLine: secondaryProcessLines) terraformingProcessMap.addAll(processLine);
	}

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {

			Industry mining = getIndustryByTag(market, "mining");

			if ((mining != null) && (mining.isFunctional() && market.getPlanetEntity() != null && !market.getPlanetEntity().isGasGiant())) {
				if (!market.hasTag(id) && terraformingProcessMap.stream().anyMatch(p -> p.getWeight() == null && p.isAllowed(market))) Global.getSector().addScript(new jdp_DelayedDepletionScript(0.85f, market, id, 140, 15, terraformingProcessMap));

				mining.getDemand(Commodities.FUEL).getQuantity().modifyFlat(id, market.getSize());
				mining.getSupply(Commodities.VOLATILES).getQuantity().modifyMult(id, MINING_BONUS, NAME);
				mining.getSupply(Commodities.ORGANICS).getQuantity().modifyMult(id, MINING_BONUS, NAME);
				mining.getSupply(Commodities.RARE_ORE).getQuantity().modifyMult(id, MINING_BONUS, NAME);
				mining.getSupply(Commodities.ORE).getQuantity().modifyMult(id, MINING_BONUS, NAME);
			} else if ((mining != null) && (!mining.isFunctional())) {
				unapply(market, id);
			}
		}

		public void unapply(MarketAPI market, String id) {
			Industry mining = getIndustryByTag(market, "mining");

			if ((mining != null)) {
				if (mining.getDemand(Commodities.FUEL) != null) {
					mining.getDemand(Commodities.FUEL).getQuantity().unmodify(id);
				} else {
					log.warn("JDP_DEBUG: " + NAME + " encountered a null fuel demand on [" + market.getName() + " " + market.getId() + "] in ["  + market.getPrimaryEntity().getContainingLocation().getId() + " " + market.getPrimaryEntity().getContainingLocation().getId() + "]");
					log.warn("JDP_DEBUG: Admin has the following skills [" + market.getAdmin().getStats().getSkillsCopy().toString() + "]");
				}

				mining.getSupplyBonus().unmodify(id);
				mining.getSupply(Commodities.VOLATILES).getQuantity().unmodify(id);
				mining.getSupply(Commodities.ORGANICS).getQuantity().unmodify(id);
				mining.getSupply(Commodities.RARE_ORE).getQuantity().unmodify(id);
				mining.getSupply(Commodities.ORE).getQuantity().unmodify(id);
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

			info.addPara("%s produced units of ore, rare ore, organics and volatiles (Mining)",
					3f, high,
					"Doubles");
			info.addPara("%s fuel demand per size level (Mining)",
					3f, high,
					"+1");
			info.addSpacer(10f);

			info.addPara("Approximately once every five months, the size of a random mining deposit will permanently decrease", 3f, high, "permanently decrease");
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
					if(admin != null) market = admin.getMarket();
				}
				info.addSpacer(10f);

				info.addPara("Primary processes:", Misc.getBasePlayerColor(),3f);
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
			return ScopeDescription.CUSTOM;
		}
	}
}