package Jaydee8652.JaydeePiracy.scripts.systems.combat_lamp_render;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.terrain.RangeBlockerUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.EnumSet;

@SuppressWarnings("ConstantValue")
public class jdp_CombatPulsarRenderer extends BaseCombatLayeredRenderingPlugin {
    public interface CombatPulsarRendererDelegate {
        SpriteAPI getPulsarTexture();
        SpriteAPI getAuroraTexture();
        jdp_CombatRangeBlockerUtil getBlocker();

        Vector2f getPulsarCenterLoc();
        Color getPulsarColor();
        float getFXMult();

        float getPulsarInnerRadius();
        float getPulsarOuterRadius();

        float getPulsarInnerWidth();
        float getPulsarOuterWidth();

        float getPulsarScrollSpeed();

        float getAuroraShortenMult(float angle);
        float getAuroraInnerOffsetMult(float angle);

        float getAuroraThicknessMult(float angle);
        float getAuroraThicknessFlat(float angle);
    }

    @Override
    public float getRenderRadius() {
        return delegate.getPulsarOuterRadius()*10;
    }

    protected float alphaMult = 0.4f;
    private final CombatPulsarRendererDelegate delegate;
    private float texOffset = 0f;
    private float phaseAngle;


    public jdp_CombatPulsarRenderer(CombatPulsarRendererDelegate delegate) {
        this.delegate = delegate;
    }

    private float currAngle;

    public void setCurrAngle(float definedAngle) {
        this.currAngle = definedAngle;
    }

    public void init(CombatEntityAPI entity) {
        super.init(entity);
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) return;

        texOffset += amount * delegate.getPulsarScrollSpeed() / delegate.getPulsarTexture().getWidth();
        while (texOffset > 1) texOffset--;

