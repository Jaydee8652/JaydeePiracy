package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import Jaydee8652.JaydeePiracy.hullmods.jdp_mercenaryRelay;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.skills.*;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.loading.SkillSpec;
import com.fs.starfarer.loading.specs.PlanetSpec;
import com.fs.starfarer.rpg.Person;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import org.magiclib.*;

import java.awt.*;
import java.lang.management.MemoryUsage;
import java.util.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.*;
import static com.fs.starfarer.api.util.Misc.IS_MERCENARY;


public class jdp_flowerfishFirstSecondThird extends SCBaseSkillPlugin {
    public static int EFFECT_RANGE = 1700;
    public static Object STATUS_KEY = new Object();


    @Override
    public String getAffectsString() {
        return "all ships in the fleet";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("The non-elite versions of player and mercenary officer elite skills are applied to allied ships within " + EFFECT_RANGE +  " units", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("*Automated ships cannot inherit skills", 0f, Misc.getGrayColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addSpacer(10f);

        tooltipMakerAPI.addPara("\"They look to us, for we are their betters. My job is to ensure that will change.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("  -Ailmar Reformatory Director Kabayaki", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltipMakerAPI.addSpacer(10f);
    }

    @Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {
        super.advanceInCombat(data, ship, amount);
        //Not if the ship is dead
        if (!ship.isAlive()) return;
        if (amount <= 0f) return;
        
        if (jdp_AptitudeFlowerfish.isHumanOfficer(ship) && (ship.getCaptain().isPlayer()) || Misc.isMercenary(ship.getCaptain())) {
            CombatEngineAPI engine = Global.getCombatEngine();

            for (ShipAPI other : engine.getShips()) {
                if (ship == other || other.getOwner() != ship.getOwner() || other.isHulk()) continue;

                //Cannot trigger if target has no officer
                if (other.getCaptain() == null) continue;

                //Cannot trigger if target has an AI core
                if (ship.getCaptain().isAICore() || ship.getVariant().hasHullMod(HullMods.AUTOMATED)) continue;

                ArrayList<String> original_skills = new ArrayList<String>();
                for (MutableCharacterStatsAPI.SkillLevelAPI original : other.getCaptain().getStats().getSkillsCopy()) {
                    if (original.getLevel() > 0f) {
                        original_skills.add(original.getSkill().getId());
                    }
                }

                float dist = Misc.getDistance(ship.getLocation(), other.getLocation());

                for (MutableCharacterStatsAPI.SkillLevelAPI skillLevel : ship.getCaptain().getStats().getSkillsCopy()) {
                    String skill = skillLevel.getSkill().getId();
                    SkillSpecAPI spec = Global.getSettings().getSkillSpec(skill);


                    if ((skillLevel.getLevel() >= 2f && skillLevel.getSkill().isElite()) && !original_skills.contains(skill)) {
                        List<SkillSpecAPI.SkillEffectSpecAPI> effects = spec.getEffectsAPI().stream().filter(e -> e.getType().equals(SkillEffectType.SHIP) && e.getRequiredSkillLevel() == 1).toList();

                        for (SkillSpecAPI.SkillEffectSpecAPI effect : effects) {
                            if (dist <= EFFECT_RANGE && !other.hasTag("jdp_flowerfishFirstSecondThird_" + skillLevel.getSkill().getName())) {
                                effect.getAsShipEffect().apply(other.getMutableStats(), other.getHullSize(), "jdp_flowerfishFirstSecondThird", 1f);
                                other.addTag("jdp_flowerfishFirstSecondThird_" + skillLevel.getSkill().getName());
                            } else if (dist > EFFECT_RANGE) {
                                effect.getAsShipEffect().unapply(other.getMutableStats(), other.getHullSize(), "jdp_flowerfishFirstSecondThird");
                                other.removeTag("jdp_flowerfishFirstSecondThird_" + skillLevel.getSkill().getName());
                            }
                        }
                    }
                }
            }
        }

        List<String> tags = ship.getTags().stream().filter(t -> t.contains("jdp_flowerfishFirstSecondThird_")).map(t -> t.replace("jdp_flowerfishFirstSecondThird_", "")).toList();

        String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_escort_package");

        if (playerShip(ship)) {
            if (tags.isEmpty()) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY, icon, "First Second Third", "no connection", true);
            } else {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY, icon, "First Second Third", tags.toString(), false);
            }
        }
    }
}