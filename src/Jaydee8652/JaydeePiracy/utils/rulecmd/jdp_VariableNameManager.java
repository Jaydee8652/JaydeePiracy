package Jaydee8652.JaydeePiracy.utils.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class jdp_VariableNameManager extends BaseCommandPlugin {
	public static String JDP_NMC_KEY = "$jdp_nmcInventionCycle";
	public static String JDP_TRIPATHIA_KEY = "$jdp_tripathiaDiscoveryData";
	public void update() {
		//Tripathia
		Global.getSettings().getHullSpec("jdp_tripathia").setDescriptionPrefix(tripathiaDiscoveryData());

		//NMC-000
		Global.getSettings().getSpecialItemSpec("jdp_frictionlessmetal").setName(
				"Frictionless NMC-" + nmcInventionCycle());
		Global.getSettings().getSpecialItemSpec("jdp_frictionlessmetal").setDesc(
				"An anomalous crystalline material acquired in extremely limited quantities during fringe mining operations on Maddie. Designation Frictionless NMC-" + nmcInventionCycle() + ", the substance was formed in the radiation of a red pulsar and is iridescent like the shell of an arthropod-analog. \n\n" +

				"Considered a chronohazard, it is capable of demonstrating novel temporal properties, inverting the effect of relativity within its sphere of influence with an intensity proportional to mass.");
	}

	public static String nmcInventionCycle() {
		var data = Global.getSector().getMemoryWithoutUpdate().get(JDP_NMC_KEY);
		if (data != null) {
			return data.toString();
		} else {
			return "000";
		}
	}

	public static String tripathiaDiscoveryData() {
		var data = Global.getSector().getMemoryWithoutUpdate().get(JDP_TRIPATHIA_KEY);
		if (data != null) {
			return data.toString();
		} else {
			return "IDENTIFICATION ERROR: Target matches no known class of ship in the Domain registry or historical reference. Submit data for temporary entry? Processing...";
		}
	}

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String command = params.get(0).getString(memoryMap);
		return switch (command) {
			case "Maddie" -> {
				Global.getSector().getMemoryWithoutUpdate().set(JDP_NMC_KEY, Global.getSector().getClock().getCycle());
				update();
				yield true;
			}
			case "Tripathia" -> {
				var string = "IDENTIFICATION ERROR, NO PERMANENT ENTRY: Data for temporary entry provided by Captain " + Global.getSector().getPlayerPerson().getName().getFullName() + " and " + Global.getSector().getPlayerPerson().getHisOrHer() + " crew aboard the " + Global.getSector().getPlayerFleet().getFlagship().getShipName() +" on " + Global.getSector().getClock().getDateString();
				Global.getSector().getMemoryWithoutUpdate().set(JDP_TRIPATHIA_KEY, string);
				update();
				yield true;
			}
			default -> false;
		};
	}
}