        float days = Global.getSector().getClock().convertToDays(amount);
        phaseAngle += days * 360f * 0.5f;
        phaseAngle = Misc.normalizeAngle(phaseAngle);
    }

    @SuppressWarnings("DataFlowIssue")
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        this.alphaMult = viewport.getAlphaMult() * delegate.getFXMult();
        if (viewport.getAlphaMult() <= 0) return;

        //Beam
        float distClose = delegate.getPulsarInnerRadius();
        float distFar = delegate.getPulsarOuterRadius();

        if (distFar < distClose + 10f) distFar = distClose + 10f;

        float length = distFar - distClose;

        float wClose = delegate.getPulsarInnerWidth();
        float wFar = delegate.getPulsarOuterWidth();

        float pixelsPerSegment = 25f;
        float beamSegments = Math.round(wFar / 25f);

        Vector2f loc = delegate.getPulsarCenterLoc();
        float x = loc.x;
        float y = loc.y;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        float beamTexHeight = delegate.getPulsarTexture().getTextureHeight();
        float beamImageHeight = delegate.getPulsarTexture().getHeight();
        float beamTexPerSegment = beamTexHeight / beamSegments;

        float beamTexWidth = delegate.getPulsarTexture().getTextureWidth();
        float beamImageWidth = delegate.getPulsarTexture().getWidth();

        jdp_CombatRangeBlockerUtil blocker = delegate.getBlocker();

        float widthFactor = ((wClose + wFar) / 2f) / (beamImageHeight * beamTexHeight);
        float texPerUnitLength = 1f / (beamImageWidth * widthFactor);

        float angle = currAngle;

        float fadeInDist = Math.min(500f, length * 0.25f);
        float fadeOutDist = Math.min(1500f, length * 0.25f);

        float [] rPrev = new float [(int) beamSegments + 1];
        float [] blockedPrev = new float [(int) beamSegments + 1];

        float [] xPrev = new float [(int) beamSegments + 1];
        float [] yPrev = new float [(int) beamSegments + 1];

        float [] texPrev = new float [(int) beamSegments + 1];

        int numInnerSegments = 1;
        int numSegments = 2 + numInnerSegments;
        float distPerInnerSegment = (length - fadeInDist - fadeOutDist) / (float) numInnerSegments;

        delegate.getPulsarTexture().bindTexture();

        for (int j = 0; j < numSegments; j++) {
            boolean isFirst = j == 0;
            boolean isLast = j == numSegments - 1;
            boolean isMid = !isFirst && !isLast;

            float alphaCloser = 1f;
            float alphaFarther = 1f;
            float r1 = distClose;
            float r2 = distFar;

            if (isFirst) {
                alphaCloser = 0f;
                alphaFarther = 1f;

                r1 = distClose;
                r2 = distClose + fadeInDist;
            } else if (isMid) {
                alphaCloser = 1f;
                alphaFarther = 1f;

                r1 = distClose + (j - 1) * distPerInnerSegment + fadeInDist;
                r2 = r1 + distPerInnerSegment;
            } else if (isLast) {
                alphaCloser = 1f;
                alphaFarther = 0f;

                r1 = distFar - fadeOutDist;
                r2 = distFar;
            }


            float w1 = wClose + (wFar - wClose) * (r1 - distClose) / length;
            float w2 = wClose + (wFar - wClose) * (r2 - distClose) / length;

            float arcClose = (float) Math.toRadians(Misc.computeAngleSpan(w1 / 2f, r1));
            float arcFar = (float) Math.toRadians(Misc.computeAngleSpan(w2 / 2f, r2));

            float closeAnglePerSegment = arcClose / beamSegments;
            float farAnglePerSegment = arcFar / beamSegments;

            float currCloseAngle = (float) Math.toRadians(angle) - arcClose / 2f;
            float currFarAngle = (float) Math.toRadians(angle) - arcFar / 2f;

            // horizontal, i.e. along width of beam
            float texProgress = 0f;

            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (float i = 0; i < beamSegments + 1; i++) {
                float blockedAt = 1f;
                float blockerMax = 100000f;
                if (isMid && blocker != null) {
                    blockerMax = blocker.getCurrMaxAt((float) Math.toDegrees((currCloseAngle)));
                    if (blockerMax > blocker.getMaxRange()) {
                        blockerMax = blocker.getMaxRange();
                    }
                    if (blockerMax < fadeInDist + 100) {
                        blockerMax = fadeInDist + 100;
                    }
                    blockedAt = (blockerMax - r1) / (r2 - r1);
                    if (blockedAt > 1) blockedAt = 1;
                    if (blockedAt < 0) blockedAt = 0;

                    rPrev[(int) i] = Math.min(r2, blockerMax);
                    blockedPrev[(int) i] = blockedAt;
                }

                float curr1 = r1;
                float curr2 = r2;

                float extraAlpha = 1f;
                if (isLast) {
                    curr1 = rPrev[(int) i];
                    float block = blockedPrev[(int) i];
                    curr2 = curr1 + Math.max(300f, fadeOutDist * block);

                    w2 = wClose + (wFar - wClose) * (curr2 - distClose) / length;
                    arcFar = (float) Math.toRadians(Misc.computeAngleSpan(w2 / 2f, curr2));
                    farAnglePerSegment = arcFar / beamSegments;
                    currFarAngle = (float) Math.toRadians(angle) - arcFar / 2f + farAnglePerSegment * i;
                }


                float cosClose = (float) Math.cos(currCloseAngle);
                float sinClose = (float) Math.sin(currCloseAngle);

                float cosFar = (float) Math.cos(currFarAngle);
                float sinFar = (float) Math.sin(currFarAngle);

                float x1 = cosClose * curr1;
                float y1 = sinClose * curr1;
                float x2 = cosFar * curr2;
                float y2 = sinFar * curr2;

                if (isMid || isLast) {
                    x1 = xPrev[(int) i];
                    y1 = yPrev[(int) i];
                }

                x2 = x1 + (x2 - x1) * blockedAt;
                y2 = y1 + (y2 - y1) * blockedAt;

                xPrev[(int) i] = x2;
                yPrev[(int) i] = y2;

                float closeTX = beamTexWidth * texPerUnitLength * (curr1 - distClose) - texOffset;
                float farTX = beamTexWidth * texPerUnitLength * ((curr1 + (curr2 - curr1) * blockedAt) - distClose) - texOffset;

                if (isMid || isLast) {
                    closeTX = texPrev[(int) i];
                }
                texPrev[(int) i] = farTX;

                float edgeMult = 1f;
                float max = 10;
                if (i < max) {
                    edgeMult = i / max;
                } else if (i > beamSegments - 1 - max) {
                    edgeMult = 1f - (i - (beamSegments - max)) / max;
                }

                Color color = delegate.getPulsarColor();

                GL11.glColor4ub(
                        (byte) color.getRed(),//Multiply by texProgress to make a cool aurora effect!
                        (byte) color.getGreen(),
                        (byte) color.getBlue(),
                        (byte) ((float) color.getAlpha() * alphaMult * alphaCloser * edgeMult));
                GL11.glTexCoord2f(closeTX, texProgress);
                GL11.glVertex2f(x1, y1);

                GL11.glColor4ub(
                        (byte) color.getRed(),
                        (byte) color.getGreen(),
                        (byte) color.getBlue(),
                        (byte) ((float) color.getAlpha() * alphaMult * alphaFarther * edgeMult));
                GL11.glTexCoord2f(farTX, texProgress);
                GL11.glVertex2f(x2, y2);

                texProgress += beamTexPerSegment;
                currCloseAngle += closeAnglePerSegment;
                currFarAngle += farAnglePerSegment;
            }
            GL11.glEnd();
        }

        GL11.glPopMatrix();

        //Aurora
        /*
        float bandWidthInTexture = 256;
        float bandIndex;

        float radStart = delegate.getPulsarInnerRadius();
        float radEnd = delegate.getPulsarInnerRadius() + 80f;

        if (radEnd < radStart + 10f) radEnd = radStart + 10f;

        float circ = (float) (Math.PI * 2f * (radStart + radEnd) / 2f);
        float auroraSegments = Math.round(circ / 50f);

        float startRad = (float) Math.toRadians(0);
        float endRad = (float) Math.toRadians(360f);
        float spanRad = Math.abs(endRad - startRad);
        float anglePerSegment = spanRad / auroraSegments;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        delegate.getAuroraTexture().bindTexture();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        float thickness = (radEnd - radStart);

        float texProgress = 0f;
        float texHeight = delegate.getAuroraTexture().getTextureHeight();
        float imageHeight = delegate.getAuroraTexture().getHeight();
        float texPerSegment = pixelsPerSegment * texHeight / imageHeight * bandWidthInTexture / thickness;

        float totalTex = Math.max(1f, Math.round(texPerSegment * auroraSegments));
        texPerSegment = totalTex / auroraSegments;

        float texWidth = delegate.getAuroraTexture().getTextureWidth();
        float imageWidth = delegate.getAuroraTexture().getWidth();

        for (int iter = 0; iter < 2; iter++) {
            if (iter == 0) {
                bandIndex = 1;
            } else {
                bandIndex = 0;
            }

            float leftTX = (float) bandIndex * texWidth * bandWidthInTexture / imageWidth;
            float rightTX = (float) (bandIndex + 1f) * texWidth * bandWidthInTexture / imageWidth - 0.001f;

            GL11.glBegin(GL11.GL_QUAD_STRIP);
            for (float i = 0; i < auroraSegments + 1; i++) {

                float segIndex = i % (int) auroraSegments;

                float phaseAngleRad;
                if (iter == 0) {
                    phaseAngleRad = (float) Math.toRadians(phaseAngle) + (segIndex * anglePerSegment * 10f);
                } else {
                    phaseAngleRad = (float) Math.toRadians(-phaseAngle) + (segIndex * anglePerSegment * 5f);
                }


                float auroraAngle = (float) Math.toDegrees(segIndex * anglePerSegment);
                if (iter == 1) auroraAngle += 180;

                float blockerMax = 100000f;
                if (blocker != null) {
                    blockerMax = blocker.getCurrMaxAt(auroraAngle);
                    blockerMax *= 0.75f;
                    if (blockerMax > blocker.getMaxRange()) {
                        blockerMax = blocker.getMaxRange();
                    }
                }

                float pulseSin = (float) Math.sin(phaseAngleRad);
                float pulseMax = thickness * delegate.getAuroraShortenMult(auroraAngle);

                if (pulseMax > blockerMax * 0.5f) {
                    pulseMax = blockerMax * 0.5f;
                }
                float pulseAmount = pulseSin * pulseMax;
                float pulseInner = pulseAmount * 0.1f;
                pulseInner *= delegate.getAuroraInnerOffsetMult(auroraAngle);

                float thicknessMult = delegate.getAuroraThicknessMult(auroraAngle);
                float thicknessFlat = delegate.getAuroraThicknessFlat(auroraAngle);

                float theta = anglePerSegment * segIndex;;
                float cos = (float) Math.cos(theta);
                float sin = (float) Math.sin(theta);

                float rInner = radStart - pulseInner;
                if (rInner < radStart * 0.9f) rInner = radStart * 0.9f;

                float rOuter = (radStart + thickness * thicknessMult - pulseAmount + thicknessFlat);

                if (blocker != null) {
                    if (rOuter > blockerMax - pulseAmount) {
                        rOuter = blockerMax - pulseAmount;
                        if (rOuter < radStart) rOuter = radStart;
                    }
                    if (rInner > rOuter) {
                        rInner = rOuter;
                    }
                }

                float x1 = cos * rInner;
                float y1 = sin * rInner;
                float x2 = cos * rOuter;
                float y2 = sin * rOuter;

                x2 += (float) (Math.cos(phaseAngleRad) * pixelsPerSegment * 0.33f);
                y2 += (float) (Math.sin(phaseAngleRad) * pixelsPerSegment * 0.33f);

                Color color = delegate.getPulsarColor();
                GL11.glColor4ub(
                        (byte)color.getRed(),
                        (byte)color.getGreen(),
                        (byte)color.getBlue(),
                        (byte)((float) color.getAlpha() * alphaMult * 0.5f));
                GL11.glTexCoord2f(leftTX, texProgress);
                GL11.glVertex2f(x1, y1);
                GL11.glTexCoord2f(rightTX, texProgress);
                GL11.glVertex2f(x2, y2);

                texProgress += texPerSegment;
            }
            GL11.glEnd();

            GL11.glRotatef(180, 0, 0, 1);
        }
        GL11.glPopMatrix();
        */
    }

    public static class jdp_CombatRangeBlockerUtil {

        private final int resolution;
        private final float maxRange;

        private final float degreesPerUnit;
        private final float [] limits;
        private final float [] curr;

        private boolean wasUpdated = false;

        public jdp_CombatRangeBlockerUtil(int resolution, float maxRange) {
            this.resolution = resolution;
            this.maxRange = maxRange;

            degreesPerUnit = 360f / (float) resolution;

            limits = new float [resolution];
            curr = new float [resolution];
        }

        public boolean wasEverUpdated() {
            return wasUpdated;
        }

        public void updateAndSync(CombatEntityAPI entity, CombatEntityAPI exclude, float diffMult) {
            updateLimits(entity, exclude, diffMult);
            sync();
        }

        public void sync() {
            if (resolution >= 0) System.arraycopy(limits, 0, curr, 0, resolution);
        }

        public float getCurrMaxAt(float angle) {
            angle = Misc.normalizeAngle(angle);

            float index = angle / 360f * resolution;
            int i1 = (int) Math.floor(index);
            int i2 = (int) Math.ceil(index);
            while (i1 >= resolution) i1 -= resolution;
            while (i2 >= resolution) i2 -= resolution;

            float v1 = curr[i1];
            float v2 = curr[i2];

            return v1 + (v2 - v1) * (index - (int) index);
        }

        public void advance(float amount, float minApproachSpeed, float diffMult) {
            for (int i = 0; i < resolution; i++) {
                curr[i] = Misc.approach(curr[i], limits[i], minApproachSpeed, diffMult, amount);
            }
        }

        public void updateLimits(CombatEntityAPI entity, CombatEntityAPI exclude, float diffMult) {
            for (int i = 0; i < resolution; i++) {
                limits[i] = maxRange;
            }

            for (ShipAPI ent : Global.getCombatEngine().getShips()) {
                if (ent == entity || ent == exclude || ent.isFighter() || ent.isPhased()) continue;
                float dist = Misc.getDistance(entity.getLocation(), ent.getLocation());
                if (dist > maxRange) continue;

                float graceRadius = 25f;
                float span = Misc.computeAngleSpan(ent.getCollisionRadius() + graceRadius, dist);

                if (!ent.isAlive()) span *= 0.35f;

                float angle = Misc.getAngleInDegrees(entity.getLocation(), ent.getLocation());

                float offsetSize = maxRange * 0.1f;

                for (float f = angle - span/2f; f <= angle + span/2f; f += degreesPerUnit) {
                    float offset = Math.abs(f - angle) / (span / 2f);
                    if (offset > 1) offset = 1;
                    offset = (1f - (float) Math.cos(offset * 3.1416f / 2f));
                    offset *= offset;
                    offset *= offsetSize;


                    int index = getIndexForAngle(f);
                    limits[index] = Math.min(dist - (ent.getCollisionRadius()) * 0.5f + offset, limits[index]);
                }
            }

            for (CombatEntityAPI ent : Global.getCombatEngine().getAsteroids()) {
                if (ent == entity || ent == exclude) continue;
                float dist = Misc.getDistance(entity.getLocation(), ent.getLocation());
                if (dist > maxRange) continue;

                float graceRadius = 35f;
                float span = Misc.computeAngleSpan(ent.getCollisionRadius()*0.8f + graceRadius, dist);

                float angle = Misc.getAngleInDegrees(entity.getLocation(), ent.getLocation());

                float offsetSize = maxRange * 0.1f;

                for (float f = angle - span/2f; f <= angle + span/2f; f += degreesPerUnit) {
                    float offset = Math.abs(f - angle) / (span / 2f);
                    if (offset > 1) offset = 1;
                    offset = (1f - (float) Math.cos(offset * 3.1416f / 2f));
                    offset *= offset;
                    offset *= offsetSize;


                    int index = getIndexForAngle(f);
                    limits[index] = Math.min(dist - (ent.getCollisionRadius()) * 0.5f + offset, limits[index]);
                }
            }

            wasUpdated = true;
        }

        public int getIndexForAngle(float angle) {
            angle = Misc.normalizeAngle(angle);

            int index = Math.round(angle / 360f * (float) resolution);
            if (index < 0) index = 0;
            while (index >= resolution) index -= resolution;

            return index;
        }

        public float getMaxRange() {
            return maxRange;
        }
    }
}
