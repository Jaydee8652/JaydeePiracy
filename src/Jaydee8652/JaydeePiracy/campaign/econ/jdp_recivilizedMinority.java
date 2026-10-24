package Jaydee8652.JaydeePiracy.campaign.econ;

import Jaydee8652.JaydeePiracy.scripts.intel.SubpopulationMovementIntel;
import Jaydee8652.JaydeePiracy.scripts.intel.admin.SubpopulationTooltipCreator;

import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_DisplayElement;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.ConditionData;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateIntel;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.rpg.Person;
import kotlin.Unit;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.capitaliseFirst;

import static Jaydee8652.JaydeePiracy.scripts.jdp_IndustryPlacer.MAIN_INDUSTRY;
import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_RECIV;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_ConditionUtils.RUINS;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_ConditionUtils.hasConditions;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.*;
import static com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition.*;
import static com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator.getTravelDays;

public class jdp_recivilizedMinority extends BaseMarketConditionPlugin implements MarketImmigrationModifier {
	public static Set<String> EFFECT_TYPES = new HashSet<String>();
	static {
		EFFECT_TYPES.add(Industries.MINING);
		EFFECT_TYPES.add(Industries.REFINING);
		EFFECT_TYPES.add(Industries.HEAVYINDUSTRY);
		EFFECT_TYPES.add(Industries.LIGHTINDUSTRY);
		EFFECT_TYPES.add(Industries.FARMING);
		EFFECT_TYPES.add(Industries.AQUACULTURE);
		EFFECT_TYPES.add(Industries.FUELPROD);
		EFFECT_TYPES.add(Industries.MILITARYBASE);
		EFFECT_TYPES.add(Industries.COMMERCE);
		EFFECT_TYPES.add(Industries.TECHMINING);
	}

	static String EFFECT_KEY = "$jdp_recivilisedEffect";

	static float TRANSIT_DAYS_BASE = 15;

	static Float MOD = 1f;
	public static int STABILITY_PENALTY = 1;

	static Float MOD_HIGH = 2f;
	static Float UPKEEP = 0.5f;

	static Float QUALITY_BONUS = 0.15f;
	static Float SIZE_BONUS = 0.3f;
	static Float DEFENSE_BONUS = 0.2f;
	static Float INCOME_BONUS = 0.2f;
	static Float TECH_BONUS = 0.5f;

	public static class SubpopulationEffectData {
		private final MarketAPI originMarket;
		private final String type;
		private final MarketConditionAPI condition;

		public SubpopulationEffectData(MarketAPI originMarket, String type, MarketConditionAPI condition) {
            this.originMarket = originMarket;
			this.type = type;
			this.condition = condition;
		}

