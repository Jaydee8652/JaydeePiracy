package Jaydee8652.JaydeePiracy;

import Jaydee8652.JaydeePiracy.campaign.econ.industries.jdp_ItemEffectsRepo;
import Jaydee8652.JaydeePiracy.campaign.econ.raid.jdp_GroundRaidObjectivesCreator;
import Jaydee8652.JaydeePiracy.campaign.procgen.jdp_LampDefenderPluginImpl;
import Jaydee8652.JaydeePiracy.plugins.jdp_lunaSettings;
import Jaydee8652.JaydeePiracy.plugins.jdp_pickAICoreOfficer;
import Jaydee8652.JaydeePiracy.scripts.*;
import Jaydee8652.JaydeePiracy.scripts.ai.jdp_defensetorpedoAI;
import Jaydee8652.JaydeePiracy.scripts.ai.jdp_relayAI;
import Jaydee8652.JaydeePiracy.scripts.skills.jdp_marinetraining;
import Jaydee8652.JaydeePiracy.utils.codex.jdp_CodexData;
import Jaydee8652.JaydeePiracy.utils.rulecmd.jdp_VariableNameManager;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.BaseModPlugin;
import Jaydee8652.JaydeePiracy.campaign.procgen.jdp_DerelictMissileDefenderPluginImpl;
import Jaydee8652.JaydeePiracy.compat.seatsofpower.*;
import data.listeners.timeline.MiscEventListener;
import data.memory.AoTDSopMemFlags;
import data.scripts.managers.TimelineListenerManager;
import org.apache.log4j.Logger;

public class JaydeePiracyPlugin extends BaseModPlugin {
    public static Logger log = Global.getLogger(JaydeePiracyPlugin.class);

    //Custom AI DRONES
    @Override
    public PluginPick<ShipAIPlugin> pickShipAI(FleetMemberAPI member, ShipAPI ship) {
        if (ship.getHullSpec().getHullId().equals("jdp_relay")) return new PluginPick<ShipAIPlugin>(new jdp_relayAI(ship), CampaignPlugin.PickPriority.MOD_GENERAL);
        return super.pickShipAI(member, ship);
    }

    //Custom AI MISSILES
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case "jdp_defensetorpedo_srm": return new PluginPick<MissileAIPlugin>(new jdp_defensetorpedoAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC);
            default:
        }
        return null;
    }

    public void onApplicationLoad() {
        jdp_ItemEffectsRepo.addItemEffectsToVanillaRepo();//Planetkiller Colony Item
    }

    public void onGameLoad(boolean newGame) {
        new jdp_retrogen().generate(Global.getSector());
        new jdp_VariableNameManager().update();

        //Listeners
        new jdp_RefitApplicator().init();
        new jdp_GroundRaidObjectivesCreator().init();
        new jdp_CreateFlowerfishExecutive().init();
        new jdp_OmegaCombatListener().init();
        new jdp_marinetraining().init();

        //Plugins
        new jdp_DerelictMissileDefenderPluginImpl().init();
        new jdp_LampDefenderPluginImpl().init();

        Global.getSector().registerPlugin(new jdp_pickAICoreOfficer());//Allows JDP Omega Cores to be officers
        if (Global.getSettings().getModManager().isModEnabled("aotd_sop")) {
            TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.MISC_EVENT,new jdp_OmegakinEvent()));
            TimelineListenerManager.getInstance().addNewListener(new MiscEventListener(AoTDSopMemFlags.FIRST_ITEM,new jdp_InterfectorEvent()));
        }
    }

    @Override
    public void onAboutToLinkCodexEntries() {
        super.onAboutToLinkCodexEntries();
        jdp_CodexData.linkCodexEntries();
    }
}
