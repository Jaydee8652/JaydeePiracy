package Jaydee8652.JaydeePiracy.hullmods;

import Jaydee8652.JaydeePiracy.scripts.skills.jdp_totalinternalreflection;
import Jaydee8652.JaydeeProficiencies.utils.refl.ReflectionUtilis;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.ai.system.V;
import com.fs.starfarer.combat.entities.Ship;
import com.fs.starfarer.loading.S;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static Jaydee8652.JaydeeProficiencies.utils.combat.jdp_WeaponMutationUtils.*;
import static java.lang.Math.*;
import static java.lang.Math.sin;

public class jdp_mirrorShield extends BaseHullMod {
    public static final Color RING_COLOR_MAIN = new Color(235, 235, 235, 255);
    public static final Color INNER_COLOR_MAIN = new Color(235, 235, 235, 75);
    public static final Color RING_COLOR_SECONDARY = new Color(125, 225, 125, 155);
    public static final Color INNER_COLOR_SECONDARY = new Color(205, 255, 205, 255);

    public static int ARC = 45;
    public static String KEY = "$jdp_reflectedProjectile";

    public static class jdp_mirrorShieldRender extends BaseCombatLayeredRenderingPlugin {
        protected CombatEngineLayers layer = CombatEngineLayers.BELOW_INDICATORS_LAYER;
        protected ShipAPI ship;
        public float texX = 0f;

        public jdp_mirrorShieldRender(ShipAPI ship) {
            this.ship = ship;
        }

        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(layer);
        }