		public void apply(MarketAPI market, String id) {
			switch (type) {
				case Industries.HEAVYINDUSTRY: {
					var industry = getIndustryByTag(market, type);
					if (industry != null && industry.isFunctional()) {
						industry.getUpkeep().modifyMult(id, UPKEEP, condition.getName());
						industry.getSupplyBonusFromOther().modifyFlat(id, MOD, condition.getName());
						market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, QUALITY_BONUS, condition.getName());
					} else {
						unapply(market, id);
					}
					break;
				}
				case Industries.MILITARYBASE: {
					Industry industry = getIndustryByTag(market, MILITARY_TAGS, Industries.TAG_PATROL);
					if (industry != null && industry.isFunctional()) {
						industry.getUpkeep().modifyMult(id, UPKEEP, condition.getName());
						market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, 1f + DEFENSE_BONUS, condition.getName());
						market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat(id, SIZE_BONUS, condition.getName());
					} else {
						unapply(market, id);
					}
					break;
				}
				case Industries.COMMERCE: {
					var industry = getIndustryByTag(market, type);
					if (industry != null && industry.isFunctional()) {
						industry.getUpkeep().modifyMult(id, UPKEEP, condition.getName());
						market.getIncomeMult().modifyMult(id, 1f + INCOME_BONUS,condition.getName());
					} else {
						unapply(market, id);
					}
					break;
				}
				case Industries.TECHMINING: {
					var industry = getIndustryByTag(market, type);
					if (industry != null && industry.isFunctional()) {
						industry.getUpkeep().modifyMult(id, UPKEEP, condition.getName());
						market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).modifyMult(id, 1f + TECH_BONUS, condition.getName());
					} else {
						unapply(market, id);
					}
					break;
				}
				default: {
					var industry = getIndustryByTag(market, type);
					if (industry != null && industry.isFunctional()) {
						industry.getUpkeep().modifyMult(id, UPKEEP, condition.getName());
						industry.getSupplyBonusFromOther().modifyFlat(id, MOD_HIGH, condition.getName());
					} else {
						unapply(market, id);
					}
					break;
				}
			}
		}

		public void unapply(MarketAPI market, String id) {
			switch (type) {
				case Industries.HEAVYINDUSTRY: {
					var industry = getIndustryByTag(market, type);
					if (industry != null) {
						industry.getUpkeep().unmodify(id);
						industry.getSupplyBonusFromOther().unmodify(id);
					}
					market.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(id);
					break;
				}
				case Industries.MILITARYBASE: {
					Industry industry = getIndustryByTag(market, MILITARY_TAGS, Industries.TAG_PATROL);
					market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);

					if (industry != null) {
						industry.getUpkeep().unmodify(id);
					}
					market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodify(id);
					break;
				}
				case Industries.COMMERCE: {
					var industry = getIndustryByTag(market, type);
					if (industry != null) {
						industry.getUpkeep().unmodify(id);
						industry.getSupplyBonusFromOther().unmodify(id);
					}
					market.getIncomeMult().unmodify(id);
					break;
				}
				case Industries.TECHMINING: {
					var industry = getIndustryByTag(market, type);
					if (industry != null) {
						industry.getUpkeep().unmodify(id);
						industry.getSupplyBonusFromOther().unmodify(id);
					}
					market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).unmodify(id);
					break;
				}
				default: {
					var industry = getIndustryByTag(market, type);
					if (industry != null) {
						industry.getUpkeep().unmodify(id);
						industry.getSupplyBonusFromOther().unmodify(id);
					}
					break;
				}
			}
		}

		public String getType() {
			return type;
		}
		public MarketAPI getOriginMarket() {
			return originMarket;
		}
		public String getOriginName() {
			return originMarket.getName();
		}
	}

	public static void appendDescription(TooltipMakerAPI tooltip, MarketAPI originMarket, String type) {
		String name = originMarket.getName();
		String Name = capitaliseFirst(name);

		var opad = 10f;

		if(type == null) return;
		switch (type) {
			case Industries.MINING: {
				tooltip.addPara(Name + " was once a hub of raw resource extraction, the disposition for the work runs as deep as the resources themselves.", opad);
				tooltip.addPara("%s production " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(MOD_HIGH));

				break;
			}
			case Industries.REFINING: {
				tooltip.addPara(Name + " was once an industrious link in the chain of sector wide manufacture, its people refined alongside the metal they worked.", opad);
				tooltip.addPara("%s production " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(MOD_HIGH));

				break;
			}
			case Industries.HEAVYINDUSTRY: {
				tooltip.addPara("The gleaming hulls of " + name + " were once renowned across the sector, and its people remain eager to ply the trade of shipbuilding.", opad);
				tooltip.addPara("Increases ship and weapon production quality by %s ",opad, Misc.getHighlightColor(), Math.round(QUALITY_BONUS * 100f) + "%");
				tooltip.addPara("%s production " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(MOD));
				break;
			}
			case Industries.LIGHTINDUSTRY: {
				tooltip.addPara("The consumer products of " + name + " were once the benchmark of quality, legal or otherwise, and its people retain pride in their handiwork.", opad);
				tooltip.addPara("%s production " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(MOD_HIGH));

				break;
			}
			case Industries.FARMING: {
				tooltip.addPara("Agrarian and humble, the people of " + name + " are said to have made bounty from ash after the fall of the old authority.", opad);
				tooltip.addPara("%s production " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(MOD_HIGH));

				break;
			}
			case Industries.AQUACULTURE: {
				tooltip.addPara("The people of " + name + " are as natural to the seas as spacers are to the vacuum, gifted in dredging abundance from the crushing depths.", opad);
				tooltip.addPara("%s production " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(MOD_HIGH));

				break;
			}
			case Industries.FUELPROD: {
				tooltip.addPara(Name + " was once a heart of antimatter fuel production, its people shaped by traditions and shibboleths that enforce best practices.", opad);
				tooltip.addPara("%s production " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(MOD_HIGH));

				break;
			}
			case Industries.MILITARYBASE: {
				tooltip.addPara("The people of " + name + " are a proud martial culture, a sword unbroken by the collapse of the old authority.", opad);
				tooltip.addPara("Increases size of fleets launched by colony by %s " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getHighlightColor(), Math.round(SIZE_BONUS * 100f) + "%");
				tooltip.addPara("Increases strength of ground defenses by %s " + addBrackets("with " + INDUSTRY_NAMES.get(type)), opad, Misc.getHighlightColor(), "x" + (DEFENSE_BONUS + 1));
				break;
			}
			case Industries.COMMERCE: {
				tooltip.addPara(Name + " was once a hub of trade and influence, its people possessed of silver-tongues that now speak anothers words.", opad);
				tooltip.addPara("Increases colony income by %s " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getHighlightColor(), Math.round(INCOME_BONUS * 100) + "%");

				break;
			}
			case Industries.TECHMINING: {
				tooltip.addPara("The collapse of central authority on " + name + " honed the survivors into ruthless and efficient scavengers, deftly picking their own graves.", opad);
				tooltip.addPara("%s finds from " + INDUSTRY_NAMES.get(type), opad, Misc.getTextColor(), Misc.getHighlightColor(),"+" + Math.round(TECH_BONUS * 100) + "%");

				break;
			}
			default:
		}
		tooltip.addPara("%s credit upkeep " + addBrackets(INDUSTRY_NAMES.get(type)), opad, Misc.getTextColor(), Misc.getHighlightColor(), "x" + UPKEEP);
	}

	public String pickEffect(MarketAPI market, MarketConditionAPI condition) {
		if(MAIN_INDUSTRY.containsKey(market.getId())) {
			var type = MAIN_INDUSTRY.get(market.getId());
			MAIN_INDUSTRY.remove(market.getId());
			return type;
		}

		var previousSize = 1f;
		if(market.hasCondition(Conditions.RUINS_WIDESPREAD)) previousSize = 2f;
		if(market.hasCondition(Conditions.RUINS_EXTENSIVE)) previousSize = 3f;
		if(market.hasCondition(Conditions.RUINS_VAST)) previousSize = 4f;

		var atmosphereMult = 1f; if(market.hasCondition(Conditions.NO_ATMOSPHERE)) atmosphereMult = 3f;
		var habitableMult = 1f; if(market.hasCondition(Conditions.HABITABLE)) habitableMult = 3f;

		WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(new Random());

		picker.add(Industries.COMMERCE, (previousSize + habitableMult) / 10f);
		if(hasConditions(market, RUINS)) picker.add(Industries.TECHMINING, (previousSize) / 10f);

		for (MarketConditionAPI marketCondition : market.getConditions()) {
			String marketConditionId = marketCondition.getId();

			if (COMMODITY.containsKey(marketConditionId)) {
				switch (COMMODITY.get(marketConditionId)) {
					case Commodities.ORE: {
						picker.add(Industries.MINING, (MODIFIER.get(marketConditionId) + 2f) / (3f * habitableMult));
						picker.add(Industries.REFINING, (MODIFIER.get(marketConditionId) + 1f) * atmosphereMult);
						picker.add(Industries.HEAVYINDUSTRY, (MODIFIER.get(marketConditionId) + previousSize) / 2f);

						break;
					}
					case Commodities.RARE_ORE: {
						picker.add(Industries.MINING, (MODIFIER.get(marketConditionId) + 3f) / (3f * habitableMult));
						picker.add(Industries.REFINING, (MODIFIER.get(marketConditionId) + 1f) * atmosphereMult);
						picker.add(Industries.HEAVYINDUSTRY, (MODIFIER.get(marketConditionId) + previousSize) / 2f);

						break;
					}
					case Commodities.ORGANICS: {
						picker.add(Industries.MINING, (MODIFIER.get(marketConditionId) + 2f) / (3f * habitableMult));
						picker.add(Industries.LIGHTINDUSTRY, (MODIFIER.get(marketConditionId) + previousSize) / 2f);

						break;
					}
					case Commodities.VOLATILES: {
						picker.add(Industries.MINING, (MODIFIER.get(marketConditionId) + 3f) / (3f * habitableMult));
						picker.add(Industries.FUELPROD, ((MODIFIER.get(marketConditionId) + previousSize) * atmosphereMult) / 2f);

						break;
					}
					case Commodities.FOOD: {
						if (!marketCondition.getId().equals(Conditions.WATER_SURFACE)) {
							if (market.hasCondition(Conditions.SOLAR_ARRAY)) return Industries.FARMING;
							picker.add(Industries.FARMING, (MODIFIER.get(marketConditionId) + 2f) / 2f);
						} else {
							if (market.hasCondition(Conditions.SOLAR_ARRAY)) return Industries.AQUACULTURE;
							picker.add(Industries.AQUACULTURE, (MODIFIER.get(marketConditionId) + 3f));
						}

						break;
					}
					case Commodities.LOBSTER: {
						return Industries.AQUACULTURE;
					}
				}
			} else {
				switch (marketConditionId) {
					case Conditions.HOT: {
						picker.add(Industries.MILITARYBASE, previousSize / 2f);
						break;
					}
					case Conditions.VERY_HOT: {
						picker.add(Industries.MILITARYBASE, previousSize);
						break;
					}
					default:
				}
			}
		}
		return picker.pick();
	}

	@Override
	public void init(MarketAPI market, MarketConditionAPI condition) {
		this.market = market;
		this.condition = condition;

		var memory = market.getMemory();
		if(!memory.contains(EFFECT_KEY) && !Global.CODEX_TOOLTIP_MODE && !memory.isEmpty()) memory.set(EFFECT_KEY, new SubpopulationEffectData(market, pickEffect(market, condition), condition));
	}

	public void apply(String id) {
		super.apply(id);

		if(!market.getMemory().contains(EFFECT_KEY)) return;
		SubpopulationEffectData data = (SubpopulationEffectData) market.getMemory().get(EFFECT_KEY);
		data.apply(market, id);

		market.addTransientImmigrationModifier(this);
	}

	public void unapply(String id) {
		super.unapply(id);

		if(!market.getMemory().contains(EFFECT_KEY)) return;
		SubpopulationEffectData data = (SubpopulationEffectData) market.getMemory().get(EFFECT_KEY);
		data.unapply(market, id);

		market.removeTransientImmigrationModifier(this);
	}

	public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
		incoming.add(Factions.POOR, 10f);
		incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
	}

	protected float getImmigrationBonus() {
		return market.getSize();
	}

	protected boolean atPlayerColony() {
		var dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
		return (!Global.CODEX_TOOLTIP_MODE && dialog != null && dialog.getInteractionTarget() != null && dialog.getInteractionTarget().getMarket() != null && dialog.getInteractionTarget().getMarket().getFaction().isPlayerFaction());
	}

	public static int getTravelDaysWithDelay(SectorEntityToken destination, SectorEntityToken origin) {
		float transitDays = TRANSIT_DAYS_BASE + getTravelDays(destination, origin);
		return Math.round(transitDays);
	}

	protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
		super.createTooltipAfterDescription(tooltip, expanded);

		String name = market.getName();
		if (Global.CODEX_TOOLTIP_MODE) {
			tooltip.addSpacer(10f);
			tooltip.addPara("Gauss rifle, schoolhouse and charter have forcefully reintegrated " + name + "'s indigenous population, scattered into subcultures by the collapse of the previous governing authority.", 3f);
			tooltip.addPara("%s population growth (based on colony size)", 10f, Misc.getHighlightColor(), "+" + (int) getImmigrationBonus());
			tooltip.addSpacer(10f);
			tooltip.addPara("Hover icons to view the variable bonus for each industry.", Misc.getGrayColor(), 3f);
			tooltip.addSpacer(10f);

			CustomPanelAPI previous = null;
			var displays = new ArrayList<jdp_DisplayElement>();

			var gap = 3f;
			var imageWidth = 150f;
			var perLine = 4f;

			for (String type : EFFECT_TYPES) {
				IndustrySpecAPI industrySpec = Global.getSettings().getIndustrySpec(type);
				if (industrySpec != null) {
					var sprite = Global.getSettings().getSprite(industrySpec.getImageName());
					var imageHeight = (imageWidth / sprite.getWidth()) * sprite.getHeight();
					var color = Misc.getBasePlayerColor();

					var displayElement = new jdp_DisplayElement(sprite, industrySpec.getName(), color, tooltip, imageWidth, imageHeight);
					tooltip.addTooltipTo(new SubpopulationTooltipCreator(industrySpec.getId(), market, false), displayElement.getElementPanel(), TooltipMakerAPI.TooltipLocation.BELOW);

					displays.add(displayElement);
					if ((displays.size() - 1) != 0 && (displays.size() - 1) % perLine == 0) {
						previous = displays.get((int) (displays.size() - (perLine + 1))).getElementPanel();
						displayElement.getPosition().belowLeft(previous, gap);
					} else if (previous != null) {
						displayElement.getPosition().rightOfTop(previous, gap);
						tooltip.setHeightSoFar(tooltip.getHeightSoFar() - imageHeight);
					}
					previous = displayElement.getElementPanel();
				}
			}
			int position = 0;
			while (position + perLine < displays.size()) position = (int) (position + perLine);
			tooltip.addSpacer(0f).getPosition().belowLeft(displays.get(position).getElementPanel(), 0f);
		} else if (market.getMemory().contains(EFFECT_KEY)) {
			SubpopulationEffectData data = (SubpopulationEffectData) market.getMemory().get(EFFECT_KEY);
			tooltip.addSpacer(10f);

			if (data.originMarket == market) {
				tooltip.addPara("Gauss rifle, schoolhouse and charter have forcefully reintegrated " + data.getOriginName() + "'s indigenous population, scattered into subcultures by the collapse of the previous governing authority.", 3f);
			} else {
				tooltip.addPara("A non-negligible fraction of the population of " + name + " originate from " + data.getOriginName() + ", forcefully reintegrated following the collapse of the previous governing authority and relocated on mass, often involuntarily.", 3f);
			}
			tooltip.addPara("%s population growth (based on colony size)", 10f, Misc.getHighlightColor(), "+" + (int) getImmigrationBonus());
			tooltip.addSpacer(10f);
			tooltip.addSectionHeading("Specialises in " + INDUSTRY_NAMES.get(data.type), market.getFaction().getBaseUIColor(), market.getFaction().getDarkUIColor(), Alignment.MID, 10f);
			appendDescription(tooltip, data.originMarket, data.type);

			var dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
			if (market.getFaction().isPlayerFaction()) {
				tooltip.addSpacer(10f);
				if (atPlayerColony())
					if (market.getAdmin().getStats().hasSkill(JDP_RECIV)) {
						tooltip.addPara("Press [F1] to consider relocation to another market under our control.", 3f, Misc.getGrayColor(), Misc.getHighlightColor(), "F1");
						tooltip.addPara("*Will cause " + STABILITY_PENALTY + " point of unrest to both markets.", 3f, Misc.getGrayColor(), Misc.getHighlightColor(), STABILITY_PENALTY + "");

						if (Keyboard.isKeyDown(Keyboard.KEY_F1)) {
							Global.getSoundPlayer().playUISound("ui_channel_comm_secure", 1f, 2f);

							ArrayList<SectorEntityToken> targets = new ArrayList<>();
							for (MarketAPI factionMarket : Global.getSector().getEconomy().getMarketsCopy()) {
								if (factionMarket.getFaction().isPlayerFaction() && factionMarket != market && !factionMarket.getMemory().contains(EFFECT_KEY)) targets.add(factionMarket.getPrimaryEntity());
							}

							dialog.showCampaignEntityPicker("Relocate " + data.getOriginName() + " " + condition.getName(), "Destination:", "Approve", Global.getSector().getPlayerFaction(), targets, new BaseCampaignEntityPickerListener() {
									public void pickedEntity(SectorEntityToken entity) {
										if (!market.getMemory().contains(EFFECT_KEY)) return;
										var destinationMarket = entity.getMarket();
										SubpopulationEffectData data = (SubpopulationEffectData) market.getMemory().get(EFFECT_KEY);

										var SubpopulationMovementIntel = new SubpopulationMovementIntel(market, destinationMarket, condition, data);
										Global.getSector().getIntelManager().addIntel(SubpopulationMovementIntel);

										Global.getSector().addScript(new DelayedActionScript(getTravelDaysWithDelay(entity, market.getPrimaryEntity())) {
											@Override
											public void doAction() {
												destinationMarket.getMemory().set(EFFECT_KEY, data);
												destinationMarket.addCondition(condition.getId());
												RecentUnrest.get(destinationMarket).add(STABILITY_PENALTY, "Forced Relocation (" + condition.getName() + ")");
												updateMarket(destinationMarket);

												MessageIntel intel = new MessageIntel();
												intel.setIcon(Global.getSettings().getMarketConditionSpec(condition.getId()).getIcon());
												intel.endAfterDelay(1);
												intel.setSound(intel.getCommMessageSound());
												intel.addLine(condition.getName() + " - Forced Relocation (Complete)", Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
												intel.addLine(BaseIntelPlugin.BULLET + market.getName() + " –> " + destinationMarket.getName(), Misc.getTextColor(), new String[]{market.getName(), destinationMarket.getName()}, Misc.getHighlightColor(), Misc.getHighlightColor());
												Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, destinationMarket);
												Global.getSector().getIntelManager().removeIntel(SubpopulationMovementIntel);
											}
										});
										Global.getSoundPlayer().playUISound("ui_cargo_marines_drop", 1f, 1f);
										market.removeCondition(condition.getId());
										market.getMemory().unset(EFFECT_KEY);
										RecentUnrest.get(market).add(STABILITY_PENALTY, "Forced Relocation (" + condition.getName() + ")");
										updateMarket(market);
									}

									public void cancelledEntityPicking() {

									}

									public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
										StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
										if (system != null) {
											return system.getNameWithLowercaseTypeShort();
										}
										return null;
									}

									public String getSelectedTextOverrideFor(SectorEntityToken entity) {
										StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
										if (system != null) {
											return system.getNameWithLowercaseType();
										}
										return null;
									}

									public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {
										int days = getTravelDaysWithDelay(entity, market.getPrimaryEntity());
										info.setParaSmallInsignia();
										String daysStr = "days";
										if (days == 1) daysStr = "day";
										info.addPara("    Estimated relocation time: %s " + daysStr, 0f, Misc.getHighlightColor(), "" + days);
									}

									public boolean canConfirmSelection(SectorEntityToken entity) {
										return true;
									}

									public float getFuelColorAlphaMult() {
										return 0f;
									}

									public float getFuelRangeMult() {
										return 0f;
									}

								});
						}
					} else {
						tooltip.addPara("Requires a qualified administrator on this market to consider relocation.", Misc.getGrayColor(), 3f);
						Person fake = (Person) Global.getFactory().createPerson();
						fake.setFaction(Factions.SLEEPER);
						fake.getStats().setSkillLevel(JDP_RECIV, 1f);
						tooltip.addSkillPanel(fake, true, 3f);
					}
				else {
					tooltip.addPara("Giving the order to relocate this condition requires secured comms channels only available at markets under our control.", Misc.getGrayColor(), 3f);
				}
			}
		}
	}
}

