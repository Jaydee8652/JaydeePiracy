package Jaydee8652.JaydeePiracy.scripts.proficiencies;

import Jaydee8652.JaydeePiracy.scripts.skills.jdp_counternarrativeConstruction;
import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_FactionBarElement;
import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_LineElement;
import Jaydee8652.JaydeeProficiencies.specialists.plugins.jdp_BaseProficiencyPlugin;
import Jaydee8652.JaydeeProficiencies.specialists.data.SpecialistSkillSection;
import Jaydee8652.JaydeeProficiencies.specialists.data.jdp_PanelData;
import Jaydee8652.JaydeeProficiencies.ui.jdp_ProgressBar;
import Jaydee8652.JaydeeProficiencies.ui.jdp_SpecialistTabInjector;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_Stats;
import Jaydee8652.JaydeeProficiencies.utils.refl.ui.ListenerFactory;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import lunalib.lunaUI.elements.LunaElement;

import java.awt.*;
import java.util.*;

import static Jaydee8652.JaydeePiracy.scripts.skills.jdp_counternarrativeConstruction.claimFabricationTarget;
import static Jaydee8652.JaydeeProficiencies.patch.MarketCommandPatch.CLAIM_INCREMENT;
import static Jaydee8652.JaydeeProficiencies.patch.MarketCommandPatch.getClaim;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.createCargoStack;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IntelUtils.PICKER_PANEL_OUTER_GAP;


