package Jaydee8652.JaydeePiracy.campaign.procgen;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.BaseGenericPlugin;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SalvageDefenderModificationPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import Jaydee8652.JaydeePiracy.campaign.world.jdp_Hiroc;

//Remember - you need to activate the script in the modplugin for it to work

public class jdp_DerelictMissileDefenderPluginImpl extends BaseGenericPlugin implements SalvageDefenderModificationPlugin {
	public void init() {
		if(Global.getSector().getPersistentData().get("jdp_DerelictMissileDefenderPluginImpl") == null) {
			Global.getSector().getPersistentData().put("jdp_DerelictMissileDefenderPluginImpl", this);
			Global.getSector().getGenericPlugins().addPlugin(this);
		}
	}

	public float getStrength(SDMParams p, float strength, Random random, boolean withOverride) {
		return strength;
	}
	public float getMinSize(SDMParams p, float minSize, Random random, boolean withOverride) {
		return minSize;
	}
	
	public float getMaxSize(SDMParams p, float maxSize, Random random, boolean withOverride) {
		return maxSize; 
	}
	
	public float getProbability(SDMParams p, float probability, Random random, boolean withOverride) {
		return probability;
	}
	
	public void reportDefeated(SDMParams p, SectorEntityToken entity, CampaignFleetAPI fleet) {
	}

	public static PersonAPI createCustodianCaptain() {
		PersonAPI person = Global.getFactory().createPerson();
		person.setFaction(Factions.DERELICT);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "jdp_rampant"));
		person.setPersonality(Personalities.RECKLESS);
		person.setRankId(Ranks.UNKNOWN);
		person.setPostId(null);

		person.getStats().setSkipRefresh(true);

		person.getStats().setLevel(8);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.FIGHTER_UPLINK, 2);
		person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);

		person.getStats().setSkipRefresh(false);

		return person;
	}

	public static PersonAPI createGuardianCaptain() {
		PersonAPI person = Global.getFactory().createPerson();
		person.setFaction(Factions.DERELICT);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "jdp_rampant"));
		person.setPersonality(Personalities.RECKLESS);
		person.setRankId(Ranks.UNKNOWN);
		person.setPostId(null);

		person.getStats().setSkipRefresh(true);

		person.getStats().setLevel(8);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.BALLISTIC_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);

		person.getStats().setSkipRefresh(false);

		return person;
	}

	public void modifyFleet(SDMParams p, CampaignFleetAPI fleet, Random random, boolean withOverride) {

		fleet.setNoFactionInName(true);
		fleet.setName("Custodians");

		fleet.getFleetData().setShipNameRandom(random);

		PersonAPI custodianCaptain = createCustodianCaptain();
		PersonAPI guardianCaptain = createGuardianCaptain();

		//Custodian
		FleetMemberAPI tekoi = fleet.getFleetData().addFleetMember("jdp_custodian_Aberrant");
		tekoi.setCaptain(custodianCaptain);
		tekoi.setShipName("Alpha-Tekoi");
		tekoi.updateStats();
		tekoi.getRepairTracker().setCR(tekoi.getRepairTracker().getMaxCR());
		tekoi.setVariant(tekoi.getVariant().clone(), false, false);
		tekoi.getVariant().setSource(VariantSource.REFIT);

		FleetMemberAPI kismet = fleet.getFleetData().addFleetMember("jdp_custodian_Aberrant");
		kismet.setCaptain(custodianCaptain);
		kismet.setShipName("Psi-Kismet");
		kismet.updateStats();
		kismet.getRepairTracker().setCR(kismet.getRepairTracker().getMaxCR());
		kismet.setVariant(kismet.getVariant().clone(), false, false);
		kismet.getVariant().setSource(VariantSource.REFIT);

		FleetMemberAPI cimarron = fleet.getFleetData().addFleetMember("jdp_custodian_Aberrant");
		cimarron.setCaptain(custodianCaptain);
		cimarron.setShipName("Mu-Cimarron");
		cimarron.updateStats();
		cimarron.getRepairTracker().setCR(cimarron.getRepairTracker().getMaxCR());
		cimarron.setVariant(cimarron.getVariant().clone(), false, false);
		cimarron.getVariant().setSource(VariantSource.REFIT);

		//Guardian
		FleetMemberAPI unicorn = fleet.getFleetData().addFleetMember("guardian_Standard");
		unicorn.setCaptain(guardianCaptain);
		unicorn.setShipName("Zayin-Unicorn");
		unicorn.updateStats();
		unicorn.getRepairTracker().setCR(unicorn.getRepairTracker().getMaxCR());
		unicorn.setVariant(unicorn.getVariant().clone(), false, false);
		unicorn.getVariant().setSource(VariantSource.REFIT);

		FleetMemberAPI rocco = fleet.getFleetData().addFleetMember("guardian_Standard");
		rocco.setCaptain(guardianCaptain);
		rocco.setShipName("Lambda-Rocco");
		rocco.updateStats();
		rocco.getRepairTracker().setCR(rocco.getRepairTracker().getMaxCR());
		rocco.setVariant(rocco.getVariant().clone(), false, false);
		rocco.getVariant().setSource(VariantSource.REFIT);
	}

	@Override
	public int getHandlingPriority(Object params) {
		if (!(params instanceof SDMParams)) return 0;
		SDMParams p = (SDMParams) params;
		
		if (p.entity != null && p.entity.getMemoryWithoutUpdate().contains(
				jdp_Hiroc.JDP_MISSILE_KEY)) {
			return 2;
		}
		return -1;
	}
	public float getQuality(SDMParams p, float quality, Random random, boolean withOverride) {
		return quality;
	}
}



