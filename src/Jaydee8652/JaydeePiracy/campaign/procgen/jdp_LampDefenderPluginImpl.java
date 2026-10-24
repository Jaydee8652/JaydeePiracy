package Jaydee8652.JaydeePiracy.campaign.procgen;
import Jaydee8652.JaydeePiracy.campaign.world.jdp_Hiroc;
import Jaydee8652.JaydeePiracy.utils.jdp_Factions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.DataForEncounterSide;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin.FleetMemberData;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.BaseGenericPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.FleetAdvanceScript;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SDMParams;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageGenFromSeed.SalvageDefenderModificationPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.text.html.HTML;

//Remember - you need to activate the script in the modplugin for it to work

public class jdp_LampDefenderPluginImpl extends BaseGenericPlugin implements SalvageDefenderModificationPlugin {
	public void init() {
		if(Global.getSector().getPersistentData().get("jdp_LampDefenderPluginImpl") == null) {
			Global.getSector().getPersistentData().put("jdp_LampDefenderPluginImpl", this);
			Global.getSector().getGenericPlugins().addPlugin(this);
		}
	}

	public void modifyFleet(SDMParams p, CampaignFleetAPI fleet, Random random, boolean withOverride) {
		fleet.getFleetData().clear();

		fleet.setNoFactionInName(true);
		fleet.setName("Erratic Fusion Lamp");

		fleet.setInflated(true);
		fleet.setFaction(jdp_Factions.JDP_FABRIQUEORBITALE);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true);

		fleet.setStationMode(true);
		fleet.addTag("sc_do_not_generate_skills");

		FleetMemberAPI lamp = fleet.getFleetData().addFleetMember("jdp_lamp_Erratic");
		lamp.setShipName("Erratic Fusion Lamp");
		lamp.updateStats();
		lamp.getRepairTracker().setCR(lamp.getRepairTracker().getMaxCR());
		lamp.setVariant(lamp.getVariant().clone(), false, false);
		lamp.getVariant().setSource(VariantSource.REFIT);
	}

	@Override
	public int getHandlingPriority(Object params) {
		if (!(params instanceof SDMParams)) return 0;
		SDMParams p = (SDMParams) params;

		if (p.entity != null && p.entity.hasTag("jdp_erratic_lamp_tag")) {
			return 2;
		}
		return -1;
	}


	public float getQuality(SDMParams p, float quality, Random random, boolean withOverride) {return quality;}
	public float getStrength(SDMParams p, float strength, Random random, boolean withOverride) {
		return strength;
	}
	public float getMinSize(SDMParams p, float minSize, Random random, boolean withOverride) {
		return minSize;
	}
	public float getMaxSize(SDMParams p, float maxSize, Random random, boolean withOverride) {
		return maxSize;
	}
	public float getProbability(SDMParams p, float probability, Random random, boolean withOverride) {return probability;}
	public void reportDefeated(SDMParams p, SectorEntityToken entity, CampaignFleetAPI fleet) {}
}





