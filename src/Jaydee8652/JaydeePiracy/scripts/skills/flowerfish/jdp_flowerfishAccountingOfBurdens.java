package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import Jaydee8652.JaydeePiracy.scripts.ai.jdp_relayAI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.combat.entities.Ship;
import kotlin.uuid.Uuid;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;
import org.magiclib.subsystems.MagicSubsystemsManager;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import com.fs.starfarer.api.combat.ShipAPI;

import java.awt.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.*;
import static java.lang.Math.min;

public class jdp_flowerfishAccountingOfBurdens extends SCBaseSkillPlugin {
    private static final int duration = 30;
    private static final Float range = 2000f;

    @Override
    public String getAffectsString() {
        return "all ships with human officers";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        float HEIGHT = 50f;
        float PAD = 10f;


        tooltip.addPara("All ships with human officers in your fleet receive the \"Burden Allocator\" subsystem", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addSpacer(10f);

        TooltipMakerAPI relay = tooltip.beginImageWithText(Global.getSettings().getSpriteName("ui", "jdp_icon_link"), HEIGHT);
        relay.addPara("Burden Allocator", 0f, h, "Burden Allocator");
        relay.addPara("Tethers to an allied ship within 2000 units, merging the flux pools of the two vessels for 30 seconds", 0f, h, "2000", "30");
        relay.addPara("Capacity is summed, and any changes to flux will affect both ships simultaneously", 0f, h);
        relay.addPara("*If one vessel overloads, so will the other, applying the shorter overload time", 0f, Misc.getGrayColor(), Misc.getHighlightColor());
        relay.addPara(" When the effect ends, flux will be redistributed proportionally to contributed flux capacity", 0f, Misc.getGrayColor(), Misc.getHighlightColor());

        tooltip.addImageWithText(PAD);
        tooltip.addSpacer(10f);

        tooltip.addPara("\"Referring not to a holostage, but a bridge. Not a drama, but a war.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltip.addPara("  -Business secrets of the Court: The Holo success of Westernesse (TriMedia Store)", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltip.addSpacer(10f);
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(SCData data, ShipAPI fighter, ShipAPI ship, String id) {
    }

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
    }

    @Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) {
        if (jdp_AptitudeFlowerfish.isHumanOfficer(ship)) {
            MagicSubsystemsManager.addSubsystemToShip(ship, new jdp_AccountingOfBurdensLash(ship));
        }
    }


    @Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {
    }

    @Override
    public void onActivation(SCData data) {
        // No implementation needed
    }

    @Override
    public void onDeactivation(SCData data) {
        // No implementation needed
    }

    public static class AccountingOfBurdensAdvanceListener implements AdvanceableListener {
        IntervalUtil interval = new IntervalUtil(1f, 1f);

        private ShipAPI ship;
        private ShipAPI target;
        private float duration;

        private Float lastStored = -1f;
        private Float lastStoredHard = -1f;

        private Boolean overloaded = false;
        private Float overloadTime;

        float capacity;

        float shipCapacity;
        float targetCapacity;

        public AccountingOfBurdensAdvanceListener(ShipAPI ship, ShipAPI target, float duration) {
            this.ship = ship;
            this.target = target;
            this.duration = duration;

            this.capacity = ship.getMutableStats().getFluxCapacity().getModifiedValue() + target.getMutableStats().getFluxCapacity().getModifiedValue();

            this.shipCapacity = ship.getMutableStats().getFluxCapacity().getModifiedValue();
            this.targetCapacity = target.getMutableStats().getFluxCapacity().getModifiedValue();

            this.overloadTime = min(ship.getMutableStats().getOverloadTimeMod().getMult(), target.getMutableStats().getOverloadTimeMod().getMult());

            ship.getMutableStats().getFluxCapacity().modifyFlat("jdp_AccountingOfBurdensAdvanceListener", capacity - ship.getMutableStats().getFluxCapacity().getModifiedValue());
            target.getMutableStats().getFluxCapacity().modifyFlat("jdp_AccountingOfBurdensAdvanceListener", capacity - target.getMutableStats().getFluxCapacity().getModifiedValue());

            ship.getMutableStats().getOverloadTimeMod().modifyMult("jdp_AccountingOfBurdensAdvanceListener", overloadTime / ship.getMutableStats().getOverloadTimeMod().getMult());
            target.getMutableStats().getOverloadTimeMod().modifyMult("jdp_AccountingOfBurdensAdvanceListener", overloadTime / target.getMutableStats().getOverloadTimeMod().getMult());

            ship.setOverloadColor(jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR);
            target.setOverloadColor(jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR);
        }

        @Override
        public void advance(float amount) {
            interval.advance(amount);
            if (interval.intervalElapsed()) lashVisual(ship, target, 1, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, 0.2f);

            if (lastStored < 0f || lastStoredHard < 0f) {
                lastStored = ship.getFluxTracker().getCurrFlux();
                lastStoredHard = ship.getFluxTracker().getHardFlux();
            }

            Float diff = (ship.getFluxTracker().getCurrFlux() - lastStored) + (target.getFluxTracker().getCurrFlux() - lastStored);
            if ((lastStored + diff) > 0f) {
                ship.getFluxTracker().setCurrFlux(lastStored + diff);
                target.getFluxTracker().setCurrFlux(lastStored + diff);
            }
            lastStored += diff;

            Float diffHard = (ship.getFluxTracker().getHardFlux() - lastStoredHard) + (target.getFluxTracker().getHardFlux() - lastStoredHard);
            if ((lastStoredHard + diffHard) > 0f) {
                ship.getFluxTracker().setHardFlux(lastStoredHard + diffHard);
                target.getFluxTracker().setHardFlux(lastStoredHard + diffHard);
            }
            lastStoredHard += diffHard;

            if ((ship.getFluxTracker().isOverloaded() || target.getFluxTracker().isOverloaded()) && !overloaded) {
                ship.getFluxTracker().forceOverload(0);
                target.getFluxTracker().forceOverload(0);
                overloaded = true;
                lashVisual(ship, target, 20, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR.brighter().brighter(), 1f);
            } else if (!ship.getFluxTracker().isOverloaded() && !target.getFluxTracker().isOverloaded()) {
                overloaded = false;
            }

            duration -= 1 * amount;

            if (playerShip(ship)) {
                int remaining = (int) duration;
                Global.getCombatEngine().maintainStatusForPlayerShip(
                        "jdp_AccountingOfBurdens",
                        Global.getSettings().getSpriteName("ui", "jdp_icon_link"),
                        "Accounting of Burdens",
                        "Linked with " + target.getName() + " for " + remaining + "s",
                        false
                );
            } else if (playerShip(ship)) {
                int remaining = (int) duration;
                Global.getCombatEngine().maintainStatusForPlayerShip(
                        "jdp_AccountingOfBurdens",
                        Global.getSettings().getSpriteName("ui", "jdp_icon_link"),
                        "Accounting of Burdens",
                        "Linked with " + ship.getName() + " for " + remaining + "s",
                        false
                );
            }

            if (duration <= 0 || !ship.isAlive() || !target.isAlive()) {
                ship.getFluxTracker().setCurrFlux((shipCapacity / capacity) * lastStored);
                target.getFluxTracker().setCurrFlux((targetCapacity / capacity) * lastStored);

                ship.getFluxTracker().setHardFlux((targetCapacity / capacity) * lastStoredHard);
                target.getFluxTracker().setHardFlux((targetCapacity / capacity) * lastStoredHard);


                ship.getMutableStats().getFluxCapacity().unmodify("jdp_AccountingOfBurdensAdvanceListener");
                target.getMutableStats().getFluxCapacity().unmodify("jdp_AccountingOfBurdensAdvanceListener");

                ship.getMutableStats().getOverloadTimeMod().unmodify("jdp_AccountingOfBurdensAdvanceListener");
                target.getMutableStats().getOverloadTimeMod().unmodify("jdp_AccountingOfBurdensAdvanceListener");

                ship.resetOverloadColor();
                target.resetOverloadColor();

                ship.removeTag("jdp_AccountingOfBurdensLinked");
                target.removeTag("jdp_AccountingOfBurdensLinked");

                ship.removeListener(this);
            }
        }
    }


    private static class jdp_AccountingOfBurdensLash extends MagicSubsystem {
        private ShipAPI ship;
        private ShipAPI target;

        public jdp_AccountingOfBurdensLash(ShipAPI ship) {
            super(ship);
            this.ship = ship;
            this.target = null;
        }

        @Override
        public int getOrder() {
            return ORDER_SHIP_MODULAR;
        }

        @Override
        public String getDisplayText() {
            return "Burden Allocator";
        }

        @Override
        public float getBaseActiveDuration() {
            return 1f;
        }

        @Override
        public float getBaseCooldownDuration() {
            return 90f;
        }

        @Override
        public boolean isToggle() {
            return false;
        }

        @Override
        public boolean shouldActivateAI(float amount) {
            return canActivate();
        }

        @Override
        public boolean canActivate() {
            return ((this.target != null) && (!target.hasTag("jdp_AccountingOfBurdensLinked") && !ship.hasTag("jdp_AccountingOfBurdensLinked")));
        }

        @Override
        public void advance(float amount, boolean isPaused) {
            this.target = findTarget(ship);
        }


        @Override
        public void onActivate() {
            if (ship.getListeners(AccountingOfBurdensDelayedActivation.class).isEmpty()) {
                ship.addListener(new AccountingOfBurdensDelayedActivation(ship, target));
            }
        }
    }

    public static class AccountingOfBurdensDelayedActivation implements AdvanceableListener {
        float elapsed = 0f;
        float delay = 10f;
        ShipAPI ship;
        ShipAPI target;


        public AccountingOfBurdensDelayedActivation(ShipAPI ship, ShipAPI target) {
            this.ship = ship;
            this.target = target;

            this.delay = (MathUtils.getDistance(ship.getLocation(), target.getLocation()) / 1000f);
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused()) return;

            elapsed += amount;
            if (elapsed < delay) return;

            ship.removeListener(this);

            if (target != null
                    && ship.getListeners(AccountingOfBurdensAdvanceListener.class).isEmpty()
                    && target.getListeners(AccountingOfBurdensAdvanceListener.class).isEmpty()
                    && validLash(ship, target)) {

                ship.addTag("jdp_AccountingOfBurdensLinked");
                target.addTag("jdp_AccountingOfBurdensLinked");

                ship.addListener(new AccountingOfBurdensAdvanceListener(ship, target, duration));
                lashVisual(ship, target, 3, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR.brighter(), 1f);
            }
        }
    }

