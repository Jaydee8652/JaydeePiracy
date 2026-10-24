package Jaydee8652.JaydeePiracy.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.magiclib.util.MagicRender;
import org.magiclib.util.MagicTargeting;
//import data.scripts.weapons.Diableavionics_virtuous_itanoEffect;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class jdp_defensetorpedoAI implements MissileAIPlugin, GuidedMissileAI{
    private CombatEngineAPI engine;
    private final MissileAPI missile;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    //    private float timer=0, delay=0.05f;
    //data
    private final float MAX_SPEED;
    //    private final int SEARCH_RANGE = 1000;
    private final float DAMPING = 0.05f;
    private final Color EXPLOSION_COLOR = new Color(255, 0, 0, 255);
    private final Color PARTICLE_COLOR = new Color(240, 200, 50, 255);
    private final int NUM_PARTICLES = 20;
    private boolean runOnce = false;

    public jdp_defensetorpedoAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        MAX_SPEED = missile.getMaxSpeed() * 1.25f; //slight over lead
    }

    @Override
    public void advance(float amount) {

        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (Global.getCombatEngine().isPaused() || missile.isFading() || missile.isFizzling()) {
            return;
        }


        //sideways launch
        if(!runOnce){
            runOnce=true;

            float angle=MathUtils.getShortestRotation(VectorUtils.getAngle(missile.getWeapon().getLocation(),missile.getWeapon().getShip().getLocation()),missile.getSource().getFacing());
            if(angle>0){
                angle = missile.getSource().getFacing()+90;
            } else {
                angle = missile.getSource().getFacing()-90;
            }

            Vector2f.add(
                    missile.getVelocity(),
                    MathUtils.getPoint(
                            new Vector2f(),
                            100,
                            angle
                    ),
                    missile.getVelocity()
            );

        }

        // if there is no target, assign one
        if (target == null
                || !Global.getCombatEngine().isEntityInPlay(target)
                || target.getOwner() == missile.getOwner()
        ) {
            missile.giveCommand(ShipCommand.ACCELERATE);

            int targetSearchRange = (int) (missile.getWeapon().getRange() * 1.5f * (missile.getMaxFlightTime() - missile.getFlightTime()) / missile.getMaxFlightTime());
            WeightedRandomPicker<MissileAPI> targetPicker = getTargetPicker(missile.getSource(), missile.getLocation(), false, targetSearchRange);
            List<MissileAPI> near = CombatUtils.getMissilesWithinRange(missile.getLocation(), targetSearchRange);
            if (!near.isEmpty()) {
                for (MissileAPI m : near) {
                    if (m == missile) continue;
                    if (m.getOwner() == missile.getOwner()) {
                        if (m.getWeaponSpec() != null && m.getWeaponSpec().getWeaponId().equals(missile.getWeaponSpec().getWeaponId())) {
                            if (m.getMissileAI() instanceof jdp_defensetorpedoAI) {
                                jdp_defensetorpedoAI ai = (jdp_defensetorpedoAI) m.getMissileAI();
                                if (ai.target instanceof MissileAPI) {
                                    MissileAPI otherMissileTarget = (MissileAPI) ai.target;
                                    int index = targetPicker.getItems().indexOf(otherMissileTarget);
                                    if (index >= 0) {
                                        targetPicker.setWeight(index, targetPicker.getWeight(index) * 0.66f);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            target = targetPicker.pick();

            return;
        }

        //finding lead point to aim to
        float dist = MathUtils.getDistanceSquared(missile.getLocation(), target.getLocation());
        if (dist < 2500) {
            proximityFuse();
            return;
        }
        lead = AIUtils.getBestInterceptPoint(
                missile.getLocation(),
                MAX_SPEED,
                target.getLocation(),
                target.getVelocity()
        );
        if (lead == null) {
            lead = target.getLocation();
        }

        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                missile.getLocation(),
                lead
        );

        float correction = MathUtils.getShortestRotation(VectorUtils.getFacing(missile.getVelocity()), correctAngle);
        if (correction > 0) {
            correction = -11.25f * ((float) Math.pow(FastTrig.cos(MathUtils.FPI * correction / 90) + 1, 2) - 4);
        } else {
            correction = 11.25f * ((float) Math.pow(FastTrig.cos(MathUtils.FPI * correction / 90) + 1, 2) - 4);
        }
        correctAngle += correction;

        //turn the missile
        float aimAngle = MathUtils.getShortestRotation(missile.getFacing(), correctAngle);
        if (aimAngle < 0) {
            missile.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            missile.giveCommand(ShipCommand.TURN_LEFT);
        }
        if (Math.abs(aimAngle) < 45) {
            missile.giveCommand(ShipCommand.ACCELERATE);
        }

        // Damp angular velocity if we're getting close to the target angle
        if (Math.abs(aimAngle) < Math.abs(missile.getAngularVelocity()) * DAMPING) {
            missile.setAngularVelocity(aimAngle / DAMPING);
        }
    }

    void proximityFuse() {
        engine.applyDamage(
                target,
                target.getLocation(),
                missile.getDamageAmount(),
                DamageType.FRAGMENTATION,
                0f,
                false,
                false,
                missile.getSource()
        );

        DamagingExplosionSpec boom = new DamagingExplosionSpec(
                0.1f,
                100,
                50,
                missile.getDamageAmount(),
                50,
                CollisionClass.PROJECTILE_NO_FF,
                CollisionClass.PROJECTILE_FIGHTER,
                2,
                5,
                5,
                25,
                new Color(225, 100, 0),
                new Color(200, 100, 25)
        );
        boom.setDamageType(DamageType.FRAGMENTATION);
        boom.setShowGraphic(false);
        boom.setSoundSetId("explosion_flak");
        engine.spawnDamagingExplosion(boom, missile.getSource(), missile.getLocation());

        if (MagicRender.screenCheck(0.1f, missile.getLocation())) {
            engine.addHitParticle(
                    missile.getLocation(),
                    new Vector2f(),
                    100,
                    1,
                    0.25f,
                    EXPLOSION_COLOR
            );
            for (int i = 0; i < NUM_PARTICLES; i++) {
                float axis = (float) Math.random() * 360;
                float range = (float) Math.random() * 100;
                engine.addHitParticle(
                        MathUtils.getPointOnCircumference(missile.getLocation(), range / 5, axis),
                        MathUtils.getPointOnCircumference(new Vector2f(), range, axis),
                        2 + (float) Math.random() * 2,
                        1,
                        1 + (float) Math.random(),
                        PARTICLE_COLOR
                );
            }
            engine.applyDamage(
                    missile,
                    missile.getLocation(),
                    missile.getHitpoints() * 2f,
                    DamageType.FRAGMENTATION,
                    0f,
                    false,
                    false,
                    missile
            );
        } else {
            engine.removeEntity(missile);
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    public void init(CombatEngineAPI engine) {
    }

    public WeightedRandomPicker<MissileAPI> getTargetPicker(CombatEntityAPI source, Vector2f searchPos, boolean ignoreFlares, float maxRange) {
        CombatEngineAPI engine = Global.getCombatEngine();
        WeightedRandomPicker<MissileAPI> missilePicker = new WeightedRandomPicker<>();

        List<MissileAPI> missiles = engine.getMissiles();
        for (MissileAPI m : missiles) {
            if (!ignoreFlares || !m.isFlare()) {
                if (!m.isFading() && m.getOwner() != source.getOwner() && m.getCollisionClass() != CollisionClass.NONE && m.getSpec().isRenderTargetIndicator()) { //is the missile alive, hittable and hostile
                    if (CombatUtils.isVisibleToSide(m, source.getOwner()) && MathUtils.isPointWithinCircle(searchPos, m.getLocation(), maxRange)) { //is it around
                        missilePicker.add(m, m.getDamageAmount() * (1 + m.getMoveSpeed() / MathUtils.getDistance(source, m)));
                    }
                }
            }
        }

        return missilePicker;
    }
}