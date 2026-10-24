package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.*;
import static Jaydee8652.JaydeePiracy.scripts.skills.flowerfish.jdp_AptitudeFlowerfish.isHumanOfficer;

public class jdp_flowerfishQuadrupolarCoupling extends SCBaseSkillPlugin {
    public static Map<ShipAPI.HullSize, Float> sizeMult = new HashMap<ShipAPI.HullSize, Float>();
    static {
        //These guys are here to stop crashes if someone else adds
        //ships with these sizes for whatever reason
        sizeMult.put(ShipAPI.HullSize.FIGHTER, 8f);
        sizeMult.put(ShipAPI.HullSize.DEFAULT, 1f);

        sizeMult.put(ShipAPI.HullSize.FRIGATE, 8f);
        sizeMult.put(ShipAPI.HullSize.DESTROYER, 4f);
        sizeMult.put(ShipAPI.HullSize.CRUISER, 2f);
        sizeMult.put(ShipAPI.HullSize.CAPITAL_SHIP, 1f);
    }

    public static Object STATUS_KEY1 = new Object();
    public static Object STATUS_KEY2 = new Object();

    public static float maxShieldPolarity = 20f;
    public static float maxArmorPolarity = 20f;

    public static float armorbreakPercentage = 5f;
    public static float stackDuration = 3f;

