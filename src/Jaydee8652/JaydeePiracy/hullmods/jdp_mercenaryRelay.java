package Jaydee8652.JaydeePiracy.hullmods;
import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeePiracy.scripts.ai.jdp_relayAI.*;

public class jdp_mercenaryRelay extends BaseHullMod {
    public static float VISION_BONUS = 2000f;

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) return "" + Math.round(VISION_BONUS);
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        float HEIGHT = 50f;
        float PAD = 10f;

        if (Global.getSettings().getModManager().isModEnabled("second_in_command")) {
            Integer totalRange = Math.round(EFFECT_RANGE + EFFECT_FADE);
            
            TooltipMakerAPI relay = tooltip.beginImageWithText(jdp_relayIcon, HEIGHT);
            relay.addPara("Mercenary Relay", 0f, h, "Mercenary Relay");

            relay.addPara("When the \"With The Network\" skill is enabled, the ship will deploy a relay drone that will seek out allied frigates and destroyers within " + totalRange.toString() + " su, providing the following bonuses once linked.", 0f, h, "\"With The Network\"", totalRange.toString());
            relay.addSpacer(10f);
            relay.addPara(" +20%% damage to the rear of enemies", 0f, h,"+20%");
            relay.addPara(" +100%% damage to weapons and engines", 0f, h,"+100%");
            relay.addPara(" +200 weapon range", 0f, h, "+200");

            tooltip.addImageWithText(PAD);
            tooltip.addSpacer(10f);

            if (ship == null) return;
            if (ship.getFleetCommander() == null) return;

            if (!ship.getFleetCommander().hasTag("jdp_flowerfishWithTheNetwork")) {
                tooltip.addSectionHeading("Bonuses Inactive", Alignment.MID, opad);
                tooltip.addPara("\"With The Network\" is not assigned to the Fleet Commander of this ship, Relay Network bonuses are inactive.", opad, bad, bad, "\"With The Network\" is not assigned to the Fleet Commander of this ship, Relay Network bonuses are inactive.");
            } else {
                tooltip.addSectionHeading("Bonuses Active", Alignment.MID, opad);
                tooltip.addPara("\"With The Network\" is assigned to the Fleet Commander of this ship, Relay Network bonuses are active.", opad, good, good, "\"With The Network\" is assigned to the Fleet Commander of this ship, Relay Network bonuses are active.");
            }
        }
    }


    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (ship != null && ship.isStationModule()) {
            return false;
        }
        if (ship != null && (ship.getVariant().getHullMods().contains("jdp_modularShip") ||
                ship.getVariant().getHullMods().contains("apex_civwhenrefit") ||
                ship.getVariant().getHullMods().contains("converted_fighterbay") ||
                ship.getVariant().getHullMods().contains("converted_hangar"))) {
            return false;
        }
        if (ship != null && !ship.isCruiser()){
            return false;
        }
        return true;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship.getVariant().getHullMods().contains("jdp_modularShip")) {
            return "Incompatible, cannot be applied to modular ships.";
        }
        if (ship.getVariant().getHullMods().contains("apex_civwhenrefit")) {
            return "Incompatible, cannot be applied to modular ships.";
        }
        if (ship.getVariant().getHullMods().contains("converted_fighterbay")){
            return "Incompatible, cannot be applied with converted fighter bays.";
        }
        if (ship.getVariant().getHullMods().contains("converted_hangar")){
            return "Incompatible, cannot be applied with converted hangar.";
        }
        if (ship.isStationModule()){
            return "Incompatible, cannot be applied to modular ships.";
        }
        if (ship.isCapital()) {
            return "Can not be installed on capital ships";
        }
        if (ship.isDestroyer()) {
            return "Can not be installed on destroyers";
        }
        if (ship.isFrigate()) {
            return "Can not be installed on frigates";
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //Generic bonus to hide its purpose without SiC
        stats.getSightRadiusMod().modifyFlat(id, VISION_BONUS);

        //Nothing else to do, just a marker for jdp_mercenaryRelayHelper which actually does all the heavy lifting
    }
}
