package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.campaign.econ.jdp_makeshiftSolarArray;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.scripts.intel.admin.SubpopulationTooltipCreator;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_DisplayElement;
import Jaydee8652.JaydeeProficiencies.specialists.tooltips.ConditionTooltipCreator;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedActionScript;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedDepletionScript;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CampaignPlanet;
import com.fs.starfarer.campaign.Faction;
import com.fs.starfarer.campaign.econ.Market;
import com.fs.starfarer.ui.G;
import com.fs.starfarer.ui.d;
import org.apache.log4j.Logger;
import org.lwjgl.input.Keyboard;
import Jaydee8652.JaydeePiracy.campaign.entities.jdp_SolarEntityPlugin.*;


import java.awt.*;
import java.util.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.utils.jdp_Conditions.JDP_MAKESHIFTSOLARARRAY;
import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_SOLAR;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonAdmin;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.updateMarket;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.addTerraformingDisplay;
import static com.fs.starfarer.api.impl.campaign.ids.Conditions.RARE_ORE_SPARSE;

import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.TerraformingProcessData;


public class jdp_solar {
	public static String UNSTABLE = "$jdp_unstable";
	public static String CONTINGENCY = "$jdp_contingency";
	public static String SOLARS = "$jdp_solars";
	public static String MARKET = "$jdp_market";

	public static float DEORBIT_TIME = 2500f;
	public static String NAME = Global.getSettings().getSkillSpec(JDP_SOLAR).getName();

	public static Float INDUSTRY_COST = 0.5f;
	private static final List<TerraformingProcessData> terraformingProcessMap = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> primaryProcessLines = new ArrayList<>();
	static {
		List<String> REQUIRED = new ArrayList<>();
		REQUIRED.add(jdp_Conditions.JDP_WATERICEIMPORT);
		REQUIRED.add(Conditions.HABITABLE);
		REQUIRED.add(JDP_MAKESHIFTSOLARARRAY);

		List<String> FARMLAND = new ArrayList<>();
		FARMLAND.add(Conditions.FARMLAND_ADEQUATE);
		FARMLAND.add(Conditions.FARMLAND_RICH);
		FARMLAND.add(Conditions.FARMLAND_BOUNTIFUL);
		FARMLAND.add(Conditions.WATER_SURFACE);
		FARMLAND.add(jdp_Conditions.JDP_SUBSHEETECOSYSTEM);

		List<TerraformingProcessData> line;

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(1f, null, null));

		line.add(new TerraformingProcessData(null, Conditions.FARMLAND_BOUNTIFUL, Conditions.FARMLAND_RICH, null, REQUIRED, null));
		line.add(new TerraformingProcessData(null, Conditions.FARMLAND_RICH, Conditions.FARMLAND_ADEQUATE, null, REQUIRED, null));
		line.add(new TerraformingProcessData(null, Conditions.FARMLAND_ADEQUATE, Conditions.FARMLAND_POOR, null, REQUIRED, null));
		line.add(new TerraformingProcessData(null, Conditions.FARMLAND_POOR, null, FARMLAND, REQUIRED, null));
		primaryProcessLines.add(line);

