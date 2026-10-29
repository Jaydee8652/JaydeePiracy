package Jaydee8652.JaydeePiracy.scripts;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.utils.jdp_People;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.util.DelayedActionScript;
import org.apache.log4j.Logger;


import static Jaydee8652.JaydeePiracy.campaign.world.jdp_Dory.addPenroseFleet;

public class jdp_OmegaCombatListener implements FleetEventListener {
	public static Logger log = Global.getLogger(JaydeePiracyPlugin.class);

	public void init() {
		if(Global.getSector().getPersistentData().get("jdp_OmegaCombatListener") == null) {
			Global.getSector().getPersistentData().put("jdp_OmegaCombatListener", this);
			Global.getSector().getPlayerFleet().addEventListener(this);
		}
		if (Global.getSector().getMemoryWithoutUpdate().getBoolean("$nex_randomSector") || Global.getSector().getMemoryWithoutUpdate().getBoolean("$jdp_conquered_hypershunt")) {
			log.info("JDP_LOG: Cleaned jdp_OmegaCombatListener");
			Global.getSector().getPlayerFleet().removeEventListener(this);
		}
	}

	@Override
	public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
	}

	@Override
	public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
		if (fleet == primaryWinner && battle.getNonPlayerSide().stream().anyMatch(f -> f.getFaction().getId().equals(Factions.OMEGA))) {
			if (!fleet.getContainingLocation().getEntitiesWithTag(Entities.CORONAL_TAP).isEmpty()) {
				if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_conquered_hypershunt")) {
					Global.getSector().getMemoryWithoutUpdate().set("$jdp_conquered_hypershunt", true);
					log.info("JDP_LOG: The Penrose has been generated");

					addPenroseFleet();

					Global.getSector().addScript(new DelayedActionScript(2) {
						@Override
						public void doAction() {
							CampaignFleetAPI player = Global.getSector().getPlayerFleet();
							if (player == null) return;
							log.info("JDP_LOG: The Penrose Quest has fired");
							Global.getSector().getCampaignUI().showInteractionDialog(new RuleBasedInteractionDialogPluginImpl("jdp_hypershuntConquered"), player);
						}
					});
				}
				//In theory, if a mod adds an Omega fleet that spawns in Hypershunt Systems it would also trigger
				//this Listener, however for my purposes, the description of the quest says you killed Omega in a
				//system with a Hypershunt, so it would still be correct.
			}
		}
	}
}

