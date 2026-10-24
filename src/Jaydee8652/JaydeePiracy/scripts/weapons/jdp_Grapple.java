package Jaydee8652.JaydeePiracy.scripts.weapons;

import Jaydee8652.JaydeePiracy.scripts.skills.flowerfish.jdp_AptitudeFlowerfish;
import Jaydee8652.JaydeePiracy.scripts.skills.flowerfish.jdp_flowerfishAccountingOfBurdens;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.*;
import static java.lang.Math.*;

public class jdp_Grapple implements OnFireEffectPlugin, OnHitEffectPlugin {
    public static String GRAPPLE_DATA_KEY = "jdp_grappleData";
    public static Color textColor = new Color(0,121,216,255);

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        // Cable
        jdp_GrapplingLineRenderer line = new jdp_GrapplingLineRenderer(weapon, projectile);
        Global.getCombatEngine().addLayeredRenderingPlugin(line);

        projectile.setCustomData(GRAPPLE_DATA_KEY, line);
        // Cable
    }
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        // Cable
        jdp_GrapplingLineRenderer line = (jdp_GrapplingLineRenderer) projectile.getCustomData().get(GRAPPLE_DATA_KEY);
        if (!shieldHit) line.setTarget(target);

        Vector2f offset = Vector2f.sub(point, target.getLocation(), null);
        offset = Misc.rotateAroundOrigin(offset, -target.getFacing());

        if (!shieldHit) line.setTargetPoint(offset);
        // Cable

        WeaponAPI weapon = projectile.getWeapon();

        if (!shieldHit) line.setResistance(weapon.getRange() * 0.f);//line.setDestructible(false);
        ShipAPI ship = projectile.getSource();

        if (!shieldHit) {
            ship.addListener(new GrappleAdvanceble(target, ship, weapon, line));
        } else {
            line.setTarget(null);
        }
    }


    public static class GrappleAdvanceble implements AdvanceableListener {
        CombatEntityAPI target;
        ShipAPI ship;
        WeaponAPI weapon;

        jdp_GrapplingLineRenderer line;
        int clock = 0;

        public GrappleAdvanceble(CombatEntityAPI target, ShipAPI ship, WeaponAPI weapon, jdp_GrapplingLineRenderer line) {
            this.target = target;
            this.ship = ship;
            this.weapon = weapon;

            this.line = line;
        }

        @Override
        public void advance(float amount) {
            Vector2f anchor = new Vector2f(line.getTargetPoint());
            anchor = Misc.rotateAroundOrigin(anchor, target.getFacing());
            Vector2f.add(target.getLocation(), anchor, anchor);

            Vector2f relativeWeaponLocation = new Vector2f(weapon.getLocation());
            Vector2f.sub(ship.getLocation(), relativeWeaponLocation, relativeWeaponLocation);

            float distance = MathUtils.getDistance(anchor, weapon.getLocation());

            if (distance >= ((weapon.getRange()) * 0.95f)) {
                clock += 1;

                Vector2f targetForce = calculateForce(ship.getMass(), ship.getVelocity(), relativeWeaponLocation, VectorUtils.getAngle(anchor, weapon.getLocation()));
                Vector2f shipForce = calculateForce(target.getMass(), target.getVelocity(), line.getTargetPoint(), VectorUtils.getAngle(weapon.getLocation(), anchor));

                targetForce.scale((float) 1 / (clock * target.getMass()));
                shipForce.scale((float) 1 / (clock * ship.getMass()));

                Vector2f.add(targetForce, target.getVelocity(), target.getVelocity());
                Vector2f.add(shipForce, ship.getVelocity(), ship.getVelocity());
            } else {
                clock = 0;
            }

            if (!ship.isAlive() || target.isExpired() || line.getTarget() == null) {
                line.setTarget(null);
                ship.removeListener(this);
            }
        }

        public Vector2f calculateForce(float mass, Vector2f velocity, Vector2f anchor, float targetAngle) {
            float angle = VectorUtils.getAngle(velocity, anchor);
            float force = (float) (velocity.length() * Math.cos(180f - angle) * mass);

            Vector2f direction = MathUtils.getPoint(new Vector2f(), 1f, targetAngle);
            direction.scale(force);

            return direction;
        }
    }
}