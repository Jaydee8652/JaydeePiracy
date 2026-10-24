package Jaydee8652.JaydeePiracy.scripts.proficiencies

import Jaydee8652.JaydeePiracy.utils.jdp_Skills
import Jaydee8652.JaydeeProficiencies.specialists.plugins.jdp_BaseProficiencyPlugin
import Jaydee8652.JaydeeProficiencies.specialists.data.SpecialistSkillSection
import Jaydee8652.JaydeeProficiencies.specialists.data.jdp_PanelData
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.impl.campaign.ids.Skills
import Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.createCargoStack;

class jdp_ProficiencyInstrument : jdp_BaseProficiencyPlugin() {
    override fun addDescription(person: PersonAPI, tooltip: TooltipMakerAPI, isHireScreen: Boolean) {
        tooltip.addPara(person.name.fullName + " is an Instrument, a proficiency focused on subterfuge, suppressing dissent and the clandestine use of Artificial Intelligence.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Instrument")
        tooltip.addSpacer(10f)
        tooltip.addPara("Most skills are not immediately numerically powerful, but have strong synergies with each other. In addition, useful faction level bonuses allow for new strategies.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "faction level bonuses")

    }

    override fun addFooter(p0: PersonAPI?, p1: jdp_PanelData?, p2: TooltipMakerAPI?) {
    }

    //Skills for the main Specialist
    override fun createMentorSections() {
        var section: SpecialistSkillSection

        section = SpecialistSkillSection(0)
        section.addSkill(Skills.INDUSTRIAL_PLANNING)
        section.addSkill(jdp_Skills.JDP_COINTEL)
        addSection(section, true)

        section = SpecialistSkillSection(0)
        section.addSkill(jdp_Skills.JDP_HACKING)
        section.addSkill(jdp_Skills.JDP_RECIV)
        section.addSkill(jdp_Skills.JDP_ATROCITY)
        section.addSkill(jdp_Skills.JDP_MARINETRAINING)
        addSection(section)

        section = SpecialistSkillSection(5)
        section.addSkill(jdp_Skills.JDP_AI)
        section.addSkill(Skills.HYPERCOGNITION, createCargoStack(CargoItemType.RESOURCES, Commodities.ALPHA_CORE as Object))
        section.setMutuallyExclusive()
        addSection(section)
    }

    override fun createMenteeSections() {
    }

    //Override XP Thresholds for levels if necessary
    //As this proficiency starts at level 2 the curve is adjusted
    override fun getXPThresholds(apprentice: Boolean): LongArray {
        return longArrayOf(
            0,      // level  1
            0,      // level  2
            12000,  // level  3
            25000,  // level  4
            35000,  // level  5
            45000,  // level  6
            55000,  // level  7
            65000,  // level  8
            75000,  // level  8
            90000,  // level 10
        )
    }
}