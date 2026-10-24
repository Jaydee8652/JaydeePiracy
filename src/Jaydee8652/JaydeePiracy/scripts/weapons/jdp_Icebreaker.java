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
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.*;
import static java.lang.Math.min;

public class jdp_Icebreaker implements OnFireEffectPlugin, OnHitEffectPlugin {
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
        if (line == null) return;

        if (!shieldHit) line.setTarget(target);

        Vector2f offset = Vector2f.sub(point, target.getLocation(), null);
        offset = Misc.rotateAroundOrigin(offset, -target.getFacing());

        if (!shieldHit) line.setTargetPoint(offset);
        // Cable

        ShipAPI ship = projectile.getSource();
        Class<? extends CombatEntityAPI> targetClass = target.getClass();

        if (!shieldHit && isShip(target)) {
            ShipAPI targetShip = (ShipAPI) target;

            targetShip.addListener(new IcebreakerAdvanceble(targetShip, ship, 10, line));
        } else {
            addFloatingText("CONNECTION FAILED...", target, ship, Misc.getNegativeHighlightColor());
            line.setTarget(null);
        }
    }


    public static class IcebreakerAdvanceble implements AdvanceableListener {
        IntervalUtil interval = new IntervalUtil(1f, 1f);
        Integer clock;
        ShipAPI target;
        ShipAPI ship;
        jdp_GrapplingLineRenderer line;

        public IcebreakerAdvanceble(ShipAPI target, ShipAPI ship, Integer duration, jdp_GrapplingLineRenderer line) {
            this.target = target;
            this.ship = ship;

            this.line = line;
            this.clock = duration;

            addFloatingText("CONNECTION ESTABLISHED...", target, ship, textColor);
            Global.getSoundPlayer().playSound("ui_transponder_on", 1.0f, 0.66f, target.getLocation(), target.getVelocity());
        }

        @Override
        public void advance(float amount) {
            interval.advance(amount);
            if(interval.intervalElapsed()) {
                clock -= 1;
                if (clock == 0) {
                    Global.getSoundPlayer().playSound("technology1", 1.0f, 0.66f, target.getLocation(), target.getVelocity());
                    addFloatingText("HACK SUCCESSFUL", target, this.ship, Misc.getPositiveHighlightColor());
                    line.setTarget(null);

                    if(target.getHullSpec().getHullId().contains("jdp_lamp")) {
                        SectorEntityToken erraticLampEntity = Global.getSector().getEntitiesWithTag("jdp_erratic_lamp_tag").get(0);
                        MemoryAPI memory = erraticLampEntity.getMemoryWithoutUpdate();
                        CampaignFleetAPI defenders = memory.getFleet("$defenderFleet");
                        if (defenders != null) {
                            memory.unset("$defenderFleet");
                            memory.set("$defenderFleetDefeated", true);
                            memory.set("$hasDefenders", false, 0);
                        }

                        Global.getCombatEngine().endCombat(3, FleetSide.PLAYER);
                    }

                    target.getEngineController().forceFlameout();
                    for(WeaponAPI weapon: target.getAllWeapons()) weapon.disable();
                    //Integer owner = target.getOwner();
                    //target.setOwner(ship.getOwner());

                    this.target.removeListener(this);
                } else {
                    addFloatingText(clock + " SECONDS REMAINING", target, this.ship, textColor);
                }

            }
            if (line.getFade() != 1) {
                addFloatingText("CONNECTION LOST...", target, ship, Misc.getNegativeHighlightColor());
                Global.getSoundPlayer().playSound("ui_transponder_off", 1.0f, 0.66f, target.getLocation(), target.getVelocity());

                this.target.removeListener(this);
            }
        }
    }
}