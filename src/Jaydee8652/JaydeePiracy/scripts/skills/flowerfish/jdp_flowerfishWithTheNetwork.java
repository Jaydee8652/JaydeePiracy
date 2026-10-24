package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import Jaydee8652.JaydeePiracy.hullmods.jdp_mercenaryRelay;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CharacterDataAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.plugins.OfficerLevelupPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.rpg.Person;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;
import org.magiclib.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static Jaydee8652.JaydeePiracy.scripts.ai.jdp_relayAI.*;

public class jdp_flowerfishWithTheNetwork extends SCBaseSkillPlugin{
    @Override
    public String getAffectsString() {
        return "all ships";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        float HEIGHT = 50f;
        float PAD = 10f;

        tooltipMakerAPI.addPara("Gain access to the Mercenary Relay hullmod", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("*Mercenary Relay costs 10 OP and can only be installed on cruisers", 0f, Misc.getGrayColor(), Misc.getHighlightColor());

        tooltipMakerAPI.addSpacer(10f);

        Integer totalRange = Math.round(EFFECT_RANGE + EFFECT_FADE);

        TooltipMakerAPI relay = tooltipMakerAPI.beginImageWithText(jdp_relayIcon, HEIGHT);
        relay.addPara("Mercenary Relay", 0f, h, "Mercenary Relay");
        relay.addPara("When the \"With The Network\" skill is enabled, the ship will deploy a relay drone that will seek out allied frigates and destroyers within " + totalRange.toString() + " su, providing the following bonuses once linked.", 0f, h, "\"With The Network\"", totalRange.toString());
        relay.addSpacer(10f);
        relay.addPara(" +20%% damage to the rear of enemies", 0f, h,"+20%");
        relay.addPara(" +100%% damage to weapons and engines", 0f, h,"+100%");
        relay.addPara(" +200 weapon range", 0f, h, "+200");

        tooltipMakerAPI.addImageWithText(PAD);
        tooltipMakerAPI.addSpacer(10f);

        tooltipMakerAPI.addPara("\"I wish they could see me [unparseable]. Just for the kill.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("  -Unknown Speaker, [ref rep HEGINT/UNKNOWN_ASSAILANTS-C1-3]", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltipMakerAPI.addSpacer(10f);
    }

    @Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {
        super.advanceInCombat(data, ship, amount);
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
        //Adds a helper hullmod who actually does all the heavy lifting
        if (!stats.getVariant().hasHullMod("jdp_mercenaryRelayHelper")) {
            stats.getVariant().addMod("jdp_mercenaryRelayHelper"); // don't add as a perma mod
        }
    }

    @Override
    public void onActivation(SCData data) {
        data.getCommander().addTag("jdp_flowerfishWithTheNetwork");

        if (data.isPlayer()) {
            CharacterDataAPI player = Global.getSector().getCharacterData();
            player.addHullMod("jdp_mercenaryRelay");
        }
    }

    @Override
    public void onDeactivation(SCData data) {
        data.getCommander().removeTag("jdp_flowerfishWithTheNetwork");

    }
}
