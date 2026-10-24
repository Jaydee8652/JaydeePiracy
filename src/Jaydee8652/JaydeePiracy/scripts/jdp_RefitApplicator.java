package Jaydee8652.JaydeePiracy.scripts;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.plugins.jdp_lunaSettings;
import Jaydee8652.JaydeePiracy.scripts.skills.jdp_totalinternalreflection;
import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import Jaydee8652.JaydeeProficiencies.skills.jdp_refit_applicator;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Objects;

import static Jaydee8652.JaydeePiracy.scripts.skills.jdp_cerebralsymbiance.JDP_CEREBRAL_SYMBIANCE_TAG;
import static Jaydee8652.JaydeePiracy.utils.jdp_Items.RELAY_DRONE_NAME;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.consumeItem;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.createCargoStack;
import static com.fs.starfarer.api.impl.combat.dweller.HumanShipShroudedHullmod.ALLOW_ON_PHASE_SHIPS;

public class jdp_RefitApplicator implements jdp_refit_applicator.RefitApplicatorImpl {
    public void init() {
        if(Global.getSector().getPersistentData().get("jdp_RefitApplicator") == null) {
            Global.getSector().getPersistentData().put("jdp_RefitApplicator", this);
            Global.getSector().getListenerManager().addListener(this);
        }
    }

    private static final Logger log = Global.getLogger(JaydeePiracyPlugin.class);

    public void run(MutableShipStatsAPI stats) {
        //Allow Shrouded hullmods on phase ships
        ALLOW_ON_PHASE_SHIPS = jdp_lunaSettings.jdp_shroudedPhase();

        if (stats.getFleetMember() != null) {
            if (stats.getVariant() != null) {
                if (stats.getFleetMember().getCaptain() != null && !stats.getFleetMember().getCaptain().getStats().hasSkill(jdp_Skills.JDP_CEREBRALSYMBIANCE))
                    stats.getVariant().removeTag(JDP_CEREBRAL_SYMBIANCE_TAG);
            }

            var temp = stats.getFleetMember().getHullSpec();
            if (temp != null) {
                var hullSpec = temp.getDParentHull() != null ? temp.getDParentHull() : temp;
                if (hullSpec.hasTag(jdp_totalinternalreflection.JDP_INCOMPREHENSIBLE_HULL)) {
                    if (!Global.getSector().getMemoryWithoutUpdate().contains(jdp_totalinternalreflection.JDP_CAN_RECOVER_PENROSE)) {
                        stats.getMaxCombatReadiness().modifyMult(jdp_totalinternalreflection.JDP_CAN_RECOVER_PENROSE, 0);
                        stats.getMaxCombatReadiness().modifyFlat(jdp_totalinternalreflection.JDP_CAN_RECOVER_PENROSE, -100, "Incomprehensible Hull");
                    } else {
                        stats.getMaxCombatReadiness().unmodify(jdp_totalinternalreflection.JDP_CAN_RECOVER_PENROSE);
                    }
                }
            }
        }
        //Destroy illegal items
        consumeItem(createCargoStack(CargoAPI.CargoItemType.FIGHTER_CHIP, RELAY_DRONE_NAME));

        //Apply Ground Support Fighters
        List<String> wings = stats.getVariant().getWings();
        ShipVariantAPI ship = stats.getVariant();
        boolean hasGroundSupport = false;

        for (String wing : wings) {
            if (!wing.isEmpty()) {
                FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(wing);

                if(spec == null) {
                    log.warn("JDP_DEBUG: Wing [" + wing + "] has null fighter spec");
                } else if (Global.getSettings().getFighterWingSpec(wing).hasTag("jdp_groundSupportFighter")) {
                    hasGroundSupport = true;
                    if (!ship.hasHullMod("jdp_supportFighter")) {
                        ship.addMod("jdp_supportFighter");
                    }
                }
            }
        }
        if (!hasGroundSupport) {
            ship.removeMod("jdp_supportFighter");
        }
    }


    @Override
    public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize) {
        run(stats);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize) {
        run(stats);
    }
}