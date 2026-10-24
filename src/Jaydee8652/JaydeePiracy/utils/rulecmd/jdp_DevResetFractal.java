package Jaydee8652.JaydeePiracy.utils.rulecmd;

import java.util.List;
import java.util.Map;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.utils.jdp_People;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.rpg.Person;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.apache.log4j.Logger;

import static Jaydee8652.JaydeePiracy.campaign.world.jdp_Dory.addPenroseFleet;


public class jdp_DevResetFractal extends BaseCommandPlugin {
	public static Logger log = Global.getLogger(JaydeePiracyPlugin.class);
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

		log.info("JDP LOG: Fractal Theorem has been reset. (BY DEV)");

		//Clean up old
		Global.getSector().addScript(new DelayedActionScript(1) {
			@Override
			public void doAction() {

				Global.getSector().getMemoryWithoutUpdate().unset("$player.shotSedge");

				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_PRinProgress");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_PRref");

				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_PRCompleted");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_penroseDefeated");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_penroseFound");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_penroseTriedAlpha");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_penroseHuntingGargoyle");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_penroseHuntingMaerula");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_penroseNoAlpha");
				Global.getSector().getMemoryWithoutUpdate().unset("$jdp_penroseHasData");

				if (Global.getSector().getEntityById("jdp_penrose_derelict") != null) {
					SectorEntityToken penrose_derelict = Global.getSector().getEntityById("jdp_penrose_derelict");
					penrose_derelict.getContainingLocation().removeEntity(penrose_derelict);
				}

				if (Global.getSector().getEntityById("jdp_penrosefleet") != null) {
					SectorEntityToken penrose_derelict = Global.getSector().getEntityById("jdp_penrosefleet");
					penrose_derelict.getContainingLocation().removeEntity(penrose_derelict);
				}
			}
		});

		//Respawn fleet
		Global.getSector().addScript(new DelayedActionScript(2) {
			@Override
			public void doAction() {
				log.info("JDP LOG: The Penorse has been regenerated (BY DEV)");

				Global.getSector().getMemoryWithoutUpdate().set("$jdp_conquered_hypershunt", true);
				addPenroseFleet();
			}
		});

		//Fire the event again
		Global.getSector().addScript(new DelayedActionScript(3) {
			@Override
			public void doAction() {
				CampaignFleetAPI player = Global.getSector().getPlayerFleet();
				if (player == null) return;
				log.info("JDP LOG: The Penrose Quest has refired (BY DEV)");
				Global.getSector().getCampaignUI().showInteractionDialog(new RuleBasedInteractionDialogPluginImpl("jdp_hypershuntConquered"), player);
			}
		});
		return true;
	}
}