    @Override
    public String getAffectsString() {
        return "all ships with human officers";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltip) {
        tooltip.addPara("Increases shield efficiency by " + Math.round(maxShieldPolarity) + "%%", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("   - This modifier becomes negative after the ship is overloaded", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "overloaded");
        tooltip.addPara("   - Once negative, increases back to the positive cap over %s/%s/%s/%s seconds depending on hullsize", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.FRIGATE)) + "", Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.CRUISER)) + "", Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "");

        tooltip.addSpacer(10f);

        tooltip.addPara("Increases effective armor strength by " + Math.round(maxArmorPolarity) + "%%", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("   - This modifier becomes negative if the ship loses more than %s of total hull within %s seconds", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round(armorbreakPercentage) + "%", "" + Math.round(stackDuration));
        tooltip.addPara("   - Once negative, increases back to the positive cap over %s/%s/%s/%s seconds depending on hullsize", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.FRIGATE)) + "", Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.DESTROYER)) + "", Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.CRUISER)) + "", Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.CAPITAL_SHIP)) + "");
        tooltip.addSpacer(10f);

        tooltip.addPara("Rate of modifier recovery is doubled if both are not at their maximum value", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("*" + maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.FRIGATE) / 2f + "/" + Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.DESTROYER) / 2f) + "/" + Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.CRUISER) / 2f)+ "/" + Math.round(maxShieldPolarity * 2 / sizeMult.get(ShipAPI.HullSize.CAPITAL_SHIP) / 2f) + " seconds to fully both modifiers simultaneously", 0f, Misc.getGrayColor(), Misc.getGrayColor());
        tooltip.addSpacer(10f);

        tooltip.addPara("\"This. This is a plainly bad idea... Let's do it\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltip.addPara("  -nanoforge engineer Alacrity Orrett", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltip.addSpacer(10f);
    }


    @Override
    public void advance(SCData data, Float amount) {
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
    }

    @Override
    public void onActivation(SCData data) {
    }

    @Override
    public void onDeactivation(SCData data) {
    }

    @Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) {
        if (jdp_AptitudeFlowerfish.isHumanOfficer(ship) && ship.getListeners(QuadrupolarCouplingAdvanceListener.class).isEmpty()) ship.addListener(new QuadrupolarCouplingAdvanceListener(ship));
    }

    @Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {

    }

    public static class QuadrupolarCouplingAdvanceListener implements AdvanceableListener, DamageListener {
        ShipAPI ship;
        ArrayList<jdp_flowerfishQuadrupolarCoupling.DamageStack> stacks = new ArrayList<>();

        float shieldPolarity;
        float armorPolarity;
        Color innerColor;

        boolean overloaded = false;
        float armorbreakThreshold = 500f;

        int mult;

        IntervalUtil soundTracker = new IntervalUtil(3f, 3f);
        boolean playedSound = false;

        public QuadrupolarCouplingAdvanceListener(ShipAPI ship) {
            this.ship = ship;

            this.shieldPolarity = 0f;
            this.armorPolarity = 0f;

            this.innerColor = ship.getVariant().getHullSpec().getShieldSpec().getInnerColor();
            this.armorbreakThreshold = (armorbreakPercentage / 100) * ship.getVariant().getHullSpec().getHitpoints();

            this.mult = 2;
        }

        @Override
        public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
            if (result.getDamageToHull() > 0f) stacks.add(new DamageStack(stackDuration, result.getDamageToHull()));
        }

        @Override
        public void advance(float amount) {
            if (!ship.isAlive() || !isHumanOfficer(ship)) ship.removeListener(this);

            soundTracker.advance(amount);

            float cumulativeDamage = 0f;
            for (DamageStack stack : new ArrayList<>(stacks)) {
                stack.duration -= 1 * amount;
                cumulativeDamage += stack.amount;

                if (stack.duration < 0f) {
                    stacks.remove(stack);
                }
            }

            if (shieldPolarity < maxShieldPolarity && !overloaded) shieldPolarity += amount * mult * sizeMult.get(ship.getHullSize());
            if (armorPolarity < maxArmorPolarity) armorPolarity += amount * mult * sizeMult.get(ship.getHullSize());

            //Shield
            if (ship.getFluxTracker().isOverloaded() && !overloaded) {
                overloaded = true;
                shieldPolarity = -maxShieldPolarity;
            } else {
                overloaded = false;
            }

            //Armor
            if (cumulativeDamage > armorbreakThreshold) {
                if (!playedSound) {
                    Global.getSoundPlayer().playSound("ui_cargo_metals_drop", MathUtils.getRandomNumberInRange(0.96f, 1.04f), 1f, ship.getLocation(), ship.getVelocity());
                    playedSound = true;
                }

                armorPolarity = -maxArmorPolarity;
                stacks.clear();
            }
            if (soundTracker.intervalElapsed()) playedSound = false;

            //Stats
            ship.getMutableStats().getShieldAbsorptionMult().modifyMult("jdp_flowerfishQuadrupolarCoupling", 1 - (shieldPolarity * 0.01f));
            ship.getMutableStats().getEffectiveArmorBonus().modifyMult("jdp_flowerfishQuadrupolarCoupling", 1 + (armorPolarity * 0.01f));

            //Shield Visual
            int ShieldRed = Math.max(0, Math.min(Math.round(innerColor.getRed() * (shieldPolarity * 2f) + 255 * (1 - (shieldPolarity * 2f))), 255));
            if (ship.getShield() != null) ship.getShield().setInnerColor(new Color(ShieldRed, innerColor.getGreen(), innerColor.getBlue(), 200));

            //Accelerate if both apolar
            if (shieldPolarity != maxShieldPolarity && armorPolarity != maxArmorPolarity) {
                mult = 2;
            } else {
                mult = 1;
            }

            if (playerShip(ship)) {
                String modS = "";
                String modA = "";
                if (shieldPolarity > 0) modS = "+";
                if (armorPolarity > 0) modA = "+";

                Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY1, Global.getSettings().getSpriteName("ui", "jdp_icon_shield"), "QUADRUPOLAR COUPLING - SHIELDS", modS + Math.round(shieldPolarity) + "% shield efficiency", shieldPolarity < 0);
                Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY2, Global.getSettings().getSpriteName("ui", "jdp_icon_armor"), "QUADRUPOLAR COUPLING - ARMOR", modA + Math.round(armorPolarity) + "% max effective armor", armorPolarity < 0);
            }
        }
    }

    public static class DamageStack {
        private float duration;
        private final float amount;

        public DamageStack(float duration, float amount) {
            this.duration = duration;
            this.amount = amount;
        }
    }
}
