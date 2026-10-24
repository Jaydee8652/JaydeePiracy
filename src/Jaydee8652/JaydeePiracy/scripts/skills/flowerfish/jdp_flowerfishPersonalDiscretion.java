package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import Jaydee8652.JaydeePiracy.hullmods.jdp_mercenaryRelay;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.plog.OfficerSkillGainRecord;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.impl.campaign.skills.*;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CharacterStats;
import com.fs.starfarer.rpg.Person;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import org.magiclib.*;

import java.awt.*;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getOfficers;


public class jdp_flowerfishPersonalDiscretion extends SCBaseSkillPlugin {

    public static float RATE_MALUS = 75;


    public static class PersonalDiscretionOfficerEffectData {
        OfficerDataAPI data;
        int newLevel;
        List<String> removeSkills = new ArrayList<String>();
        List<String> removeElite = new ArrayList<String>();
        FleetMemberAPI member;

        public boolean hasChanges() {
            return data.getPerson().getStats().getLevel() != newLevel ||
                    !removeSkills.isEmpty() || !removeElite.isEmpty();
        }
    }

    public static Boolean addAndCheckTag(CampaignFleetAPI fleet, String tag) {
        if (fleet.hasTag(tag)) return true;

        fleet.addTag(tag);
        return false;
    }

