package Jaydee8652.JaydeePiracy.scripts;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.utils.jdp_People;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import Jaydee8652.JaydeePiracy.campaign.world.*;
import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class jdp_retrogen implements SectorGeneratorPlugin {
    public static Logger log = Global.getLogger(JaydeePiracyPlugin.class);

    //RetroGenerate
    @Override
    public void generate(SectorAPI sector) {
        jdp_IndustryPlacer.placeIndustries();
        jdp_IndustryPlacer.assignMainIndustry();

        //Systems
        //Generate Hiroc
        if (sector.getStarSystem("jdp_Hiroc") == null) {
            new jdp_Hiroc().generate(sector);
            log.info("JDP_RETROGEN_SYSTEMS: Hiroc System did not exist. Has been generated retroactively");
        }
        else
            log.info("JDP_RETROGEN_SYSTEMS: Hiroc System already exists. No action taken");

        //Generate Dory
        if (sector.getStarSystem("jdp_Dory") == null) {
            new jdp_Dory().generate(sector);
            log.info("JDP_RETROGEN_SYSTEMS: Dory System did not exist. Has been generated retroactively");
        }
        else
            log.info("JDP_RETROGEN_SYSTEMS: Dory System already exists. No action taken");

        //Generate Grin
        if (sector.getStarSystem("jdp_Grin") == null) {
            new jdp_Grin().generate(sector);
            log.info("JDP_RETROGEN_SYSTEMS: Grin System did not exist. Has been generated retroactively");
        }
        else
            log.info("JDP_RETROGEN_SYSTEMS: Grin System already exists. No action taken");

        //Events
        ThemeGenContext context = new ThemeGenContext();//These values aren't used.
        new jdp_MiscEventGenerator().generateForSector(context, 1f);

        //People
        //Come last so they can be added to generated markets
        jdp_People.jdp_createMiscCharacters();//Creates All Characters
    }
}