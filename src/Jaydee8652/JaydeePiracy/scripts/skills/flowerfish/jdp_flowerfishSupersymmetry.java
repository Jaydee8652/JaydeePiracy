package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import Jaydee8652.JaydeeProficiencies.patch.WeaponFireListenerPatch;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import java.util.*;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.*;
import static Jaydee8652.JaydeePiracy.scripts.skills.flowerfish.jdp_AptitudeFlowerfish.isHumanOfficer;
import static com.fs.starfarer.api.combat.WeaponAPI.WeaponSize.SMALL;
import static com.fs.starfarer.api.combat.WeaponAPI.WeaponType.BALLISTIC;
import static com.fs.starfarer.api.combat.WeaponAPI.WeaponType.ENERGY;

public class jdp_flowerfishSupersymmetry extends SCBaseSkillPlugin {
    public static float duration = 3f;
    public static float bonus = 1f;
    public static int cap = 50;
    public static float beamRefire = 1f;

    public static Object STATUS_KEY1 = new Object();
    public static Object STATUS_KEY2 = new Object();

    @Override
    public String getAffectsString() {
        return "all ships with human officers";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Firing ballistic/energy weapons reduces the flux cost of the other type by " + Math.round(bonus) + "%% for " + Math.round(duration) + " seconds, capped at " + cap + "%%", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("*Only affects weapons of the same size or smaller, beams are not considered", 0f, Misc.getGrayColor(), Misc.getGrayColor());
        tooltipMakerAPI.addSpacer(10f);

        tooltipMakerAPI.addPara("\"There is irony in, through destructive interference, making destruction serve yet further destruction.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("  -Proving Grounds Transcript, Undisclosed Location", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltipMakerAPI.addSpacer(10f);
    }

    public static class SupersymmetryAdvanceListener implements AdvanceableListener, WeaponFireListenerPatch.WeaponFireListener {
        ShipAPI ship;
        public SupersymmetryAdvanceListener(ShipAPI ship) {
            this.ship = ship;
        }
        ArrayList<WeaponStack> stacks = new ArrayList<>();

        float countLargeBallistic = 0;
        float countMediumBallistic = 0;
        float countSmallBallistic = 0;

        float countLargeEnergy = 0;
        float countMediumEnergy = 0;
        float countSmallEnergy = 0;

        @Override
        public void advance(float amount) {
            if (!ship.isAlive() || !isHumanOfficer(ship)) ship.removeListener(this);
            for (WeaponStack stack : new ArrayList<>(stacks)) {
                stack.duration -= 1 * amount;

                if (stack.duration < 0f) {
                    stacks.remove(stack);
                }
            }

            countLargeBallistic = MathUtils.clamp(stacks.stream().filter(s -> s.size.equals(WeaponAPI.WeaponSize.LARGE) && s.type.equals(BALLISTIC)).toList().size(), 0, cap);
            countMediumBallistic = MathUtils.clamp(countLargeBallistic + stacks.stream().filter(s -> s.size.equals(WeaponAPI.WeaponSize.MEDIUM) && s.type.equals(BALLISTIC)).toList().size(), 0, cap);
            countSmallBallistic = MathUtils.clamp(countMediumBallistic + stacks.stream().filter(s -> s.size.equals(SMALL) && s.type.equals(BALLISTIC)).toList().size(), 0, cap);

            countLargeEnergy = MathUtils.clamp(stacks.stream().filter(s -> s.size.equals(WeaponAPI.WeaponSize.LARGE) && s.type.equals(ENERGY)).toList().size(), 0, cap);
            countMediumEnergy = MathUtils.clamp(countLargeEnergy + stacks.stream().filter(s -> s.size.equals(WeaponAPI.WeaponSize.MEDIUM) && s.type.equals(ENERGY)).toList().size(), 0, cap);
            countSmallEnergy = MathUtils.clamp(countMediumEnergy + stacks.stream().filter(s -> s.size.equals(SMALL) && s.type.equals(ENERGY)).toList().size(), 0, cap);

            if (playerShip(ship)) {
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY1, Global.getSettings().getSpriteName("ui", "jdp_icon_ballistic"), "SUPERSYMMETRY - BALLISTIC", Math.round(countSmallEnergy) + "/" + Math.round(countMediumEnergy) + "/" + Math.round(countLargeEnergy) + "% flux discount.", false);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY2, Global.getSettings().getSpriteName("ui", "jdp_icon_energy"), "SUPERSYMMETRY - ENERGY", Math.round(countSmallBallistic) + "/" + Math.round(countMediumBallistic) + "/" + Math.round(countLargeBallistic) + "% flux discount.", false);
            }
        }

        @Override
        public void reportProjectileSpawn(Object o, WeaponAPI weaponAPI, CombatEngineAPI combatEngineAPI) {

        }

        @Override
        public void reportOnFire(int burst, WeaponAPI weapon, CombatEngineAPI combatEngineAPI) {
            if (burst > 0) return;
            WeaponAPI.WeaponSize size = weapon.getSize();
            WeaponType type = weapon.getType();

            switch (type) {
                case BALLISTIC:
                    stacks.add(new WeaponStack(duration, type, size));
                    switch (size) {
                        case SMALL ->
                                ship.getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.01f * countSmallEnergy);
                        case MEDIUM ->
                                ship.getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.01f * countMediumEnergy);
                        case LARGE ->
                                ship.getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.01f * countLargeEnergy);
                    }
                    break;
                case ENERGY:
                    stacks.add(new WeaponStack(duration, type, size));
                    switch (size) {
                        case SMALL ->
                                ship.getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.01f * countSmallBallistic);
                        case MEDIUM ->
                                ship.getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.01f * countMediumBallistic);
                        case LARGE ->
                                ship.getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.01f * countLargeBallistic);
                    }
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {
    }

    @Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) {
        if (isHumanOfficer(ship) && ship.getListeners(SupersymmetryAdvanceListener.class).isEmpty()) ship.addListener(new SupersymmetryAdvanceListener(ship));
    }

    @Override
    public void onActivation(SCData data) {}

    @Override
    public void onDeactivation(SCData data) {}

    public static class WeaponStack {
        private float duration;
        private final WeaponType type;
        private final WeaponAPI.WeaponSize size;

        public WeaponStack(float duration, WeaponType type, WeaponAPI.WeaponSize size) {
            this.duration = duration;
            this.type = type;
            this.size = size;
        }
    }
}
