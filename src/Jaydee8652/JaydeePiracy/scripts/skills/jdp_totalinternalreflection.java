package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistTags;
import Jaydee8652.JaydeeProficiencies.utils.refl.ReflectionUtilis;
import Jaydee8652.JaydeeProficiencies.utils.refl.ui.ClassRefs;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.impl.hullmods.ShardSpawner;
import com.fs.starfarer.api.input.InputEventClass;
import com.fs.starfarer.api.input.InputEventType;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData;


public class jdp_totalinternalreflection {
	public static final String JDP_CAN_RECOVER_PENROSE = "$jdp_canRecoverPenrose";
	public static final String JDP_INCOMPREHENSIBLE_HULL = "jdp_incomprehensible";



	public static class Level0 extends BaseSkillEffectDescription implements ShipSkillEffect {
		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {
			Color h = Misc.getHighlightColor();

			info.addPara("Enables the recovery and maintenance of the Penrose-class Tesseron Fleetkiller", h, 3f);
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}

		@Override
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {

		}

		@Override
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {

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
				Global.getSector().getMemoryWithoutUpdate().set(JDP_CAN_RECOVER_PENROSE, true);
			}
		}

		@Override
		public void unapply(MutableCharacterStatsAPI stats, String id) {
			if (Objects.equals(id, jdp_SpecialistTags.SKILL_TAG_MARKER)) {
				Global.getSector().getMemoryWithoutUpdate().unset(JDP_CAN_RECOVER_PENROSE);
			}
		}
	}
}