    protected static ShipAPI findTarget(ShipAPI ship) {

        //TODO: Toggling Autopilot simply uses the system, because for a frame it thinks the player is activating
        // the system and so bypasses the target priority checks
        if (ship == Global.getCombatEngine().getPlayerShip() && !Global.getCombatEngine().getCombatUI().isAutopilotOn()) {
            if (ship.getShipTarget() != null) {
                return ship.getShipTarget();
            } else {
                Vector2f worldCursorPosition = CombatUtils.toWorldCoordinates(new Vector2f(Mouse.getX(), Mouse.getY()));
                ShipAPI closestValidShip = null;
                float closestDist = Float.MAX_VALUE;

                for (ShipAPI target : Global.getCombatEngine().getShips()) {
                    if (validLash(ship, target)) {
                        float dist = MathUtils.getDistanceSquared(ship.getLocation(), worldCursorPosition);
                        if (dist < closestDist) {
                            closestValidShip = target;
                            closestDist = dist;
                        }
                    }
                }
                return closestValidShip;
            }
        } else {
            WeightedRandomPicker<ShipAPI> picker = new WeightedRandomPicker<>();

            for (ShipAPI target : Global.getCombatEngine().getShips()) {

                //Slight randomness
                float w = (float) Math.random();
                if (w < 0.4f) w = 0.4f;

                if(validLash(ship, target)) {
                    //Higher the flux the more you want that combined pool, both stats wise and in combat.
                    w *= (float) 0.001 * target.getMutableStats().getFluxDissipation().getModifiedValue();
                    w *= (float) 0.0001 * target.getMutableStats().getFluxCapacity().getModifiedValue();

                    w *= (float) 0.001 * target.getFluxTracker().getCurrFlux();
                    w *= (float) 0.01 * target.getFluxTracker().getHardFlux();

                    //Link to escorts
                    if (escortCheck(ship, target)) w *= 10;
                    if (escortCheck(target, ship)) w *= 10;

                    //Try to avoid linking capitals to capitals and frigates to frigates
                    if ((ship.isFrigate() || ship.isDestroyer()) && (target.isCruiser() || target.isCapital())) w *= 3;
                    if ((target.isFrigate() || target.isDestroyer()) && (ship.isCruiser() || ship.isCapital())) w *= 3;

                    if (target.getVariant().hasHullMod(HullMods.FLUX_SHUNT)) w *= 2;

                    //Avoid lashing to and then overloading the player
                    if (ship.getFluxTracker().getFluxLevel() > 0.9 && target == Global.getCombatEngine().getPlayerShip()) w = 0;

                    //Don't link to civilians
                    if (target.getVariant().isCivilian()) w = 0;

                    if (w > 1) picker.add(target, w);
                }
            }

            if (picker.isEmpty()) return null;
            return picker.pick();
        }
    }

