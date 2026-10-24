package Jaydee8652.JaydeePiracy.scripts.proficiencies;

import Jaydee8652.JaydeePiracy.utils.jdp_Items;
import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import Jaydee8652.JaydeeProficiencies.specialists.data.jdp_PanelData;
import Jaydee8652.JaydeeProficiencies.specialists.plugins.jdp_BaseProficiencyPlugin;
import Jaydee8652.JaydeeProficiencies.specialists.data.SpecialistSkillSection;
import Jaydee8652.JaydeeProficiencies.specialists.data.jdp_Specialist;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_FactionUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.createCargoStack;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IntelUtils.PICKER_PANEL_OUTER_GAP;

public class jdp_ProficiencyDancer extends jdp_BaseProficiencyPlugin {
    // Skills for the main Specialist
    @Override
    public void createMentorSections() {
        SpecialistSkillSection section;

        section = new SpecialistSkillSection(0);
        section.addSkill(jdp_Skills.JDP_CEREBRALSYMBIANCE);
        addSection(section, true);

        section = new SpecialistSkillSection(0);
        section.addSkill(jdp_Skills.JDP_DEEPEUTECTICGRID);
        section.addSkill(jdp_Skills.JDP_PERFECTPREDICTION);
        addSection(section, false);

        section = new SpecialistSkillSection(2);
        section.addSkill(jdp_Skills.JDP_DANCING);
        section.addSkill(jdp_Skills.JDP_MATERIELSUBSUMPTION);
        section.addSkill(jdp_Skills.JDP_APPLIEDCONTRAVENTION);
        addSection(section, false);

        section = new SpecialistSkillSection(4);
        section.addSkill(jdp_Skills.JDP_TOTALINTERNALREFLECTION, createCargoStack(CargoItemType.RESOURCES, jdp_Items.JDP_CORE_OMEGA));
        section.addSkill(jdp_Skills.JDP_MENTORSHIP, createCargoStack(CargoItemType.RESOURCES, jdp_Items.JDP_CORE_OMEGA));
        section.setMutuallyExclusive();
        addSection(section, false);
    }

    @Override
    public void createMenteeSections() {
        SpecialistSkillSection section;

        section = new SpecialistSkillSection(0);
        section.addSkill(jdp_Skills.JDP_DEEPEUTECTICGRID);
        section.addSkill(jdp_Skills.JDP_PERFECTPREDICTION);
        addSection(section, false);

        section = new SpecialistSkillSection(1);
        section.addSkill(jdp_Skills.JDP_DANCING);
        section.addSkill(jdp_Skills.JDP_MATERIELSUBSUMPTION);
        section.addSkill(jdp_Skills.JDP_APPLIEDCONTRAVENTION);
        section.setMutuallyExclusive();
        addSection(section, false);
    }