public class jdp_ProficiencyInstrument extends jdp_BaseProficiencyPlugin {
    @Override
    public void addDescription(PersonAPI person, TooltipMakerAPI tooltip, Boolean isHireScreen) {
        tooltip.addPara(person.getName().getFullName() + " is an Instrument, a proficiency focused on subterfuge, suppressing dissent and the clandestine use of Artificial Intelligence.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Instrument");
        tooltip.addSpacer(10f);
        tooltip.addPara("Most skills are not immediately numerically powerful, but have strong synergies with each other. In addition, useful faction level bonuses allow for new strategies.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "faction level bonuses");
    }

    @Override
    public void createMentorSections() {
        SpecialistSkillSection section;

        section = new SpecialistSkillSection(0);
        section.addSkill(Skills.INDUSTRIAL_PLANNING);
        section.addSkill(jdp_Skills.JDP_COINTEL);
        addSection(section, true);

        section = new SpecialistSkillSection(0);
        section.addSkill(jdp_Skills.JDP_RECIV);
        section.addSkill(jdp_Skills.JDP_RADICAL);
        addSection(section, false);

        section = new SpecialistSkillSection(3);
        section.addSkill(Skills.HYPERCOGNITION, createCargoStack(CargoItemType.RESOURCES, Commodities.ALPHA_CORE));
        section.addSkill(jdp_Skills.JDP_MENTORSHIP);
        section.setMutuallyExclusive();
        addSection(section, false);

        section = new SpecialistSkillSection(4);
        section.addSkill(jdp_Skills.JDP_AI);
        section.addSkill(jdp_Skills.JDP_ATROCITY);
        section.setMutuallyExclusive();
        addSection(section, false);
    }

    @Override
    public void createMenteeSections() {
        SpecialistSkillSection section;

        section = new SpecialistSkillSection(0);
        section.addSkill(jdp_Skills.JDP_RECIV);
        section.addSkill(jdp_Skills.JDP_HACKING);
        section.addSkill(jdp_Skills.JDP_MARINETRAINING);
        addSection(section, false);
    }

    public static float claimPanelHeight = 30f;

    @Override
    public void addFooter(PersonAPI person, jdp_PanelData panelData, TooltipMakerAPI tooltip) {
        tooltip.addPara("The Counternarrative Construction skill allows claims to be fabricated against other factions, a claim allows a single Saturation Bombardment without repercussions. Claims can only be fabricated against one faction at a time, and only one claim against each faction can be stored.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Counternarrative Construction", "Saturation Bombardment");
        tooltip.addSpacer(10f);
        tooltip.addPara("Each claim used against a faction increases the effort required to fabricate another by %s.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round((CLAIM_INCREMENT - 1) * 100) + "%");

        var panel = panelData.panel();
        var width = panelData.width();

        var scrollerPanel = panel.createCustomPanel(width, 160, null);
        panel.addComponent(scrollerPanel).inBL(0f, PICKER_PANEL_OUTER_GAP);

        TooltipMakerAPI element = scrollerPanel.createUIElement(width, 160, true);

        Set<FactionAPI> set = new HashSet<>(Global.getSector().getEconomy().getMarketsCopy().stream().map(MarketAPI::getFaction).toList());
        set.remove(Global.getSector().getPlayerFaction());

        var factions = new ArrayList<>(set);
        factions.sort(Comparator.comparingInt(f -> getClaim(f).getProgress()));
        Collections.reverse(factions);

        jdp_FactionBarElement previous = null;
        for (FactionAPI faction : factions) {
            var claim = getClaim(faction);
            var label = new jdp_ProgressBar.jdp_LabelData(tooltip.createLabel(claim.getProgress() + " / " + claim.getGoal(), Misc.getTextColor()), Font.SERIF);

            var bar = new jdp_FactionBarElement(faction.getId(), (float) claim.getProgress() / claim.getGoal(), label, new ListenerFactory.onClickScript() {
                @Override
                public void onClick(InputEventAPI event, Object object) {
                    jdp_FactionBarElement bar = (jdp_FactionBarElement) object;

                    if (event.isRMBEvent()) {
                        Global.getSoundPlayer().playUISound("ui_char_decrease_skill", 1f, 1f);
                        Global.getSector().getMemoryWithoutUpdate().unset(jdp_counternarrativeConstruction.CLAIM_FABRICATION_TARGET);
                    } else {
                        if (Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().contains(jdp_counternarrativeConstruction.HAS_COUNTERNARRATIVE)) {
                            Global.getSoundPlayer().playUISound("ui_button_pressed", 1f, 1f);
                            Global.getSector().getMemoryWithoutUpdate().set(jdp_counternarrativeConstruction.CLAIM_FABRICATION_TARGET, faction.getId());
                        } else {
                            bar.playSound("ui_char_can_not_increase_skill_or_aptitude", 1f, 1f);
                        }
                    }
                }

                @Override
                public void checkCriteria(Object object) {
                    jdp_FactionBarElement bar = (jdp_FactionBarElement) object;
                    if (faction != claimFabricationTarget()) {
                        if (!Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().contains(jdp_counternarrativeConstruction.HAS_COUNTERNARRATIVE)) {
                            if (bar.getHoverFade() > 0.3f) bar.setHoverFade(0.3f);
                        } else {
                            if (bar.getHoverFade() > 0.9f) bar.setHoverFade(0.9f);
                            if (bar.getHoverFade() < 0.35f) bar.setHoverFade(0.35f);
                        }
                    } else {
                        if (!Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().contains(jdp_counternarrativeConstruction.HAS_COUNTERNARRATIVE)) {
                            if (bar.getHoverFade() < 0.5f) bar.setHoverFade(0.5f);
                            if (bar.getHoverFade() > 0.6f) bar.setHoverFade(0.6f);
                        } else {
                            if (bar.getHoverFade() < 0.9f) bar.setHoverFade(0.9f);
                        }
                    }
                }

            }, element, width - PICKER_PANEL_OUTER_GAP * 2 + 6f, claimPanelHeight);

            tooltip.addTooltipTo(new jdp_counternarrativeConstruction.jdp_counternarrativeTooltipCreator(person, faction), bar.getElementPanel(), TooltipMakerAPI.TooltipLocation.BELOW);

            if (previous != null) {
                bar.getPosition().belowLeft(previous.getElementPanel(), 3f);
            } else {
                bar.getPosition().inTL(PICKER_PANEL_OUTER_GAP,3f);
            }

            previous = bar;
            element.addSpacer(3f);
        }
        scrollerPanel.addUIElement(element).inBL(0f, 0f);
    }

    @Override
    public long[] getXPThresholds(Boolean apprentice) {
        if (apprentice) return new long[]{0L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L, 45000L};
        return new long[]{
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
        };
    }
}