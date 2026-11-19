package Jaydee8652.JaydeePiracy.scripts.proficiencies;

import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import Jaydee8652.JaydeeProficiencies.specialists.plugins.jdp_BaseProficiencyPlugin;
import Jaydee8652.JaydeeProficiencies.specialists.data.SpecialistSkillSection;
import Jaydee8652.JaydeeProficiencies.specialists.data.jdp_PanelData;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.createCargoStack;

public class jdp_ProficiencyTerraformer extends jdp_BaseProficiencyPlugin {
    @Override
    public void addDescription(PersonAPI person, TooltipMakerAPI tooltip, Boolean isHireScreen) {
        tooltip.addPara(person.getName().getFullName() + " is a Terraformer, a proficiency focused almost entirely on manipulating planetary conditions.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Terraformer");
        tooltip.addSpacer(10f);
        tooltip.addPara("Most skills have side-effects or drawbacks but the ability to improve, deplete and shift conditions can provide powerful boosts to production or make room for Colony Items. ", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Colony Items");
    }

    @Override
    public void createMentorSections() {
        SpecialistSkillSection section;

        section = new SpecialistSkillSection(0);
        section.addSkill(Skills.INDUSTRIAL_PLANNING);
        addSection(section, true);

        section = new SpecialistSkillSection(0);
        section.addSkill(jdp_Skills.JDP_MENTORSHIP);
        section.addSkill(jdp_Skills.JDP_TECHMORTICIAN);
        section.addSkill(jdp_Skills.JDP_ATOMICEXTRACTION);
        addSection(section);

        section = new SpecialistSkillSection(3);
        section.addSkill(jdp_Skills.JDP_ACCELERATORTECHNICIAN);
        section.addSkill(jdp_Skills.JDP_ATMOSPHAGEDISPERSAL);
        addSection(section);

        section = new SpecialistSkillSection(4);
        section.addSkill(jdp_Skills.JDP_ECOSPHEREMANAGEMENT);
        section.addSkill(jdp_Skills.JDP_SOLAR);
        section.setMutuallyExclusive();
        addSection(section);
    }

    @Override
    public void createMenteeSections() {
        SpecialistSkillSection section;

        section = new SpecialistSkillSection(0);
        section.addSkill(jdp_Skills.JDP_TECHMORTICIAN);
        section.addSkill(jdp_Skills.JDP_ATOMICEXTRACTION);
        section.addSkill(jdp_Skills.JDP_ATMOSPHAGEDISPERSAL);
        addSection(section);

        section = new SpecialistSkillSection(2);
        section.addSkill(jdp_Skills.JDP_ACCELERATORTECHNICIAN);
        addSection(section);
    }

    @Override
    public void addFooter(PersonAPI personAPI, jdp_PanelData jdpPanelData, TooltipMakerAPI tooltipMakerAPI) {

    }
}