    @Override
    public void addDescription(PersonAPI person, TooltipMakerAPI tooltip, Boolean isHireScreen) {
        tooltip.addPara(person.getName().getFullName() + " is a Dancer, a proficiency focused on esoteric and powerful combat abilities. It is capable of perfectly integrating with fleet operations.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Dancer");
        tooltip.addSpacer(10f);
        tooltip.addPara("Once the connection is sufficiently deepened, the core can be imbibed, granting you a fraction of its strength or knowledge in exchange.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "granting you a fraction of its strength or knowledge in exchange");
    }

    @Override
    public void addFooter(PersonAPI personAPI, jdp_PanelData panelData, TooltipMakerAPI tooltip) {
        tooltip.addPara("This proficiency can only gain up to a maximum of %s experience from combat against a faction. Once this limit is reached, further engagements with the faction will no longer grant experience.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round(FACTION_XP_CAP) + "", "Mentor", "Apprentice");
        tooltip.addSpacer(10f);

        tooltip.addPara("This pool is shared between the Mentor and all Apprentices", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round(FACTION_XP_CAP) + "", "Mentor", "Apprentices");
        tooltip.addSpacer(10f);

        var panel = panelData.panel();
        var width = panelData.width();

        Map<FactionAPI, Float> data = (Map<FactionAPI, Float>) Global.getSector().getMemoryWithoutUpdate().get(EXPERIENCE_CONTRIBUTIONS);
        if (data == null) data = new HashMap<>();

        Map<FactionAPI, Float> temp = new LinkedHashMap<>();
        data.entrySet().stream().sorted((k1, k2) -> k1.getValue().compareTo(k2.getValue())).forEach(k -> temp.put(k.getKey(), k.getValue()));
        data = temp;

        var scrollerPanel = panel.createCustomPanel(width, 160, null);
        panel.addComponent(scrollerPanel).inBL(0f, PICKER_PANEL_OUTER_GAP);

        TooltipMakerAPI element = scrollerPanel.createUIElement(width, 160, true);

        float modW = 300f;
        float nameW = width - modW - 5f - PICKER_PANEL_OUTER_GAP;

        element.beginTable(this.spec.getColor(), jdp_FactionUtils.createDarkUIColor(this.spec.getColor()), jdp_FactionUtils.createBrightUIColor(this.spec.getColor()), 20f, true, true, new Object [] {"Faction", nameW, "Experience Gained", modW});
        for (FactionAPI faction : data.keySet()) {
            var value = data.get(faction);
            var color = Misc.getTextColor();

            if (Math.abs(FACTION_XP_CAP - value) <= 0) color = Misc.getGrayColor();
            element.addRow(faction.getColor(), faction.getDisplayName(), color, Math.round(value) + "");
        }
        element.addTable("No Experience Gained", 0, 10f);

        scrollerPanel.addUIElement(element).inBL(0f, 0f);
    }

    static float FACTION_XP_CAP = 15000f;
    static String EXPERIENCE_CONTRIBUTIONS = "$jdp_omegaExperienceContributions";
    @Override
    public Float calculateExperience(jdp_Specialist specialist, Float experience, FleetEncounterContext context) {
        Map<FactionAPI, Float> data = (Map<FactionAPI, Float>) Global.getSector().getMemoryWithoutUpdate().get(EXPERIENCE_CONTRIBUTIONS);
        if (data == null) data = new HashMap<>();

        var total = 0f;
        Map<FactionAPI, Float> contributions = new HashMap<>();
        for (var fleet: context.getBattle().getNonPlayerSideSnapshot()) {
            var faction = fleet.getFaction();

            var contribution = 0f;
            var members = fleet.getFleetData().getSnapshot().stream().map(m -> m.getId()).toList();
            var cas = context.getDataFor(fleet).getOwnCasualties().stream().filter(c -> members.contains(c.getMember().getId())).toList();

            for (var member : cas) {
                float fp = member.getMember().getFleetPointCost();

                fp *= 1f + (member.getMember().getCaptain().getStats().getLevel() / 5f);
                fp *= context.computePlayerContribFraction() * Math.max(1f, context.getDifficulty());
                contribution += fp;
            }
            contributions.put(faction, contributions.getOrDefault(faction, 0f) + contribution);
            total += contribution;
        }

        var gained = 0f;
        for (var faction : contributions.keySet()) {
            var fraction = contributions.get(faction) / total;
            var xp = Math.min((FACTION_XP_CAP - data.getOrDefault(faction, 0f)), fraction * experience);
            data.put(faction, data.getOrDefault(faction, 0f) + xp);
            gained += xp;
        }

        Global.getSector().getMemoryWithoutUpdate().set(EXPERIENCE_CONTRIBUTIONS, data);
        return gained;
    }

    @Override
    public long[] getXPThresholds(Boolean apprentice) {
        if (apprentice) return new long[]{0L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L};
        return new long[]{0L, 12000L, 25000L, 35000L, 45000L, 55000L, 65000L, 75000L, 90000L, 100000L};
    }
}