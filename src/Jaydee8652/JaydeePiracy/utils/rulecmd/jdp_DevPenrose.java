package Jaydee8652.JaydeePiracy.utils.rulecmd;

import java.util.List;
import java.util.Map;

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

public class jdp_DevPenrose extends BaseCommandPlugin {
	public static Logger log = Global.getLogger(jdp_People.class);
	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		log.info("JDP LOG: The Penorse has been generated (BY DEV)");

		Global.getSector().getMemoryWithoutUpdate().set("$jdp_conquered_hypershunt", true);
		addPenroseFleet();

		Global.getSector().addScript(new DelayedActionScript(2) {
			@Override
			public void doAction() {
				CampaignFleetAPI player = Global.getSector().getPlayerFleet();
				if (player == null) return;
				log.info("JDP LOG: The Penrose Quest has fired (BY DEV)");
				Global.getSector().getCampaignUI().showInteractionDialog(new RuleBasedInteractionDialogPluginImpl("jdp_hypershuntConquered"), player);
			}
		});
		return true;
	}
}








