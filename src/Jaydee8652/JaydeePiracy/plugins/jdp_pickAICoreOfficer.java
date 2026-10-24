package Jaydee8652.JaydeePiracy.plugins;

import java.util.Random;

import Jaydee8652.JaydeePiracy.utils.jdp_Items;
import Jaydee8652.JaydeePiracy.utils.jdp_Ranks;
import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.BaseCampaignPlugin;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.BaseAICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeePiracy.utils.jdp_People.JDP_OMEGA;

public class jdp_pickAICoreOfficer extends BaseCampaignPlugin {
	//Loaded on game in the mod plugin, applies officer stats to my copy of the Omega Core
	@Override
	public PluginPick<AICoreOfficerPlugin> pickAICoreOfficerPlugin(String commodityId) {
		if (commodityId != null && commodityId.equals(jdp_Items.JDP_CORE_OMEGA)) {
			return new PluginPick<>(
                    new omegaOfficerPluginImpl(),
					CampaignPlugin.PickPriority.MOD_SET
			);
		}
		return null;
	}

	public static class omegaOfficerPluginImpl extends BaseAICoreOfficerPluginImpl {//Applies the Omega officer stats to my clone of the item
		public static float POINT_MULT = 4;

		public PersonAPI createPerson(String aiCoreId, String factionId, Random random) {
			PersonAPI person = Global.getSector().getImportantPeople().getPerson(JDP_OMEGA);
			person.setAICoreId(aiCoreId);
			person.getStats().setSkipRefresh(true);

			// undo the +1 level from integration if the ship was scuttled since we're reusing the same PersonAPI
			if (Misc.isUnremovable(person)) {
				Misc.setUnremovable(person, false);
				person.getStats().setLevel(person.getStats().getLevel() - 1);
				for (MutableCharacterStatsAPI.SkillLevelAPI skillLevel : person.getStats().getSkillsCopy()) {
					if (skillLevel.getSkill().isCombatOfficerSkill() && !skillLevel.getSkill().hasTag(Skills.TAG_NPC_ONLY)) {
						person.getStats().setSkillLevel(skillLevel.getSkill().getId(), 0);
						break;
					}
				}
			}
			person.getMemoryWithoutUpdate().set(AUTOMATED_POINTS_MULT, POINT_MULT);
			person.setPersonality(Personalities.RECKLESS);

			person.getStats().setSkipRefresh(false);
			return person;
		}
	}
}
