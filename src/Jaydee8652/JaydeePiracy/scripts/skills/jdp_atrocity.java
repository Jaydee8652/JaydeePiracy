package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_DisplayElement;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedActionScript;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_Stats;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeePiracy.campaign.econ.jdp_recivilizedMinority.STABILITY_PENALTY;
import static Jaydee8652.JaydeeProficiencies.patch.MarketCommandPatch.isOverlooked;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.MILITARY_TAGS;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.getIndustryByTag;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.generateTerraformingIntel;

public class jdp_atrocity {
	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if(market.isPlayerOwned()) {
				if (market.hasIndustry(Industries.HIGHCOMMAND) && market.getIndustry(Industries.HIGHCOMMAND).isFunctional()) {
					Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().set(jdp_Stats.SATURATION_BOMBARDMENT_DECIV, true);

					if (!market.hasTag(id) && !isOverlooked()) {
						var script = new jdp_DelayedActionScript(0f, market, id, 5, 0) {
							@Override
							public void doAction() {
								if (!Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().contains(jdp_Stats.SATURATION_BOMBARDMENT_OVERLOOK)) {
									MessageIntel intel = new MessageIntel();
									intel.setIcon(Global.getSettings().getSkillSpec(id.replace("_GO_0", "")).getSpriteName());
									intel.endAfterDelay(1);
									intel.setSound(intel.getCommMessageSound());
									intel.addLine(Global.getSettings().getSkillSpec(id.replace("_GO_0", "")).getName() + " - " + market.getName(), Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
									intel.addLine(BaseIntelPlugin.BULLET + "Justifications Fabricated", Misc.getHighlightColor());
									Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
								}
								Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().set(jdp_Stats.SATURATION_BOMBARDMENT_OVERLOOK, true);
								market.removeTag(id);
							}

							@Override
							public Boolean confirmation() {
								var highcommand = market.getIndustry(Industries.HIGHCOMMAND);
								return ((highcommand != null) && (highcommand.isFunctional()) && !isOverlooked() && market.getAdmin().getStats().hasSkill(id.replace("_GO_0", "")));
							}
						};
						Global.getSector().addScript(script);
					}
				}

				var cleanupscript = new DelayedActionScript(5f) {
					@Override
					public void doAction() {
						market.removeTag(id+"_clean");

						if (market.getAdmin().getStats().hasSkill(id.replace("_GO_0", ""))) return;
						Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().unset(jdp_Stats.SATURATION_BOMBARDMENT_DECIV);
						Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().unset(jdp_Stats.SATURATION_BOMBARDMENT_OVERLOOK);
						market.removeTag(id);
					}
				};
				if(!market.hasTag(id+"_clean")) {
					market.addTag(id+"_clean");
					Global.getSector().addScript(cleanupscript);
				}
			}
		}

		public void unapply(MarketAPI market, String id) {
		}

		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {

			info.addPara("Performing saturation bombardments will add the Decivilized condition to destroyed markets", 3f, Misc.getHighlightColor(), "Decivilized");
			info.addSpacer(10f);
			MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(Conditions.DECIVILIZED);
			var sprite = Global.getSettings().getSprite(spec.getIcon());
			var height = 40f;
			var imageWidth = (height / sprite.getHeight()) * sprite.getWidth();

			var element = new jdp_DisplayElement(sprite, "", Misc.getBasePlayerColor(), info, imageWidth, height);
			element.setHasBorder(true);
			info.addPara("Decivilized", Misc.getHighlightColor(), 3f).getPosition().rightOfTop(element.getElementPanel(), 6f);
			info.addPara("%s hazard rating", 3f, Misc.getHighlightColor(), "+25%");
			info.setHeightSoFar(info.getHeightSoFar() - height);

			var space = info.addPara("", Misc.getHighlightColor(), 3f);
			space.getPosition().setXAlignOffset(-(height + 6f));
			info.addPara("After approximately four months, justifications for saturation bombardments will be fabricated, after which saturation bombardments will only provoke the market owner", 3f, Misc.getHighlightColor(), "only provoke the market owner");
			info.addPara("*These bonuses are lost upon moving this administrator or unassigning this skill", 3f, Misc.getGrayColor(), Misc.getHighlightColor());
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.CUSTOM;
		}
	}
}