        @Override
        public float getRenderRadius() {
            return Float.MAX_VALUE;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (viewport == null) return;
            if (ship.getShield().isOff()) return;

            float arc = ship.getShield().getActiveArc();
            float radius = ship.getShieldRadiusEvenIfNoShield() + 30f;
            float opacityMult =0.1f;

            try {
                Global.getSettings().loadTexture("graphics/fx/shields256ringd.png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var sprite = Global.getSettings().getSprite("graphics/fx/shields256.png");
            var spriteTwo = Global.getSettings().getSprite("graphics/fx/shields256ringd.png");

            sprite.bindTexture();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glPushMatrix();
            GL11.glTranslatef(ship.getLocation().x, ship.getLocation().y, 0f);
            GL11.glRotatef(ship.getShield().getFacing() - (arc / 2f), 0f, 0f, 1f);

            for (int i = 0; i <= 2; i++) {
                Color color = RING_COLOR_SECONDARY;
                if (i == 2) {
                    color = INNER_COLOR_SECONDARY;
                    spriteTwo.bindTexture();
                }

                float scroll = texX;
                float invertScroll = 1 - texX;
                GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) 0);
                GL11.glTexCoord2f(0.5f, 0.5f);
                GL11.glVertex2f(0.0f, 0.0f);

                float var2 = 2f * (float) PI * radius * arc / 360f;
                int numPoints = Math.max((int) ((var2 / 20f) + 1), 2);
                int unknownTwo = (int) ((arc / 5f) + 1);
                numPoints = Math.max(numPoints, unknownTwo);

                int unknownThree = numPoints - 1;
                double radians = Math.toRadians((arc / unknownThree));

                for (int iter = 0; iter < numPoints; iter++) {
                    float colorMult = opacityMult;

                    float degrees = (float) Math.toDegrees(radians * iter);
                    float constant = 10f;
                    if (degrees < constant || arc - degrees < constant) {
                        colorMult = Math.min(degrees, arc - degrees) / constant * opacityMult;
                    }

                    GL11.glColor4ub(
                            (byte) color.getRed(),
                            (byte) color.getGreen(),
                            (byte) color.getBlue(),
                            (byte) (color.getAlpha() * colorMult)
                    );

                    double pos = iter * radians;

                    if (i == 1) {
                        GL11.glTexCoord2f(
                                0.5f + (float) cos((pos + invertScroll + (PI / 4.0))) / 2.0f,
                                0.5f + (float) sin((pos + invertScroll + (PI / 4.0))) / 2.0f
                        );
                    } else {
                        GL11.glTexCoord2f(
                                0.5f + (float) cos((pos + scroll)) / 2.0f,
                                0.5f + (float) sin((pos + scroll)) / 2.0f
                        );
                    }

                    float x = radius * (float) cos(pos);
                    float y = radius * (float) sin(pos);
                    GL11.glVertex2f(x, y);
                }

                GL11.glEnd();
            }
            GL11.glPopMatrix();
        }
    }

    public static boolean isPointOnShield(ShipAPI ship, Vector2f point) {
        var angle = Misc.normalizeAngle(Misc.getAngleInDegrees(ship.getShieldCenterEvenIfNoShield(), point));
        var facing = ship.getShield().getFacing();

        var diff = (facing - angle + 180) % 360 - 180;
        var arc = ((ship.getShield().getActiveArc() / 2));

        var dist = Math.abs(Misc.getDistance(ship.getShieldCenterEvenIfNoShield(), point) - ship.getShield().getRadius());
        return ((diff <= arc && diff >= -arc) && (dist <= 5f));
    }

    public static class ReflectionData {
        float newFacing;
        Vector2f vector;
        Vector2f point;

        ReflectionData(float newFacing, Vector2f vector, Vector2f point) {
            this.point = point;
            this.vector = vector;
            this.newFacing = newFacing;
        }
    }

    public static ReflectionData calculateReflection(Vector2f point, Vector2f velocity, ShipAPI ship) {
        Vector2f shieldCenter = ship.getShieldCenterEvenIfNoShield();
        Vector2f towardCenter = new Vector2f(point.x - shieldCenter.x, point.y - shieldCenter.y);

        float newFacing;
        final Vector2f reflect;

        if (towardCenter.lengthSquared() > 0f) {
            towardCenter.normalise();
            float dot = Vector2f.dot(towardCenter, velocity);
            Vector2f parallel = new Vector2f(towardCenter.x * dot, towardCenter.y * dot);
            reflect = new Vector2f(-velocity.x + 2f * (velocity.x - parallel.x), -velocity.y + 2 * (velocity.y - parallel.y));
            newFacing = Misc.getAngleInDegrees(reflect);
        } else {
            reflect = new Vector2f(1f, 0f);
            newFacing = 0f;
        }
        return new ReflectionData(newFacing, reflect, point);
    }

    public static class MirrorShieldDamageTaken implements DamageTakenModifier, AdvanceableListener {
        final String id;
        final ShipAPI ship;
        MirrorShieldDamageTaken(ShipAPI ship, String id) {
            this.ship = ship;
            this.id = id;
        }

        public static jdp_mirrorShieldRender visuals;
        public static ArrayList<BeamReflectionDrone> beam_drones = new ArrayList<>();

        private float shiftRate = 1;
        protected float phase = 0.0F;
        protected float direction = 1;

        public static class BeamReflectionDrone {
            private final BeamAPI beam;
            private final ShipAPI fx_drone;
            private final ShipAPI ship;

            public BeamReflectionDrone(BeamAPI beam, ShipAPI ship) {
                this.beam = beam;
                this.ship = ship;

                ShipVariantAPI variant = Global.getSettings().createEmptyVariant("jdp_fxdrone", Global.getSettings().getHullSpec("jdp_fxdrone"));
                variant.addWeapon("WS 000", beam.getWeapon().getId());
                WeaponGroupSpec group = new WeaponGroupSpec(WeaponGroupType.LINKED);
                group.addSlot("WS 000");
                variant.addWeaponGroup(group);

                this.fx_drone = Global.getCombatEngine().createFXDrone(variant);

                fx_drone.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
                fx_drone.setOwner(ship.getOriginalOwner());
                fx_drone.getMutableStats().getHullDamageTakenMult().modifyMult("dem", 0f); // so it's non-targetable
                fx_drone.setDrone(true);
                fx_drone.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP, 100000f, ship);

                var weapon = fx_drone.getAllWeapons().get(0);
                if (weapon.getShip() != null) {
                    fx_drone.getMutableStats().getEnergyWeaponDamageMult().applyMods(weapon.getShip().getMutableStats().getEnergyWeaponDamageMult());
                    fx_drone.getMutableStats().getMissileWeaponDamageMult().applyMods(weapon.getShip().getMutableStats().getMissileWeaponDamageMult());
                    fx_drone.getMutableStats().getBallisticWeaponDamageMult().applyMods(weapon.getShip().getMutableStats().getBallisticWeaponDamageMult());

                    fx_drone.getMutableStats().getDamageToCapital().applyMods(weapon.getShip().getMutableStats().getDamageToCapital());
                    fx_drone.getMutableStats().getDamageToCruisers().applyMods(weapon.getShip().getMutableStats().getDamageToCruisers());
                    fx_drone.getMutableStats().getDamageToDestroyers().applyMods(weapon.getShip().getMutableStats().getDamageToDestroyers());
                    fx_drone.getMutableStats().getDamageToFrigates().applyMods(weapon.getShip().getMutableStats().getDamageToFrigates());
                    fx_drone.getMutableStats().getDamageToFighters().applyMods(weapon.getShip().getMutableStats().getDamageToFighters());

                    fx_drone.getMutableStats().getDamageToMissiles().applyMods(weapon.getShip().getMutableStats().getDamageToMissiles());
                    fx_drone.getMutableStats().getDamageToTargetEnginesMult().applyMods(weapon.getShip().getMutableStats().getDamageToTargetEnginesMult());
                    fx_drone.getMutableStats().getDamageToTargetHullMult().applyMods(weapon.getShip().getMutableStats().getDamageToTargetHullMult());
                    fx_drone.getMutableStats().getDamageToTargetShieldsMult().applyMods(weapon.getShip().getMutableStats().getDamageToTargetShieldsMult());
                    fx_drone.getMutableStats().getDamageToTargetWeaponsMult().applyMods(weapon.getShip().getMutableStats().getDamageToTargetWeaponsMult());

                    fx_drone.getMutableStats().getBallisticWeaponRangeBonus().applyMods(weapon.getShip().getMutableStats().getBallisticWeaponRangeBonus());
                    fx_drone.getMutableStats().getMissileWeaponRangeBonus().applyMods(weapon.getShip().getMutableStats().getMissileWeaponRangeBonus());
                    fx_drone.getMutableStats().getEnergyWeaponRangeBonus().applyMods(weapon.getShip().getMutableStats().getEnergyWeaponRangeBonus());
                    fx_drone.getMutableStats().getBeamWeaponRangeBonus().applyMods(weapon.getShip().getMutableStats().getBeamWeaponRangeBonus());
                    fx_drone.getMutableStats().getBeamPDWeaponRangeBonus().applyMods(weapon.getShip().getMutableStats().getBeamPDWeaponRangeBonus());
                    fx_drone.getMutableStats().getWeaponRangeThreshold().applyMods(weapon.getShip().getMutableStats().getWeaponRangeThreshold());
                }

                weapon.ensureClonedSpec();

                var spec = (BeamWeaponSpecAPI) weapon.getSpec();
                spec.setChargeupTime(0);
                spec.setChargedownTime(0);
                spec.setAmmoPerSecond(1000f);

                spec.getHiddenFireOffsets().clear();
                spec.getHiddenFireOffsets().add(new Vector2f(3,0));
                spec.getHiddenAngleOffsets().clear();
                spec.getHiddenAngleOffsets().add(0f);

                spec.getTurretFireOffsets().clear();
                spec.getTurretFireOffsets().add(new Vector2f(3,0));
                spec.getTurretAngleOffsets().clear();
                spec.getTurretAngleOffsets().add(0f);

                spec.getHardpointFireOffsets().clear();
                spec.getHardpointFireOffsets().add(new Vector2f(3,0));
                spec.getHardpointAngleOffsets().clear();
                spec.getHardpointAngleOffsets().add(0f);

                fx_drone.setCollisionClass(CollisionClass.NONE);
                fx_drone.giveCommand(ShipCommand.SELECT_GROUP, null, 0);

                Global.getCombatEngine().addEntity(this.fx_drone);
                fx_drone.setOwner(ship.getOwner());
                fx_drone.setDoNotRender(true);
                beam_drones.add(this);
                fx_drone.giveCommand(ShipCommand.FIRE, beam.getFrom(), 0);
            }

            public void update(ReflectionData reflection) {
                fx_drone.getLocation().set(reflection.point);
                fx_drone.setFacing(reflection.newFacing);
                fx_drone.getVelocity().set(ship.getVelocity());
                fx_drone.setAngularVelocity(ship.getAngularVelocity());

                Vector2f dir = Misc.getUnitVectorAtDegreeAngle(reflection.newFacing);
                dir.scale(1000f);
                Vector2f.add(dir, reflection.point, dir);
                fx_drone.getMouseTarget().set(dir);

                WeaponAPI tLaser = fx_drone.getWeaponGroupsCopy().get(0).getWeaponsCopy().get(0);
                tLaser.setFacing(reflection.newFacing);
                tLaser.updateBeamFromPoints();
            }
        }

        @Override
        public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (!shieldHit) return id;

            if (param instanceof MissileAPI proj) {
                if (proj.getCustomData().containsKey(KEY)) return id;
                var weapon = proj.getWeapon();
                if (weapon == null) return id;

                var ReflectionData = calculateReflection(point, proj.getVelocity(), ship);
                MissileAPI newProj = spawnMissile(ship, weapon, proj.getSpec(), point, ReflectionData.newFacing, ship.getVelocity());
                if (newProj.getSpec().getOnFireEffect() != null) newProj.getSpec().getOnFireEffect().onFire(newProj, weapon, Global.getCombatEngine());
                if (newProj.getSource() != null) newProj.setHitpoints(newProj.getSource().getMutableStats().getMissileHealthBonus().computeEffective(newProj.getHitpoints()));
                newProj.setDamageAmount(proj.getDamageAmount());

                newProj.getVelocity().set(ReflectionData.vector);
                newProj.setCustomData(KEY, true);
                newProj.setOwner(ship.getOwner());
            } else if (param instanceof DamagingProjectileAPI proj) {
                if (proj.getCustomData().containsKey(KEY)) return id;
                var weapon = proj.getWeapon();
                if (weapon == null) return id;

                var ReflectionData = calculateReflection(point, proj.getVelocity(), ship);
                DamagingProjectileAPI newProj = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(ship, weapon, weapon.getId(), point, ReflectionData.newFacing, ship.getVelocity());
                newProj.getVelocity().set(ReflectionData.vector);
                newProj.setCustomData(KEY, true);
                newProj.setOwner(100);
            } else if (param instanceof BeamAPI beam) {
                var weapon = beam.getWeapon();
                if (weapon == null) return id;

                var enemy = weapon.getShip();
                if (enemy == null) return id;
                if (Objects.equals(enemy.getHullSpec().getHullId(), "jdp_fxdrone")) return id;

                if (new ArrayList<>(beam_drones).stream().noneMatch(drone -> drone.beam == beam)) {
                    new BeamReflectionDrone(beam, ship);
                }
            }
            return id;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused()) return;
            if (visuals == null) {
                visuals = new jdp_mirrorShieldRender(ship);
                Global.getCombatEngine().addLayeredRenderingPlugin(visuals);
            }

            if (ship.getShield().getArc() > ARC) ship.getShield().setArc(ARC);

            for (BeamReflectionDrone drone : new ArrayList<>(beam_drones)) {
                var beam = drone.beam;
                var weapon = beam.getWeapon();

                drone.update(calculateReflection(beam.getRayEndPrevFrame(), new Vector2f(beam.getRayEndPrevFrame().x - beam.getRayEndPrevFrame().x, beam.getRayEndPrevFrame().y - beam.getFrom().y), ship));
                if (!beam.getWeapon().isBurstBeam() || drone.fx_drone.getAllWeapons().get(0).usesAmmo()) {
                    drone.fx_drone.giveCommand(ShipCommand.FIRE, drone.beam.getFrom(), 0);
                }

                if (!weapon.isFiring() || ship.getShield().isOff() || !weapon.getShip().isAlive() || !isPointOnShield(ship, beam.getRayEndPrevFrame())) {
                    beam_drones.remove(drone);
                    Global.getCombatEngine().removeEntity(drone.fx_drone);
                }
            }

            phase += amount * direction * shiftRate;
            while (phase > 1) {
                direction = -1;
                phase = 1;
            }
            while (phase < 0) {
                direction = 1;
                phase = 0;
            }

            int ring_red = RING_COLOR_MAIN.getRed();
            int ring_green = (int) (RING_COLOR_MAIN.getGreen() + (Math.floor(phase * (255 - RING_COLOR_MAIN.getGreen()))));
            int ring_blue = (int) (RING_COLOR_MAIN.getBlue() + (Math.floor(phase * 0.5f * (255 - RING_COLOR_MAIN.getBlue()))));

            int inner_red = (int) (INNER_COLOR_MAIN.getRed() + (Math.floor(phase * 0.5f * (255 - INNER_COLOR_MAIN.getRed()))));
            int inner_green = (int) (INNER_COLOR_MAIN.getGreen() + (Math.floor(phase * (255 - INNER_COLOR_MAIN.getGreen()))));
            int inner_blue = INNER_COLOR_MAIN.getBlue();

            var ring = new Color(ring_red, ring_green, ring_blue, RING_COLOR_MAIN.getAlpha());
            var inner = new Color(inner_red, inner_green, inner_blue, INNER_COLOR_MAIN.getAlpha());
            ship.getShield().applyShieldEffects(inner, ring, (6.5f + phase), 3f, 10f);
        }
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getListeners(MirrorShieldDamageTaken.class).isEmpty()) {
            ship.addListener(new MirrorShieldDamageTaken(ship, id));
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        Color h = Misc.getHighlightColor();
        float HEIGHT = 50f;
        float PAD = 10f;


        TooltipMakerAPI proj = tooltip.beginImageWithText(Global.getSettings().getSpriteName("ui", "jdp_icon_dual"), HEIGHT);
        proj.addPara("Projectiles/Beams", h, 0f);
        proj.addPara("Projectiles and beams are reflected opposite to their incident axis", 0f, h, "+20%");

        tooltip.addImageWithText(PAD);

        TooltipMakerAPI missile = tooltip.beginImageWithText(Global.getSettings().getSpriteName("codex", "manual_missiles"), HEIGHT);
        missile.addPara("Missiles", h, 0f);
        missile.addPara("Missile guidance systems are compromised upon contact, re-targeting to their source", 0f, h);

        tooltip.addImageWithText(PAD);
        tooltip.addSpacer(10f);

        tooltip.addPara("%s of damage is dealt by the redirected projectile. The exertion of equivalent energy at both the original and reflected impact site implies a violation of more fundamental laws.", 0f, h, "100%");
        tooltip.addSpacer(10f);

        tooltip.addPara("The arc of these exotic shields cannot be improved beyond %s degrees.", 0f, h, ""+ARC);
    }


    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "100%";
        return null;
    }
}






