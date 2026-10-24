package Jaydee8652.JaydeePiracy.scripts.ai;

import Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils;
import Jaydee8652.JaydeePiracy.scripts.skills.flowerfish.jdp_AptitudeFlowerfish;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.util.MagicLensFlare;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.*;
import static java.lang.Math.min;

//based on code from the Maltese from EDshipyard, itself based Sundog's ICE repair drone and Dark.Revenant's Imperium Titan
// this is the relay drone
public class jdp_relayAI extends jdp_customShipAI {
    public static String jdp_relayIcon = Global.getSettings().getSpriteName("ui", "jdp_icon_relay");

    private final ShipwideAIFlags flags = new ShipwideAIFlags();
    private final ShipAIConfig config = new ShipAIConfig();
    private ShipAPI carrier;
    ShipAPI target;
    Vector2f targetOffset;

    boolean returning = false;
    float range = 3500;

    public static float SABOTAGE_BONUS = 10f;
    public static float RANGE_BONUS = 200f;
    public static float FLANK_BONUS = 20f;

    public static float EFFECT_RANGE = 3000f;
    public static float EFFECT_FADE = 500f;


    private final IntervalUtil interval = new IntervalUtil(0.25f, 0.33f);
    private final IntervalUtil countdown = new IntervalUtil(4f, 4f);

    public jdp_relayAI(ShipAPI ship) {
        super(ship);
    }

    @Override
    public void advance(float amount) {
        if (carrier == null) {
            init();
        }

        if (ship.isLanding()) {
            countdown.advance(amount);
            if (countdown.intervalElapsed()) {
                ship.getWing().getSource().land(ship);
                return;
            }
        }

        interval.advance(amount);
        if (interval.intervalElapsed()) {
            super.advance(amount);

            if (target == null) return;

            if (target.getListeners(WithTheNetworkAdvanceListener.class).isEmpty()
                    && MathUtils.getDistance(ship, target) < 35f
                    && target != carrier) {
                target.addListener(new WithTheNetworkAdvanceListener(target, ship, carrier));
            } else if (returning
                    && !ship.isLanding()
                    && MathUtils.getDistance(ship, carrier) < carrier.getCollisionRadius() / 3f) {
                ship.beginLandingAnimation(carrier);
            }
        }
        goToDestination();
    }



    public class WithTheNetworkDelayedRetarget implements AdvanceableListener {
        float elapsed = 0f;
        float delay;
        ShipAPI ship;

        public WithTheNetworkDelayedRetarget(float delay, ShipAPI ship) {
            this.delay = delay;
            this.ship = ship;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused()) return;

            elapsed += amount;
            if (elapsed < delay) return;

            //If the drone doesn't reach the target within 15 seconds, try a different ship
            if (!ship.isAlive() || ship.getListeners(WithTheNetworkAdvanceListener.class).isEmpty()) ship.removeTag("jdp_WithTheNetworkRelayTarget");
            ship.removeListener(this);

