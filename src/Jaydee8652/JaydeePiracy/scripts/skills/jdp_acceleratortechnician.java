package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeePiracy.utils.jdp_Planets;
import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_DisplayElement;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedActionScript;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.IconRenderMode;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.specs.PlanetSpec;
import kotlin.text.UStringsKt;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_ACCELERATORTECHNICIAN;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonAdmin;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.updateMarket;

public class jdp_acceleratortechnician {
	private static final Logger log = Global.getLogger(JaydeePiracyPlugin.class);
	public static float ACCESS = 0.25f;
	public static float HAZARD = 0.15f;

	public static int FARMING_BONUS = 1;

	public static String NAME = Global.getSettings().getSkillSpec(JDP_ACCELERATORTECHNICIAN).getName();

	public static Set<String> JDP_WATER = new HashSet<String>();
	static {
		JDP_WATER.add(jdp_Planets.JDP_GLASSED);
		JDP_WATER.add(Planets.FROZEN);
		JDP_WATER.add(Planets.CRYOVOLCANIC);
		JDP_WATER.add(Planets.ROCKY_ICE);
		JDP_WATER.add(Planets.ICE_GIANT);
	}

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if (market.getId().equals("fake_Colonize")) return;
			var spaceport = getIndustryByTag(market, "spaceport");

