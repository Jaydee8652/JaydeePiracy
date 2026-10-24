package Jaydee8652.JaydeePiracy.scripts.proficiencies

import Jaydee8652.JaydeePiracy.utils.jdp_Skills
import Jaydee8652.JaydeeProficiencies.specialists.plugins.jdp_BaseProficiencyPlugin
import Jaydee8652.JaydeeProficiencies.specialists.data.SpecialistSkillSection
import Jaydee8652.JaydeeProficiencies.specialists.data.jdp_PanelData
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class jdp_ProficiencyTerraformer : jdp_BaseProficiencyPlugin() {
    override fun addDescription(person: PersonAPI, tooltip: TooltipMakerAPI, isHireScreen: Boolean) {
        tooltip.addPara(person.name.fullName + " is a Terraformer, a proficiency focused almost entirely on manipulating planetary conditions.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Terraformer")
        tooltip.addSpacer(10f)
        tooltip.addPara("Most skills have side-effects or drawbacks but the ability to improve, deplete and shift conditions can provide powerful boosts to production or make room for Colony Items. ", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Colony Items")
    }

    override fun addFooter(p0: PersonAPI?, p1: jdp_PanelData?, p2: TooltipMakerAPI?) {
    }

    //Skills for the main Specialist
    override fun createMentorSections() {
        var section: SpecialistSkillSection

        section = SpecialistSkillSection(0)
        section.addSkill(Skills.INDUSTRIAL_PLANNING)
        addSection(section, true)

        section = SpecialistSkillSection(0)
        section.addSkill(jdp_Skills.JDP_MENTORSHIP)
        section.addSkill(jdp_Skills.JDP_TECHMORTICIAN)
        section.addSkill(jdp_Skills.JDP_ATOMICEXTRACTION)
        addSection(section)

        section = SpecialistSkillSection(3)
        section.addSkill(jdp_Skills.JDP_ACCELERATORTECHNICIAN)
        section.addSkill(jdp_Skills.JDP_ATMOSPHAGEDISPERSAL)
        addSection(section)

        section = SpecialistSkillSection(4)
        section.addSkill(jdp_Skills.JDP_ECOSPHEREMANAGEMENT)
        section.addSkill(jdp_Skills.JDP_SOLAR)
        section.setMutuallyExclusive()
        addSection(section)
    }

    //Skills for the apprentice of the Specialist, only used with the Mentorship skill
    override fun createMenteeSections() {
        var section: SpecialistSkillSection

        section = SpecialistSkillSection(0)
        section.addSkill(jdp_Skills.JDP_TECHMORTICIAN)
        section.addSkill(jdp_Skills.JDP_ATOMICEXTRACTION)
        section.addSkill(jdp_Skills.JDP_ATMOSPHAGEDISPERSAL)
        addSection(section)

        section = SpecialistSkillSection(2)
        section.addSkill(jdp_Skills.JDP_ACCELERATORTECHNICIAN)
        addSection(section)
    }
}