    @Override
    public String getAffectsString() {
        return "all officers";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltip) {
        tooltip.addPara("+1 to maximum level of officers under your command", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("+1 to maximum number of elite skills for officers under your command", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("*If this skill is unassigned officers over the level limit will have excess skills made inactive", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "");
        tooltip.addSpacer(10f);

        tooltip.addPara("Affects: fleet", 0f, Misc.getGrayColor(), Misc.getBasePlayerColor(), "fleet");
        tooltip.addSpacer(10f);

        tooltip.addPara("Command point recovery rate is reduced by " + Math.round(100 - RATE_MALUS) + "%%.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addSpacer(10f);

        tooltip.addPara("\"I am confident in my abilities. I would not make such a mistake.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltip.addPara("  -Internal Trial Transcript, Maki Insubordination Case", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltip.addSpacer(10f);
    }

    @Override
    public void onActivation(SCData data) {
        data.getCommander().getStats().getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).modifyFlat("jdp_flowerfishPersonalDiscretion", 1f);
        data.getCommander().getStats().getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).modifyFlat("jdp_flowerfishPersonalDiscretion", 1f);
        data.getCommander().getStats().getDynamic().getMod(Stats.COMMAND_POINT_RATE_COMMANDER).modifyMult("jdp_flowerfishPersonalDiscretion", (RATE_MALUS / 100f));

        if (!data.isNPC()) {
            for (PersonAPI officer : getOfficers()) {
                Object mapObj = officer.getMemoryWithoutUpdate().get("$jdp_flowerfishPersonalDiscretion_inactive");

                if (!(mapObj instanceof HashMap)) continue;
                HashMap<String, Float> map = (HashMap<String, Float>) mapObj;

                for (HashMap.Entry<String, Float> entry : map.entrySet()) {
                    String skill = entry.getKey();
                    Float level = entry.getValue();
                    officer.getStats().setSkillLevel(skill, level);
                    PlaythroughLog.getInstance().addOfficerSkillRecord(new OfficerSkillGainRecord(officer.getId(), skill, level >= 2));
                }

                officer.getMemoryWithoutUpdate().set("$jdp_flowerfishPersonalDiscretion_inactive", null);

                officer.getStats().setSkillLevel("jdp_inactive", 0f);
            }
        }

        // Increase officer levels and give skills
        if (data.isNPC() && !addAndCheckTag(data.getFleet(),"jdp_flowerfishPersonalDiscretion_update")) {
            int levels = 1;

            List<FleetMemberAPI> membersWithOfficers = data.getFleet().getFleetData().getMembersListCopy().stream().filter(
                    m -> m.getCaptain() != null
                            && !m.getCaptain().isDefault()
                            && !Misc.isAutomated(m)
                            && !Global.getSector().getImportantPeople().containsPerson(m.getCaptain()))
                    .toList();

            OfficerLevelupPlugin plugin = (OfficerLevelupPlugin) Global.getSettings().getPlugin("officerLevelUp");
            for (FleetMemberAPI member : membersWithOfficers) {
                PersonAPI captain = member.getCaptain();
                if (captain.hasTag("jdp_flowerfishPersonalDiscretion_increased_lv")) continue; // Do not increase the level multiple times
                captain.addTag("jdp_flowerfishPersonalDiscretion_increased_lv");

                for (int level = 0; level < levels; level++) {
                    captain.getStats().setLevel(captain.getStats().getLevel() + 1);

                    List<String> skills = plugin.pickLevelupSkills(captain, new Random());
                    if (!skills.isEmpty()) {
                        String pick = skills.get(new Random().nextInt(skills.size()));
                        captain.getStats().increaseSkill(pick);
                    }
                }
            }
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        data.getCommander().getStats().getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).unmodify("jdp_flowerfishPersonalDiscretion");
        data.getCommander().getStats().getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).unmodify("jdp_flowerfishPersonalDiscretion");
        data.getCommander().getStats().getDynamic().getMod(Stats.COMMAND_POINT_RATE_COMMANDER).unmodify("jdp_flowerfishPersonalDiscretion");

        if (!data.isNPC()) {
            int maxLevel = Math.round(Global.getSector().getPlayerPerson().getStats().getDynamic().getMod(Stats.OFFICER_MAX_LEVEL_MOD).computeEffective(Global.getSettings().getFloat("officerMaxLevel")));
            int maxElite = Math.round(Global.getSector().getPlayerPerson().getStats().getDynamic().getMod(Stats.OFFICER_MAX_ELITE_SKILLS_MOD).computeEffective(Global.getSettings().getFloat("officerMaxEliteSkills")));

            for (OfficerDataAPI officerData : Global.getSector().getPlayerFleet().getFleetData().getOfficersCopy()) {
                var officer = officerData.getPerson();
                if (officer.getMemoryWithoutUpdate().contains(MemFlags.OFFICER_MAX_LEVEL) || officer.getMemoryWithoutUpdate().contains(MemFlags.EXCEPTIONAL_SLEEPER_POD_OFFICER)) continue;
                var stats = officer.getStats();
                var map = new HashMap<String, Float>();

                var skills = stats.getSkillsCopy();
                var elite_skills = skills.stream().filter(s -> s.getLevel() >= 2).toList();

                PersonalDiscretionOfficerEffectData effect = new PersonalDiscretionOfficerEffectData();
                effect.data = officerData;
                effect.newLevel = stats.getLevel();

                if (stats.getLevel() > maxLevel) {
                    final List<String> skillsInOrderOfGain = new ArrayList<String>();
                    for (OfficerSkillGainRecord rec : PlaythroughLog.getInstance().getOfficerSkillsLearned()) {
                        if (rec.getPersonId().equals(officer.getId()) && !rec.isElite() && stats.getSkillLevel(rec.getSkillId()) > 0) {
                            skillsInOrderOfGain.add(rec.getSkillId());
                        }
                    }
                    Collections.reverse(skillsInOrderOfGain);

                    int skillsToRemove = stats.getLevel() - maxLevel;
                    for (int i = 0; i < skillsToRemove && !skillsInOrderOfGain.isEmpty(); i++) {
                        effect.removeSkills.add(skillsInOrderOfGain.remove(0));
                    }
                    if (!effect.removeSkills.isEmpty()) {
                        effect.newLevel = maxLevel;
                    }
                }

                int numElite = elite_skills.size();
                if (numElite > maxElite) {
                    List<String> eliteInOrderOfGain = new ArrayList<String>();
                    for (OfficerSkillGainRecord rec : PlaythroughLog.getInstance().getOfficerSkillsLearned()) {
                        if (rec.getPersonId().equals(officer.getId()) && rec.isElite() && stats.getSkillLevel(rec.getSkillId()) >= 2) {
                            eliteInOrderOfGain.add(rec.getSkillId());
                        }
                    }
                    Collections.reverse(eliteInOrderOfGain);

                    int eliteToRemove = numElite - maxElite;
                    /*if (effect.removeSkills.size() == eliteToRemove) {
                        effect.removeElite = effect.removeSkills;
                        continue;
                    }
                    if (!effect.removeSkills.isEmpty()) {
                        eliteInOrderOfGain = eliteInOrderOfGain.stream().sorted(Comparator.comparing(e -> effect.removeSkills.contains(e))).toList();
                    }*/
                    for (int i = 0; i < eliteToRemove && !eliteInOrderOfGain.isEmpty(); i++) {
                        effect.removeElite.add(eliteInOrderOfGain.remove(0));
                    }
                }

                if (effect.hasChanges()) {
                    for (String id : effect.removeElite) {
                        stats.setSkillLevel(id, 1);
                        PlaythroughLog.getInstance().removeOfficerSkillRecord(officer.getId(), id, true);
                    }
                    for (String id : effect.removeSkills) {
                        stats.setSkillLevel(id, 0);
                        PlaythroughLog.getInstance().removeOfficerSkillRecord(officer.getId(), id, false);
                        PlaythroughLog.getInstance().removeOfficerSkillRecord(officer.getId(), id, true);
                        map.put(id, 1f);
                    }
                    if (effect.newLevel != stats.getLevel()) {
                        officer.getMemoryWithoutUpdate().set("$jdp_flowerfishPersonalDiscretion_inactive", map);
                        officer.getStats().setSkillLevel("jdp_inactive", 1f);
                    }
                }
            }
        }
    }
}