			if (spaceport != null && spaceport.isFunctional()) {
				market.getAccessibilityMod().modifyFlat(id, ACCESS, NAME);
				market.getHazard().modifyFlat(id, HAZARD, NAME);
				spaceport.getDemand(Commodities.FUEL).getQuantity().modifyMult(id, 0f);
				List<MarketAPI> MARKETS = Global.getSector().getEconomy().getMarketsCopy();
				MARKETS.remove(market);
				MARKETS = MARKETS.stream().filter(m -> m.getStarSystem() == market.getStarSystem()).toList();

				for (MarketAPI otherMarket : MARKETS) {
					if (market.getFaction() == null) return;
					otherMarket.getHazard().modifyFlat(id, HAZARD, NAME + " (" + market.getName() + ")");

					if (market.getPlanetEntity() != null && JDP_WATER.contains(market.getPlanetEntity().getTypeId()) && !otherMarket.getFaction().isHostileTo(market.getFaction()) && !otherMarket.hasCondition(jdp_Conditions.JDP_WATERICEIMPORT)) {
						otherMarket.addCondition(jdp_Conditions.JDP_WATERICEIMPORT);
						updateMarket(otherMarket);
					}
				}

				var script = new DelayedActionScript(5f) {
					@Override
					public void doAction() {
						market.removeTag(id);
						if (market.getAdmin().getStats().hasSkill(id.replace("_GO_0", ""))) return;

						List<MarketAPI> MARKETS = Global.getSector().getEconomy().getMarketsCopy();
						MARKETS.remove(market);
						MARKETS = MARKETS.stream().filter(m -> m.getStarSystem() == market.getStarSystem()).toList();

						for (MarketAPI otherMarket : MARKETS) {
							otherMarket.removeCondition(jdp_Conditions.JDP_WATERICEIMPORT);
							clear(otherMarket, id);
							updateMarket(otherMarket);
						}
					}
				};
				if(!market.hasTag(id)) {
					market.addTag(id);
					Global.getSector().addScript(script);
				}
			} else if (spaceport != null && (!spaceport.isFunctional())) {
				unapply(market, id);
			}
		}

		public void unapply(MarketAPI market, String id) {
			market.getAccessibilityMod().unmodifyFlat(id);
			market.getHazard().unmodifyFlat(id);

			var spaceport = getIndustryByTag(market, "spaceport");
			if ((spaceport != null)) spaceport.getDemand(Commodities.FUEL).getQuantity().unmodify(id);
			clear(market, id);
		}

		public void clear(MarketAPI market, String id) {
			market.getHazard().unmodifyFlat(id);
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

			info.addPara("%s fuel demand (Spaceport, Megaport)", 3f, Misc.getHighlightColor(), "Nullfies");
			info.addPara("%s accessibility", 3f, Misc.getHighlightColor(), "+" + Math.round(ACCESS * 100) + "%");
			info.addSpacer(10f);
			info.addPara("Provides water ice to allied markets in the same system (if available on planet type)", 3f, high, "water ice");
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
					info.addPara("*Press [F1] to close list of water ice bearing planet types", 3f, neutral, Misc.getHighlightColor(), "F1");
					var admin = getPersonAdmin(stats);
					if (admin != null) market = admin.getMarket();
					if (Global.getSector().getCurrentlyOpenMarket() != null) market = Global.getSector().getCurrentlyOpenMarket();
				} else {
					info.addPara("*List of water ice bearing planet types", 3f, neutral, Misc.getHighlightColor(), "F1");
				}
				info.addSpacer(10f);
				CustomPanelAPI previous = null;
				var displays = new ArrayList<jdp_DisplayElement>();

				var gap = 3f;
				var imageWidth = 150f;
				var perLine = Math.floor(width / (imageWidth + gap));

				for (String planetType : JDP_WATER) {
					PlanetSpecAPI planetSpec = (PlanetSpecAPI) Global.getSettings().getSpec(PlanetSpec.class, planetType, true);
					if (planetSpec != null) {
						var sprite = Global.getSettings().getSprite(planetSpec.getTexture());
						var imageHeight = (imageWidth / sprite.getWidth()) * sprite.getHeight();
						var color = Misc.getBasePlayerColor();
						if(market != null && market.getPlanetEntity() != null && market.getPlanetEntity().getTypeId().equals(planetType)) color = Misc.getHighlightColor();

						var displayElement = new jdp_DisplayElement(sprite, planetSpec.getName(), color, info, imageWidth, imageHeight);
						displays.add(displayElement);

						if ((displays.size() - 1) != 0 && (displays.size() - 1) % perLine == 0) {
							previous = displays.get((int) (displays.size() - (perLine + 1))).getElementPanel();
							displayElement.getPosition().belowLeft(previous, gap);
						} else if (previous != null) {
							displayElement.getPosition().rightOfTop(previous, gap);
							info.setHeightSoFar(info.getHeightSoFar() - imageHeight);
						}
						previous = displayElement.getElementPanel();
					}
				}
				int position = 0;
				while (position + perLine < displays.size()) position = (int) (position + perLine);
				info.addSpacer(0f).getPosition().belowLeft(displays.get(position).getElementPanel(), 0f);
			} else {
				info.addPara("*Press and hold [F1] for a list of water ice bearing planet types", 3f, neutral, Misc.getHighlightColor(), "F1");
			}
			info.addSpacer(10f);

			MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(jdp_Conditions.JDP_WATERICEIMPORT);
			var sprite = Global.getSettings().getSprite(spec.getIcon());
			var height = 40f;
			var imageWidth = (height / sprite.getHeight()) * sprite.getWidth();

			var element = new jdp_DisplayElement(sprite, "", Misc.getBasePlayerColor(), info, imageWidth, height);
			element.setHasBorder(true);
			info.addPara("Water Ice Imports", high, 3f).getPosition().rightOfTop(element.getElementPanel(), 6f);
			info.addPara("Planets receiving water ice gain +" + FARMING_BONUS + " food production (Farming)", 3f, Misc.getHighlightColor(), "water ice", "+" + FARMING_BONUS);
			info.setHeightSoFar(info.getHeightSoFar() - height);

			info.addSpacer(10f);
			var space = info.addPara("", high, 3f);
			space.getPosition().setXAlignOffset(-(height + 6f));

			info.addPara("Affects: all colonies in system", 0f, Misc.getGrayColor(), Misc.getBasePlayerColor(), "all colonies in system");
			info.addSpacer(10f);
			info.addPara("%s hazard rating", 3f, Misc.getHighlightColor(), "+" + Math.round(HAZARD * 100) + "%");
		}

        public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.CUSTOM;
		}
	}
}