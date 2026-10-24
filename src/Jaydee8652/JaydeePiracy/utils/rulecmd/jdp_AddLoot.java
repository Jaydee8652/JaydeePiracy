package Jaydee8652.JaydeePiracy.utils.rulecmd;

import Jaydee8652.JaydeePiracy.scripts.intel.NotableSystemIntel;
import Jaydee8652.JaydeePiracy.utils.jdp_People;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.intel.misc.SurveyDataForPlanetIntel;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial;
import com.fs.starfarer.api.util.Misc;

import java.util.*;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils.findInterestingSystem;

public class jdp_AddLoot extends BaseCommandPlugin {
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        TextPanelAPI text = dialog.getTextPanel();

        String command = params.get(0).getString(memoryMap);
        return switch (command) {
            case "Maddie" -> {
                var quantity = 3;
                var data = new SpecialItemData("jdp_frictionlessmetal", null);
                cargo.addSpecial(data, quantity);
                AddRemoveCommodity.addItemGainText(data, quantity, text);

                yield true;
            }
            case "Hiroc" -> {
                var quantity = 1;
                var data = new SpecialItemData("industry_bp", "jdp_planetkillerLaunchSite");
                cargo.addSpecial(data, quantity);
                AddRemoveCommodity.addItemGainText(data, quantity, text);

                yield true;
            }
            case "Mantle" -> {
                PlanetAPI depleted = (PlanetAPI) Global.getSector().getMemoryWithoutUpdate().get("$jdp_autonomousMantleBoreKey");

                var random = new Random();
                HashSet<StarSystemAPI> systems = new HashSet<>();
                systems.add(depleted.getStarSystem());
                systems.add(findInterestingSystem(systems, random));
                systems.add(findInterestingSystem(systems, random));

                for (StarSystemAPI system : systems) new NotableSystemIntel(system, "One of three fringe systems known to have been frequented by the EUTS Intelligent Designer, an executive transport shuttle owned by the Eridani-Utopia Terraforming Company.\n\nIt may contain uninhabited terraformed worlds free for survey or colonisation.", text);

                yield true;
            }
            default -> false;
        };
    }

}
