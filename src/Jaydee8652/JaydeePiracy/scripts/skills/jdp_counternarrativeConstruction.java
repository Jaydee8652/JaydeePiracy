package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import Jaydee8652.JaydeeProficiencies.patch.MarketCommandPatch;
import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_DisplayElement;
import Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistTags;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_Stats;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.console.commands.DevMode;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

import static Jaydee8652.JaydeeProficiencies.patch.MarketCommandPatch.getClaim;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getSpecialistData;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.*;

public class jdp_counternarrativeConstruction implements EconomyTickListener {
	public static String HAS_COUNTERNARRATIVE = "$jdp_hasCounternarrativeConstruction";
	public static String CLAIM_FABRICATION_TARGET = "$jdp_claimFabricationTarget";
	public static int PROGRESS_PER_TICK = 1;

	@Override
	public void reportEconomyTick(int iterIndex) {
		var faction = claimFabricationTarget();
		if (faction == null) return;

		HashMap<FactionAPI, MarketCommandPatch.claimData> data = (HashMap<FactionAPI, MarketCommandPatch.claimData>) Global.getSector().getMemoryWithoutUpdate().get(jdp_Stats.SATBOMB_CLAIMS);
		var claim = getClaim(faction);

		var progress = PROGRESS_PER_TICK;
		if(Global.getSettings().isDevMode()) progress *= 10;

		claim.setProgress(MathUtils.clamp((claim.getProgress() + progress), 0, claim.getGoal()));
		if (claim.getProgress() >= claim.getGoal() && !claim.getReady()) {
			claim.setReady(true);

			MessageIntel intel = new MessageIntel();
			intel.setIcon(Global.getSettings().getSkillSpec(jdp_Skills.JDP_ATROCITY).getSpriteName());
			intel.endAfterDelay(1);
			intel.setSound(intel.getCommMessageSound());
			intel.addLine(Global.getSettings().getSkillSpec(jdp_Skills.JDP_ATROCITY).getName() + " - Fabricate Claim (Complete)", Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
			intel.addLine(BaseIntelPlugin.BULLET + "Faction: " + capitaliseFirst(faction.getDisplayName()), Misc.getTextColor(), new String[]{capitaliseFirst(faction.getDisplayName())}, faction.getBaseUIColor());
			Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.CHARACTER_TAB);
		}

		data.replace(faction, claim);
		Global.getSector().getMemoryWithoutUpdate().set(jdp_Stats.SATBOMB_CLAIMS, data);
	}

	@Override
	public void reportEconomyMonthEnd() {

	}

	public static FactionAPI claimFabricationTarget() {
		if (!Global.getSector().getMemoryWithoutUpdate().contains(CLAIM_FABRICATION_TARGET)) return null;
		return Global.getSector().getFaction((String) Global.getSector().getMemoryWithoutUpdate().get(CLAIM_FABRICATION_TARGET));
	}


	public static class jdp_counternarrativeTooltipCreator extends BaseTooltipCreator {
		private final PersonAPI person;
		private final FactionAPI faction;

		public jdp_counternarrativeTooltipCreator(PersonAPI person, FactionAPI faction) {
			this.person = person;
			this.faction = faction;
		}

		@Override
		public float getTooltipWidth(Object tooltipParam) {
			return 650f;
		}

		@Override
		public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
			tooltip.addPara("Select the faction against which will be claims fabricated with the Counternarrative Construction skill.", 3f, Misc.getTextColor(), Misc.getHighlightColor(), "Counternarrative Construction");
			tooltip.addPara("*If the skill is unassigned, the current progress towards claims will be saved.", Misc.getGrayColor(), 3f);
			tooltip.addSpacer(10f);

			var article = "";
			if ("the ".equals(faction.getDisplayNameWithArticle().toLowerCase().replace(faction.getDisplayName().toLowerCase(), ""))) article = "the ";

			var previous = getClaim(faction).getPrevious();
			var prev = tooltip.addPara("A total of " + previous + " claims have been fabricated and used against " + article + capitaliseFirst(faction.getDisplayName()) + ".", 3f, Misc.getTextColor(), Misc.getHighlightColor());
			prev.setHighlight(previous + "", capitaliseFirst(faction.getDisplayName()));
			prev.setHighlightColors(Misc.getHighlightColor(), faction.getBaseUIColor());

			tooltip.addSpacer(10f);
			tooltip.addPara("Left-click to select a faction.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Left-click");
			tooltip.addPara("Right-click to unselect a faction.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Right-click");

			var specialist = getSpecialistData().getSpecialist(person.getId());
			assert specialist != null;

			if (!Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().contains(jdp_counternarrativeConstruction.HAS_COUNTERNARRATIVE)) {
				tooltip.addSpacer(10f);
				var skill = tooltip.addPara(person.getName().getFullName() + " must have the Counternarrative Construction skill active to fabricate claims.", 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
				skill.setHighlight(person.getName().getFullName(), "Counternarrative Construction");
				skill.setHighlightColors(specialist.getProficiencySpec().getColor(), Misc.getHighlightColor());
			}
		}
	}

	public static class Level0 extends BaseSkillEffectDescription implements ShipSkillEffect {
		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {

			info.addPara("Performing Saturation Bombardments will add the Decivilized condition to destroyed markets", 3f, Misc.getHighlightColor(), "Saturation Bombardments", "Decivilized");
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
			info.addPara("Allows justifications for Saturation Bombardment to be fabricated against a faction", 3f, Misc.getHighlightColor(), "Saturation Bombardment");
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

		@Override
		public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {

		}

		@Override
		public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {

		}
	}

	public static class Level1 implements CharacterStatsSkillEffect {
		@Override
		public String getEffectDescription(float level) {
			return "";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

		@Override
		public void apply(MutableCharacterStatsAPI stats, String id, float level) {
			if (Objects.equals(id, jdp_SpecialistTags.SKILL_TAG_MARKER)) {
				Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().set(jdp_Stats.SATURATION_BOMBARDMENT_DECIV, true);
				Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().set(HAS_COUNTERNARRATIVE, true);

				if(!Global.getSector().getListenerManager().hasListenerOfClass(jdp_counternarrativeConstruction.class)) {
					Global.getSector().getListenerManager().addListener(new jdp_counternarrativeConstruction());
				}
			}
		}

		@Override
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			if (Objects.equals(id, jdp_SpecialistTags.SKILL_TAG_MARKER)) {
				Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().unset(jdp_Stats.SATURATION_BOMBARDMENT_DECIV);
				Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().unset(HAS_COUNTERNARRATIVE);

				if(Global.getSector().getListenerManager().hasListenerOfClass(jdp_counternarrativeConstruction.class)) {
					Global.getSector().getListenerManager().removeListenerOfClass(jdp_counternarrativeConstruction.class);
				}
			}
		}
	}
}