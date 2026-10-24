package Jaydee8652.JaydeePiracy.hullmods;

import Jaydee8652.JaydeePiracy.campaign.entities.jdp_FusionLampEntityPlugin;
import Jaydee8652.JaydeePiracy.scripts.systems.combat_lamp_render.jdp_CombatPulsarRenderer;
import Jaydee8652.JaydeePiracy.scripts.systems.combat_lamp_render.jdp_CombatPulsarRenderer.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.terrain.FlareManager;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.List;

public class jdp_fusionBurn extends BaseHullMod implements jdp_CombatPulsarRenderer.CombatPulsarRendererDelegate, FlareManager.FlareManagerDelegate {
    Color color = Misc.setAlpha(jdp_FusionLampEntityPlugin.GLOW_COLOR, 255);
    public static final Color JITTER_COLOR = new Color(255, 165, 90, 55);
    public static final Color JITTER_UNDER_COLOR = new Color(255, 165, 90, 155);

    float maxWindBurn = 1f;
    float windMult = 5000f;

    public ShipAPI ship = null;

    protected CombatEntityAPI entity;

    public float PULSAR_ARC = 90f;
    public static final float PULSAR_LENGTH = 4000f;
    public float fxMult = 0.85f;//fxMult = 0.2f;
    public float scrollSpeed = 10f;

    protected SpriteAPI flareTexture = Global.getSettings().getSprite("terrain", "pulsar");
    protected SpriteAPI auroraTexture = Global.getSettings().getSprite("terrain", "aurora");

    protected jdp_CombatPulsarRenderer flare1, flare2;
    protected jdp_CombatRangeBlockerUtil blocker = null;
    protected FlareManager flareManager = new FlareManager(this);