    protected static boolean validLash(ShipAPI ship, ShipAPI target) {
        return ((ship.getOwner() == target.getOwner()) &&
                (ship != target) &&
                target.isAlive() &&
                !target.isFighter() &&
                !target.isStation() &&
                !target.isStationModule() &&
                !ship.isStation() &&
                !ship.isStationModule() &&
                !target.hasTag("jdp_AccountingOfBurdensLinked") &&
                !ship.hasTag("jdp_AccountingOfBurdensLinked")) &&
                ship.getListeners(AccountingOfBurdensAdvanceListener.class).isEmpty() &&
                target.getListeners(AccountingOfBurdensAdvanceListener.class).isEmpty() &&
                ship.getListeners(AccountingOfBurdensDelayedActivation.class).isEmpty() &&
                target.getListeners(AccountingOfBurdensDelayedActivation.class).isEmpty() &&
                (MathUtils.getDistance(ship.getLocation(), target.getLocation()) <= range);
    }

    protected static void lashVisual(ShipAPI ship, ShipAPI target, Integer count, Color color, Float volume) {
        Global.getSoundPlayer().playSound("energy_lash_fire", MathUtils.getRandomNumberInRange(0.96f, 1.04f), volume, ship.getLocation(), ship.getVelocity());
        Global.getSoundPlayer().playSound("energy_lash_fire", MathUtils.getRandomNumberInRange(0.96f, 1.04f), volume, target.getLocation(), target.getVelocity());

        for (int i = 0; i < count; i++) {
            Vector2f targetPoint = null;
            Vector2f shipPoint = null;

            while (targetPoint == null) {
                Vector2f potentialPoint = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
                if (CollisionUtils.isPointWithinBounds(potentialPoint, target)) {
                    targetPoint = potentialPoint;
                }
            }
            while (shipPoint == null) {
                Vector2f potentialPoint = MathUtils.getRandomPointInCircle(ship.getLocation(), ship.getCollisionRadius());
                if (CollisionUtils.isPointWithinBounds(potentialPoint, ship)) {
                    shipPoint = potentialPoint;
                }
            }

            Global.getCombatEngine().spawnEmpArcVisual(
                    targetPoint,
                    target,
                    shipPoint,
                    ship,
                    3f,
                    color,
                    Color.WHITE);
            Global.getCombatEngine().spawnEmpArcVisual(
                    shipPoint,
                    ship,
                    targetPoint,
                    target,
                    3f,
                    color,
                    Color.WHITE);
        }
    }
}
