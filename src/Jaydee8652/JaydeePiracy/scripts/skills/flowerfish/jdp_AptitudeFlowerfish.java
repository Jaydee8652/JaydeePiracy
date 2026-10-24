package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;

import Jaydee8652.JaydeePiracy.plugins.jdp_lunaSettings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaSettings.LunaSettings;
import second_in_command.SCData;
import second_in_command.specs.SCAptitudeSection;
import second_in_command.specs.SCBaseAptitudePlugin;


import java.awt.*;
import java.util.Objects;

public class jdp_AptitudeFlowerfish extends SCBaseAptitudePlugin {
    public static Color JDP_FLOWERFISH_COLOUR = new Color(187,36,55,255);

    @Override
    public void addCodexDescription(TooltipMakerAPI tooltip) {
        tooltip.addPara("The Flowerfish aptitiude is added by JaydeePiracy. It is favoured by agents of the Flowerfish Network, a secretive cabal operating in Westernesse. The aptitude is focused on officers and "+
                        "powerful synergistic bonuses.",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "Flowerfish", "favoured by agents of the Flowerfish Network");
        tooltip.addSpacer(10f);
        tooltip.addPara("Flowerfish favours flexible formations and ships with versatile stat and weapon spreads to achieve maximum combat effectiveness.",
                0f, Misc.getTextColor(), Misc.getHighlightColor());
    }

    @Override
    public String getOriginSkillId() {
        return "jdp_flowerfishOurStrategicAdvantage";
    }

    @Override
    public void createSections() {
        SCAptitudeSection section1 = new SCAptitudeSection(true, 0, "technology1");
        section1.addSkill("jdp_flowerfishUrbanCamo");
        section1.addSkill("jdp_flowerfishSupersymmetry");
        section1.addSkill("jdp_flowerfishWithTheNetwork");
        addSection(section1);

        SCAptitudeSection section2 = new SCAptitudeSection(true, 2, "technology3");
        section2.addSkill("jdp_flowerfishFirstSecondThird");
        section2.addSkill("jdp_flowerfishPunchDownClawUp");
        section2.addSkill("jdp_flowerfishQuadrupolarCoupling");
        section2.addSkill("jdp_flowerfishPerpetualPursuit");
        addSection(section2);

        SCAptitudeSection section3 = new SCAptitudeSection(false, 4, "technology4");
        section3.addSkill("jdp_flowerfishPersonalDiscretion");
        section3.addSkill("jdp_flowerfishAccountingOfBurdens");
        addSection(section3);
    }

    @Override
    public Float getNPCFleetSpawnWeight(SCData scData, CampaignFleetAPI campaignFleetAPI) {
        if (jdp_lunaSettings.jdp_experimentalFlowerfish().equals(true) && Objects.equals(campaignFleetAPI.getFaction().getId(), "jdp_flowerfish")) return Float.MAX_VALUE;
        return 0f;
    }

    public static Boolean isHumanOfficer(ShipAPI ship) {
        PersonAPI captain = ship.getCaptain();
        return (captain != null && !captain.isAICore() && !captain.isDefault() && !captain.hasTag("rat_neuro_shard"));
    }
}
