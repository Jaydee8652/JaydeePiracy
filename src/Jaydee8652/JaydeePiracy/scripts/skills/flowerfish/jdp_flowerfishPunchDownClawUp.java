package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import Jaydee8652.JaydeePiracy.hullmods.jdp_mercenaryRelay;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.skills.*;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.rpg.Person;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import org.magiclib.*;

import java.awt.*;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class jdp_flowerfishPunchDownClawUp extends SCBaseSkillPlugin {

    public static float BONUS_PER_SIZE = 2.5f;

    @Override
    public String getAffectsString() {
        return "all ships with human officers";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltip) {
        tooltip.addPara("Increases damage to other ships by " + BONUS_PER_SIZE + "%% per difference in size class", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("   - This bonus is doubled for destroyers and cruisers", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "destroyers", "cruisers");
        tooltip.addSpacer(10f);

/*
        //float modW = 100f;
        float nameW = (tooltip.getWidthSoFar() -20f) / 5;
        tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                20f, true, true,
                new Object [] {"Class", nameW, "Frigates", nameW, "Destroyers", nameW, "Cruisers", nameW, "Captial", nameW});

        tooltip.addRow(Misc.getTextColor(), "Frigates", Misc.getGrayColor(), "-", Misc.getHighlightColor(), "2.5%", Misc.getHighlightColor(), "5%", Misc.getHighlightColor(), "7.5%");
        tooltip.addRow(Misc.getTextColor(), "Destroyers", Misc.getHighlightColor(), "5%", Misc.getGrayColor(), "-", Misc.getHighlightColor(), "5%", Misc.getHighlightColor(), "10%");
        tooltip.addRow(Misc.getTextColor(), "Cruisers", Misc.getHighlightColor(), "10%", Misc.getHighlightColor(), "5%", Misc.getGrayColor(), "-", Misc.getHighlightColor(), "5%");
        tooltip.addRow(Misc.getTextColor(), "Captial", Misc.getHighlightColor(), "7.5%", Misc.getHighlightColor(), "5%", Misc.getHighlightColor(), "2.5%", Misc.getGrayColor(), "-");
        tooltip.addTable("", 0, 10f);
        tooltip.addSpacer(20f);
*/



        tooltip.addPara("\"It is as a paradox, existing at the top of the food chain, and the bottom, and every rung in between.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltip.addPara("  -Wiltschi, N. The Jangala Question, Xenoflora 17(3) 201", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltip.addSpacer(10f);
    }

    @Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) {
        ShipAPI.HullSize hullSize = ship.getHullSize();

        if (jdp_AptitudeFlowerfish.isHumanOfficer(ship)) {
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                ship.getMutableStats().getDamageToFrigates().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 3f);
                ship.getMutableStats().getDamageToDestroyers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 2f);
                ship.getMutableStats().getDamageToCruisers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 1f);
                //ship.getMutableStats().getDamageToCapital().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 0f);

            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                ship.getMutableStats().getDamageToFrigates().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 2f * 2f);
                ship.getMutableStats().getDamageToDestroyers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 1f * 2f);
                //ship.getMutableStats().getDamageToCruisers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 0f * 2f);
                ship.getMutableStats().getDamageToCapital().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 1f * 2f);

            } else if (hullSize == ShipAPI.HullSize.DESTROYER) {
                ship.getMutableStats().getDamageToFrigates().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 1f * 2f);
                //ship.getMutableStats().getDamageToDestroyers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 0f * 2f);
                ship.getMutableStats().getDamageToCruisers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 1f * 2f);
                ship.getMutableStats().getDamageToCapital().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 2f * 2f);

            } else if (hullSize == ShipAPI.HullSize.FRIGATE) {
                //ship.getMutableStats().getDamageToFrigates().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 0f);
                ship.getMutableStats().getDamageToDestroyers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 2f);
                ship.getMutableStats().getDamageToCruisers().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 2f);
                ship.getMutableStats().getDamageToCapital().modifyPercent("jdp_flowerfishPunchDownClawUp", BONUS_PER_SIZE * 3f);
            }
        }
    }
}