            chooseTarget();
        }
    }

    public class WithTheNetworkDelayedUnapply implements AdvanceableListener {
        float elapsed = 0f;
        float delay;
        ShipAPI ship;
        ShipAPI drone;

        public WithTheNetworkDelayedUnapply(float delay, ShipAPI ship, ShipAPI drone) {
            this.delay = delay;
            this.ship = ship;
            this.drone = drone;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused()) return;

            elapsed += amount;
            if (elapsed < delay) return;

            //If the drone doesn't reconnect within 5 seconds, try a different ship
            Global.getSoundPlayer().playSound("ui_transponder_off", 1.0f, 0.66f, drone.getLocation(), drone.getVelocity());
            if (!ship.getListeners(WithTheNetworkAdvanceListener.class).isEmpty()) ship.removeListenerOfClass(WithTheNetworkAdvanceListener.class);

            ship.removeTag("jdp_WithTheNetworkRelayTarget");
            ship.removeListener(this);

            chooseTarget();
        }
    }

    public class WithTheNetworkAdvanceListener implements AdvanceableListener, DamageDealtModifier {
        private ShipAPI ship;
        private ShipAPI drone;
        private ShipAPI carrier;


        public WithTheNetworkAdvanceListener(ShipAPI ship, ShipAPI drone, ShipAPI carrier) {
            this.ship = ship;
            this.drone = drone;
            this.carrier = carrier;

            //Match drone speed to ship speed
            drone.getMutableStats().getMaxTurnRate().modifyFlat("jdp_flowerfishWithTheNetworkFlat", (ship.getMutableStats().getMaxTurnRate().getModifiedValue() - drone.getMutableStats().getMaxTurnRate().getModifiedValue()));
            drone.getMutableStats().getTurnAcceleration().modifyFlat("jdp_flowerfishWithTheNetworkFlat", (ship.getMutableStats().getTurnAcceleration().getModifiedValue() - drone.getMutableStats().getTurnAcceleration().getModifiedValue()));

            drone.getMutableStats().getMaxSpeed().modifyFlat("jdp_flowerfishWithTheNetworkFlat", (ship.getMutableStats().getMaxSpeed().getModifiedValue() - drone.getMutableStats().getMaxSpeed().getModifiedValue()));
            drone.getMutableStats().getAcceleration().modifyFlat("jdp_flowerfishWithTheNetworkFlat", (ship.getMutableStats().getAcceleration().getModifiedValue() - drone.getMutableStats().getAcceleration().getModifiedValue()));
            drone.getMutableStats().getDeceleration().modifyFlat("jdp_flowerfishWithTheNetworkFlat", (ship.getMutableStats().getDeceleration().getModifiedValue() - drone.getMutableStats().getDeceleration().getModifiedValue()));

            Global.getSoundPlayer().playSound("ui_transponder_on", 1.0f, 0.66f, drone.getLocation(), drone.getVelocity());
        }

        @Override
        public void advance(float amount) {
            float radSum = ship.getShieldRadiusEvenIfNoShield() + carrier.getShieldRadiusEvenIfNoShield();
            radSum *= 0.75f;
            float dist = Misc.getDistance(ship.getShieldCenterEvenIfNoShield(), carrier.getShieldCenterEvenIfNoShield());
            dist -= radSum;

            float mag = 0f;
            if (dist < EFFECT_RANGE) {
                mag = 1f;
            } else if (dist < EFFECT_RANGE + EFFECT_FADE) {
                mag = 1f - (dist - EFFECT_RANGE) / EFFECT_FADE;
            }

            if (mag > 0) {
                ship.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat("jdp_flowerfishWithTheNetwork", RANGE_BONUS * mag);
                ship.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat("jdp_flowerfishWithTheNetwork", RANGE_BONUS * mag);
                ship.getMutableStats().getMissileWeaponRangeBonus().modifyFlat("jdp_flowerfishWithTheNetwork", RANGE_BONUS * mag);

                ship.getMutableStats().getDamageToTargetEnginesMult().modifyPercent("jdp_flowerfishWithTheNetwork", SABOTAGE_BONUS * mag);
                ship.getMutableStats().getDamageToTargetWeaponsMult().modifyPercent("jdp_flowerfishWithTheNetwork", SABOTAGE_BONUS * mag);

                if (!ship.getListeners(WithTheNetworkDelayedUnapply.class).isEmpty()) ship.removeListenerOfClass(WithTheNetworkDelayedUnapply.class);
            } else {
                ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify("jdp_flowerfishWithTheNetwork");
                ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify("jdp_flowerfishWithTheNetwork");
                ship.getMutableStats().getMissileWeaponRangeBonus().unmodify("jdp_flowerfishWithTheNetwork");

                ship.getMutableStats().getDamageToTargetEnginesMult().unmodify("jdp_flowerfishWithTheNetwork");
                ship.getMutableStats().getDamageToTargetWeaponsMult().unmodify("jdp_flowerfishWithTheNetwork");
                
                if (ship.getListeners(WithTheNetworkDelayedUnapply.class).isEmpty()) ship.addListener(new WithTheNetworkDelayedUnapply(5, ship, drone));
            }

            if (!ship.isAlive() || !drone.isAlive()) {
                //Match drone speed to ship
                drone.getMutableStats().getMaxTurnRate().unmodify("jdp_flowerfishWithTheNetworkFlat");
                drone.getMutableStats().getTurnAcceleration().unmodify("jdp_flowerfishWithTheNetworkFlat");

                drone.getMutableStats().getMaxSpeed().unmodify("jdp_flowerfishWithTheNetworkFlat");
                drone.getMutableStats().getAcceleration().unmodify("jdp_flowerfishWithTheNetworkFlat");
                drone.getMutableStats().getDeceleration().unmodify("jdp_flowerfishWithTheNetworkFlat");



                ship.getMutableStats().getBallisticWeaponRangeBonus().unmodify("jdp_flowerfishWithTheNetwork");
                ship.getMutableStats().getEnergyWeaponRangeBonus().unmodify("jdp_flowerfishWithTheNetwork");
                ship.getMutableStats().getMissileWeaponRangeBonus().unmodify("jdp_flowerfishWithTheNetwork");

                ship.getMutableStats().getDamageToTargetEnginesMult().unmodify("jdp_flowerfishWithTheNetwork");
                ship.getMutableStats().getDamageToTargetWeaponsMult().unmodify("jdp_flowerfishWithTheNetwork");

                ship.removeListener(this);
                ship.removeTag("jdp_WithTheNetworkRelayTarget");
                chooseTarget();
            }

            if (playerShip(ship)) {
                if (mag > 0.005f) {
                    String percent = "" + (int) Math.round(mag * 100f) + "%";
                    Global.getCombatEngine().maintainStatusForPlayerShip(
                            "jdp_flowerfishWithTheNetwork", jdp_relayIcon, "With The Network", percent + " telemetry quality", false);
                } else {
                    Global.getCombatEngine().maintainStatusForPlayerShip(
                            "jdp_flowerfishWithTheNetwork", jdp_relayIcon, "With The Network", "no connection", true);
                }
            }
        }

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            float radSum = ship.getShieldRadiusEvenIfNoShield() + carrier.getShieldRadiusEvenIfNoShield();
            radSum *= 0.75f;
            float dist = Misc.getDistance(ship.getShieldCenterEvenIfNoShield(), carrier.getShieldCenterEvenIfNoShield());
            dist -= radSum;

            float mag = 0f;
            if (dist < EFFECT_RANGE) {
                mag = 1f;
            } else if (dist < EFFECT_RANGE + EFFECT_FADE) {
                mag = 1f - (dist - EFFECT_RANGE) / EFFECT_FADE;
            }

            if (!(target instanceof ShipAPI)) return null;

            float targetAngle = Misc.getAngleInDegrees(target.getLocation(), point);
            float targetFacing = target.getFacing();

            if (Math.abs(MathUtils.getShortestRotation(targetFacing, targetAngle)) > 130f) {
                damage.getModifier().modifyMult("jdp_flowerfishWithTheNetworkListener", (1 + ((FLANK_BONUS * mag) / 100f)));
            }
            return "jdp_flowerfishWithTheNetworkDamageMod";
        }
    }

    @Override
    public boolean needsRefit() {
        return ship.getFluxTracker().getFluxLevel() >= 1f;
    }

    @Override
    public void cancelCurrentManeuver() {
    }

    @Override
    public void evaluateCircumstances() {
        if (carrier == null || !carrier.isAlive()) {
            //Killlllll meeeeeeee fatheeeeeer
            Global.getCombatEngine().applyDamage(ship, ship.getLocation(),
                    ship.getMaxHitpoints() * 10f, DamageType.HIGH_EXPLOSIVE, 0,
                    true, true, ship);

            return;
        }

        if (ship.getListeners(WithTheNetworkDelayedRetarget.class).isEmpty()) setTarget(chooseTarget());
    }

    ShipAPI chooseTarget() {
        if (needsRefit()) {
            returning = true;
            return carrier;
        } else returning = false;

        if (carrier.getShipTarget() != null
                && carrier.getOwner() == carrier.getShipTarget().getOwner()
                && (carrier.getShipTarget().isFrigate() || carrier.getShipTarget().isDestroyer())
                && carrier.getShipTarget().isAlive()
                && MathUtils.getDistance(carrier, carrier.getShipTarget()) < range
                && !carrier.getShipTarget().hasTag("jdp_WithTheNetworkRelayTarget")) {
            return carrier.getShipTarget();
        }

        ShipAPI currentTarget = carrier;
        float currentTargetPriority = 0f;

        for (ShipAPI s : Global.getCombatEngine().getShips()) {
            float d = MathUtils.getDistance(carrier, s);
            if (carrier.getOwner() == s.getOwner()
                    && (s.isFrigate() || s.isDestroyer())
                    && s.isAlive()
                    && d < range
                    && !s.hasTag("jdp_WithTheNetworkRelayTarget")) {

                    float priority = d / 1000f;
                    if (s.isDestroyer()) {
                        priority++;
                    }
                    if (s == Global.getCombatEngine().getPlayerShip()) {
                        priority++;
                    }
                    if (escortCheck(ship, target)) {
                        priority++;
                        priority *= 2f;
                    }
                    if (escortCheck(target, ship)) {
                        priority++;
                        priority *= 2f;
                    }

                    if (currentTargetPriority < priority / 2f) {
                        currentTargetPriority = priority / 2f;
                        currentTarget = s;
                    }
                }
            }

        if (carrier != currentTarget) return currentTarget;
        return null;
    }

    void setTarget(ShipAPI t) {
        if (target == t || t == null) return;

        target = t;
        this.ship.setShipTarget(t);

        if (ship.getListeners(WithTheNetworkDelayedRetarget.class).isEmpty()) ship.addListener(new WithTheNetworkDelayedRetarget(15, target));
        target.addTag("jdp_WithTheNetworkRelayTarget");
    }

    void goToDestination() {
        float angleDif = MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(ship.getLocation(), target.getLocation()));

        if (MathUtils.getDistance(ship.getLocation(), target.getLocation()) > target.getShieldRadiusEvenIfNoShield()) {
            accelerate();
        } else {
            decelerate();
        }

        if (Math.abs(angleDif) > 5) turnToward(target.getLocation());

        strafeToward(target.getLocation());
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }

    @Override
    public void setDoNotFireDelay(float amount) {
    }

    @Override
    public ShipAIConfig getConfig() {
        return config;
    }

    public void init() {
        carrier = ship.getWing().getSourceShip();
        target = carrier;
        targetOffset = jdp_StolenUtils.toRelative(carrier, carrier.getLocation());
        range = ship.getWing().getRange();
    }
}