    protected float pulsarAngle = 60f;
    protected float rotationRate = 5f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getShipAI() != null) init(ship);

        if (!ship.isAlive() || ship.isHulk()) return;

        ship.getLocation().set(0, 3000);

        pulsarAngle += rotationRate * amount;
        pulsarAngle = Misc.normalizeAngle(pulsarAngle);

        flare1.setCurrAngle(pulsarAngle);
        flare2.setCurrAngle(pulsarAngle + 180f);

        flare1.advance(amount);
        flare2.advance(amount);

        if (amount > 0 && blocker != null) {
            blocker.updateLimits(entity, entity, 0.5f);
            blocker.advance(amount, 100f, 0.5f);
        }

        render(CombatEngineLayers.BELOW_SHIPS_LAYER, Global.getCombatEngine().getViewport());

        ship.setJitterUnder(this, JITTER_UNDER_COLOR, 1f, 25, 0f, 7f);
        ship.setJitter(this, JITTER_COLOR, 1f, 2, 0f, 5f);

        applyEffect(ship, amount);
    }

    public void init(ShipAPI ship) {
        this.ship = ship;
        this.entity = ship.getMutableStats().getEntity();

        ship.setShipAI(null);
        ship.getLocation().set(0, 3000);

        if (blocker == null) {
            blocker = new jdp_CombatRangeBlockerUtil(2000, PULSAR_LENGTH * 2f);
        }

        flare1 = new jdp_CombatPulsarRenderer(this);
        flare2 = new jdp_CombatPulsarRenderer(this);

        Global.getCombatEngine().addLayeredRenderingPlugin(flare1);
        Global.getCombatEngine().addLayeredRenderingPlugin(flare2);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (blocker != null && !blocker.wasEverUpdated()) {
            blocker.updateAndSync(entity, entity, 1f);
        }

        flare1.render(layer, viewport);
        flare2.render(layer, viewport);
    }

    public void applyEffect(CombatEntityAPI entity, float amount) {
        List<ShipAPI> ships = Global.getCombatEngine().getShips();
        for (ShipAPI other : ships) {
            if (other == ship || ship.getChildModulesCopy().contains(other)) continue;
            float intensity = getIntensityAtPoint(other.getLocation());
            if (other.getVariant().hasHullMod(HullMods.SOLAR_SHIELDING)) intensity *= 0.5f;

            Vector2f targetPoint = null;
            for (int i = 0; i < 100; i++) {
                while (targetPoint == null) {
                    Vector2f potentialPoint = MathUtils.getRandomPointInCircle(other.getLocation(), other.getCollisionRadius());
                    if (CollisionUtils.isPointWithinBounds(potentialPoint, other)) targetPoint = potentialPoint;
                }
            }
            Global.getCombatEngine().applyDamage(other, targetPoint, 5 + (20f * (intensity / 5 + intensity)), DamageType.ENERGY, 0, true, true, ship);
            //Global.getCombatEngine().applyDamage(other, targetPoint, 50f, DamageType.ENERGY, 0, true, true, ship);
        }

        List<CombatEntityAPI> push = CombatUtils.getEntitiesWithinRange(ship.getLocation(), PULSAR_LENGTH * 0.95f);
        for (CombatEntityAPI tmp : push) {
            if (tmp == ship) continue;

            float intensity = getIntensityAtPoint(tmp.getLocation());
            float currWindBurn = intensity * maxWindBurn;

            if (intensity <= 0) return;

            // "wind" effect - adjust velocity
            float currSpeed = tmp.getVelocity().length();

            float angle = Misc.getAngleInDegreesStrict(this.entity.getLocation(), tmp.getLocation());
            Vector2f windDir = Misc.getUnitVectorAtDegreeAngle(angle);

            if (currWindBurn < 0) windDir.negate();

            Vector2f velDir = Misc.normalise(new Vector2f(tmp.getVelocity()));
            velDir.scale(currSpeed);

            Vector2f vel = tmp.getVelocity();
            windDir.scale((float) (intensity * windMult / Math.pow(tmp.getMass(), 0.5)));
            tmp.getVelocity().set(vel.x + windDir.x, vel.y + windDir.y);
        }
    }

    public float getIntensityAtPoint(Vector2f point) {
        float maxDist = PULSAR_LENGTH;
        float minDist = ship.getCollisionRadius();
        float dist = Misc.getDistance(point, ship.getLocation());

        if (dist > maxDist) return 0f;

        float intensity = 1f;
        if (minDist < maxDist) {
            intensity = 1f - ((dist - minDist) / (maxDist - minDist));
            if (intensity < 0) intensity = 0;
            if (intensity > 1) intensity = 1;
        }

        float angle = Misc.getAngleInDegreesStrict(ship.getLocation(), point);
        float diff = Math.min(Misc.getAngleDiff(angle, pulsarAngle), Misc.getAngleDiff(angle, pulsarAngle + 180f));

        if (diff > (PULSAR_ARC / 2f)) intensity = 0f;
        return intensity;
    }

    public Vector2f getPulsarCenterLoc() {
        return getRelatedEntity().getLocation();
    }

    public jdp_CombatRangeBlockerUtil getBlocker() {
        return blocker;
    }

    public float getFXMult() {
        return fxMult;
    }

    public Color getPulsarColor() {
        return color;
    }

    public float getPulsarInnerRadius() {
        return getRelatedEntity().getCollisionRadius();
    }

    public float getPulsarOuterRadius() {
        return ship.getCollisionRadius() + 50f + PULSAR_LENGTH;
    }

    public float getPulsarInnerWidth() {
        return PULSAR_ARC / 360f * 2f * (float) Math.PI * getPulsarInnerRadius();
    }

    public float getPulsarOuterWidth() {
        float r1 = getPulsarInnerRadius();
        float r2 = getPulsarOuterRadius();
        return getPulsarInnerWidth() * r2 / r1;
    }

    public float getPulsarScrollSpeed() {
        return scrollSpeed;
    }

    public float getAuroraShortenMult(float angle) {
        return 0.85f + flareManager.getShortenMod(angle);
    }

    public float getAuroraInnerOffsetMult(float angle) {return flareManager.getInnerOffsetMult(angle);}

    public float getAuroraThicknessFlat(float angle) {
        if (flareManager.isInActiveFlareArc(angle)) {
            return flareManager.getExtraLengthFlat(angle);
        }
        return 0;
    }

    public float getAuroraThicknessMult(float angle) {
        if (flareManager.isInActiveFlareArc(angle)) {
            return flareManager.getExtraLengthMult(angle);
        }
        return 1f;
    }
    public CombatEntityAPI getRelatedEntity() {return entity;}

    public SpriteAPI getPulsarTexture() {return flareTexture;}
    public SpriteAPI getAuroraTexture() {return auroraTexture;}

    public float getFlareArcMax() {
        return 60;
    }

    public float getFlareArcMin() {
        return 30;
    }

    public float getFlareExtraLengthFlatMax() {
        return 70;
    }

    public float getFlareExtraLengthFlatMin() {
        return 20;
    }

    public float getFlareExtraLengthMultMax() {
        return 1.2f;
    }

    public float getFlareExtraLengthMultMin() {
        return 1;
    }

    public float getFlareFadeInMax() {
        return 10f;
    }

    public float getFlareFadeInMin() {
        return 3f;
    }

    public float getFlareFadeOutMax() {
        return 10f;
    }

    public float getFlareFadeOutMin() {
        return 3f;
    }

    public float getFlareOccurrenceAngle() {
        return 0;
    }

    public float getFlareOccurrenceArc() {
        return 360f;
    }

    public float getFlareProbability() {
        return 0.2f;//params.flareProbability;
    }

    public float getFlareSmallArcMax() {
        return 15;
    }

    public float getFlareSmallArcMin() {
        return 5;
    }

    public float getFlareSmallExtraLengthFlatMax() {
        return 40;
    }

    public float getFlareSmallExtraLengthFlatMin() {
        return 10;
    }

    public float getFlareSmallExtraLengthMultMax() {
        return 1.05f;
    }

    public float getFlareSmallExtraLengthMultMin() {
        return 1;
    }

    public float getFlareSmallFadeInMax() {
        return 2f;
    }

    public float getFlareSmallFadeInMin() {
        return 1f;
    }

    public float getFlareSmallFadeOutMax() {
        return 2f;
    }

    public float getFlareSmallFadeOutMin() {
        return 1f;
    }

    public float getFlareShortenFlatModMax() {
        return 0.05f;
    }

    public float getFlareShortenFlatModMin() {
        return 0.05f;
    }

    public float getFlareSmallShortenFlatModMax() {
        return 0.05f;
    }

    public float getFlareSmallShortenFlatModMin() {
        return 0.05f;
    }

    public int getFlareMaxSmallCount() {
        return 3;
    }

    public int getFlareMinSmallCount() {
        return 1;
    }

    public float getFlareSkipLargeProbability() {
        return 0f;
    }
    public SectorEntityToken getFlareCenterEntity() {
        return null;
    }

    public List<Color> getFlareColorRange() {
        return List.of();
    }
}