package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.fleet.FleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.rpg.Person;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;

import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonOfficer;

public class jdp_inactive {
	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
		}


		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {

			var officer = getPersonOfficer(stats);
			if (officer != null) {
				HashMap<String, Float> map;
				map = (HashMap<String, Float>) officer.getPerson().getMemoryWithoutUpdate().get("$jdp_flowerfishPersonalDiscretion_inactive");

				if (map != null) {
					String plurality = "skill is";
					if (map.size() > 1) plurality = "skills are";

					info.addPara("This officer is above their max level and elite skill cap, the following " + plurality + " inactive.",
							3f, Misc.getHighlightColor(), "This officer is above their max level and elite skill cap, the following " + plurality + " inactive.");
					info.addSpacer(10f);

					Person fake = (Person) Global.getFactory().createPerson();
					fake.setFaction(Factions.SLEEPER);

					for (HashMap.Entry<String, Float> entry : map.entrySet()) {
						String inactiveSkill = entry.getKey();
						Float level = entry.getValue();

						fake.getStats().setSkillLevel(inactiveSkill, level);
					}
					info.addSkillPanel(fake, false, 3f);
				}
			}
		}

		@Override
		public boolean hasCustomDescription() {
			return true;
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
	public static class Level2 implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
		}

		public String getEffectDescription(float level) {
			return "";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}