		for(List<TerraformingProcessData> processLine: primaryProcessLines) terraformingProcessMap.addAll(processLine);
	}


	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if (market.hasCondition(Conditions.SOLAR_ARRAY)) market.addCondition(JDP_MAKESHIFTSOLARARRAY);
			var heavyindustry = getIndustryByTag(market, Industries.TAG_HEAVYINDUSTRY);

			if ((heavyindustry != null) && (heavyindustry.isFunctional())) {
				if (!market.hasCondition(JDP_MAKESHIFTSOLARARRAY)) {
					heavyindustry.getSupplyBonusFromOther().modifyMult(id, INDUSTRY_COST, NAME);

					if (!market.hasTag(id + "_panels")) {
						var script = new jdp_DelayedActionScript(0f, market, id + "_panels", 110, 15) {
							@Override
							public void action() {
								var fade_in = 1f;
								var solars = new ArrayList<SectorEntityToken>();

								market.removeTag("jdp_solar_impact");
								market.removeTag("jdp_solar_warned");

								var planet = market.getPlanetEntity();
								if (planet == null || planet.getOrbitFocus() == null) return;
								market.addCondition(JDP_MAKESHIFTSOLARARRAY);

								String faction = Factions.NEUTRAL;
								float period = planet.getCircularOrbitPeriod();
								float angle = planet.getCircularOrbitAngle();
								float radius = 270f + planet.getRadius();

								//Mirrors and shades
								boolean shade =
										planet.hasCondition(Conditions.VERY_HOT) ||
												planet.hasCondition(Conditions.HOT) ||
												planet.getStarSystem().getStar().getTypeId().equals(StarTypes.BLUE_GIANT) ||
												planet.getStarSystem().getStar().getTypeId().equals(StarTypes.BLUE_SUPERGIANT);
								boolean mirror =
										planet.hasCondition(Conditions.VERY_COLD) ||
												planet.hasCondition(Conditions.COLD) ||
												planet.hasCondition(Conditions.POOR_LIGHT) ||
												planet.getStarSystem().getStar().getTypeId().equals(StarTypes.RED_DWARF) ||
												planet.getStarSystem().getStar().getTypeId().equals(StarTypes.BROWN_DWARF);

								boolean forceFew = false;
								if (!shade && !mirror) {
									mirror = true;
									shade = true;
									forceFew = true;
								}

								if (mirror) {
									SectorEntityToken mirror1 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Beta", "jdp_makeshift_mirror", faction);
									mirror1.setCircularOrbitPointingDown(planet, angle + 30, radius, period);
									Misc.fadeIn(mirror1, fade_in);
									solars.add(mirror1);

									SectorEntityToken mirror2 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Alpha", "jdp_makeshift_mirror", faction);
									mirror2.setCircularOrbitPointingDown(planet, angle - 30, radius, period);
									Misc.fadeIn(mirror2, fade_in);
									solars.add(mirror2);

									if (!forceFew) {
										SectorEntityToken mirror3 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Gamma", "jdp_makeshift_mirror", faction);
										mirror3.setCircularOrbitPointingDown(planet, angle + 0, radius, period);
										Misc.fadeIn(mirror3, fade_in);
										solars.add(mirror3);
									}
								}

								if (shade) {
									SectorEntityToken shade2 = planet.getStarSystem().addCustomEntity(null, "Stellar Shade Psi", "jdp_makeshift_shade", faction);
									shade2.setCircularOrbitPointingDown(planet, angle + 180 + 0, radius + 25, period);
									Misc.fadeIn(shade2, fade_in);
									solars.add(shade2);
								}

								var added = Global.getSettings().getMarketConditionSpec(JDP_MAKESHIFTSOLARARRAY).getName().replace(" Deposits", "");
								MessageIntel intel = new MessageIntel();

								intel.setIcon(Global.getSettings().getSkillSpec(id.replace("_GO_0_panels", "")).getSpriteName());
								intel.endAfterDelay(1);
								intel.setSound(intel.getCommMessageSound());
								intel.addLine(Global.getSettings().getSkillSpec(id.replace("_GO_0_panels", "")).getName() + " - " + market.getName(), Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
								intel.addLine(BaseIntelPlugin.BULLET + "Added: " + added, Misc.getTextColor(), new String[]{added}, Misc.getHighlightColor());
								Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);

								market.getPrimaryEntity().getMemory().set(SOLARS, solars);
								updateMarket(market);
								market.removeTag(id);
							}

							@Override
							public Boolean confirmation() {
								var heavyindustry = getIndustryByTag(market, Industries.TAG_HEAVYINDUSTRY);

								return ((heavyindustry != null) && (heavyindustry.isFunctional()) && market.getAdmin().getStats().hasSkill(id.replace("_GO_0_panels", "")));
							}
						};
						Global.getSector().addScript(script);
					}
				} else {
					unapply(market, id);
				}
			} else if ((heavyindustry != null) && (!heavyindustry.isFunctional())) {
				unapply(market, id);
			}

			if (!market.hasTag(id) && terraformingProcessMap.stream().anyMatch(p -> p.getWeight() == null && p.isAllowed(market))) Global.getSector().addScript(new jdp_DelayedDepletionScript(0f, market, id, 140, 15, terraformingProcessMap));
		}

		public void unapply(MarketAPI market, String id) {
			var heavyindustry = getIndustryByTag(market, "heavyindustry");

			if ((heavyindustry != null)) heavyindustry.getSupplyBonusFromOther().unmodify(id);
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

			info.addPara("Redirects heavy industry output to construct a Makeshift Solar Array over approximately four months", 3f, Misc.getHighlightColor(), "Orbital Solar Array", "Makeshift Solar Array");
			info.addPara("%s commodity production (Heavy Industry, Orbital Works)",
					3f, high,
					"Halves");
			info.addPara("*Will not occur if an Orbital Solar Array is present", Misc.getGrayColor(), 3f);
			info.addSpacer(10f);

			MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(JDP_MAKESHIFTSOLARARRAY);
			var sprite = Global.getSettings().getSprite(spec.getIcon());
			var height = 40f;
			var imageWidth = (height / sprite.getHeight()) * sprite.getWidth();

			var element = new jdp_DisplayElement(sprite, "", Misc.getBasePlayerColor(), info, imageWidth, height);
			element.setHasBorder(true);

			info.addPara("Makeshift Solar Array", high, 3f).getPosition().rightOfTop(element.getElementPanel(), 6f);;
			jdp_makeshiftSolarArray.addTooltip(info, false, 3f);
			info.setHeightSoFar(info.getHeightSoFar() - height);

			var space = info.addPara("", high, 3f);
			space.getPosition().setXAlignOffset(-(height + 6f));

			info.addPara("Makeshift Solar Arrays require constant maintenance and will begin to violently de-orbit without this skill", 3f, high, "Makeshift Solar Arrays", "violently de-orbit");
			info.addSpacer(10f);

			info.addPara("Affects: governed colony (with Solar Array and Water Ice Imports)", 0f, Misc.getGrayColor(), Misc.getBasePlayerColor(), "governed colony (with Solar Array and Water Ice Imports)");
			info.addSpacer(10f);

			//Turns out the codex doesn't actually know how tall tooltips are in regard to adding related entires. This shuffles the text so that it all fits
			if(!Global.CODEX_TOOLTIP_MODE) {
				info.addPara("Approximately once every five months, the quality of farmland will permanently increase", 3f, Misc.getHighlightColor(),  "permanently increase");
			} else {
				info.addPara("Approximately once every five months, the quality of farmland will permanently", 3f, Misc.getHighlightColor(),  "permanently");
				info.addPara("increase", 3f, Misc.getHighlightColor(),  "increase");
			}

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
			} else {
				info.addPara("*Press and hold [F1] for a detailed breakdown", 3f, neutral, Misc.getHighlightColor(), "F1");
			}
			//If doesn't have solar array, builds makeshift array. Needs maintenance, will break down within a month
			//Removes ship production from Orbital Works to make the array
			//If water is available, steadily increases farmland. Will reduce back down once the arrays are gone?
		}

        public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.CUSTOM;
		}
	}
}