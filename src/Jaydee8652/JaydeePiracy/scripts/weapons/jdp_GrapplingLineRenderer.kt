package Jaydee8652.JaydeePiracy.scripts.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class jdp_GrapplingLineRenderer(var weapon: WeaponAPI, var projectile: DamagingProjectileAPI) : BaseCombatLayeredRenderingPlugin() {
    var sprite = Global.getSettings().getSprite(Global.getSettings().getCommoditySpec(Commodities.ALPHA_CORE).iconName)
    var currentPoints = ArrayList<Vector2f>()
    var previousPoints = ArrayList<Vector2f>()
    var velocities = ArrayList<Vector2f>()

    var normalLength = 1f
    var dampening = 0.99f

    var resistance = 0.0f
    var destructible = true

    var pointCount = Math.round(weapon.range / 50)//30

    var target: CombatEntityAPI? = null
    var targetPoint = Vector2f()

    var firstFrame = true
    var hadTarget = false

    var fade = 1f
    var lineColor = Misc.optColor(projectile.projectileSpec.behaviorJSON, "lineColor", null);

    init {
        for (i in 0 until pointCount) {

            var loc = (weapon.location.plus(Vector2f(1f * i, 1f * i)))

            currentPoints.add(Vector2f(loc))
            previousPoints.add(Vector2f(loc))
            velocities.add(Vector2f())
        }
    }



    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
    }

    override fun getRenderRadius(): Float {
        return Float.MAX_VALUE//1000000f
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        if (Global.getCombatEngine().isPaused) return

        normalLength = 5f + (5 * fade)
        var strength = 0.5f + (20 * (1 - fade))

        dampening = 0.97f

        if (firstFrame) {
            for (i in 0 until velocities.size) {
                var vel = Vector2f(projectile.velocity.x, projectile.velocity.y)
                velocities[i] = vel
            }
        }
        firstFrame = false

        for (i in currentPoints.indices) {
            //First Point
            if (i == 0) {
                var X_vector2 = previousPoints[i + 1].x - previousPoints[i].x
                var Y_vector2 = previousPoints[i + 1].y - previousPoints[i].y
                var Magnitude2 = Vector2f(X_vector2, Y_vector2).length()
                var Extension2 = Magnitude2 - normalLength

                var xv = (X_vector2 / Magnitude2 * Extension2)
                var yv = (Y_vector2 / Magnitude2 * Extension2)

                velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength), velocities[i].y * dampening + (yv * strength))
                currentPoints[i] = Vector2f(previousPoints[i].x + (velocities[i].x * amount), previousPoints[i].y + (velocities[i].y * amount))
                continue
            }

            //Last Point
            if (i == currentPoints.size - 1) {
                var X_vector1 = previousPoints[i - 1].x - previousPoints[i].x
                var Y_vector1 = previousPoints[i - 1].y - previousPoints[i].y

                var Magnitude1 = Vector2f(X_vector1, Y_vector1).length()
                var Extension1 = Magnitude1 - normalLength

                var xv = (X_vector1 / Magnitude1 * Extension1)
                var yv = (Y_vector1 / Magnitude1 * Extension1)

                velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength), velocities[i].y * dampening + (yv * strength))
                currentPoints[i] = Vector2f(previousPoints[i].x + (velocities[i].x * amount), previousPoints[i].y + (velocities[i].y * amount))
                continue
            }

            var X_vector1 = previousPoints[i - 1].x - previousPoints[i].x
            var Y_vector1 = previousPoints[i - 1].y - previousPoints[i].y

            var Magnitude1 = Vector2f(X_vector1, Y_vector1).length()
            var Extension1 = Magnitude1 - normalLength

            var X_vector2 = previousPoints[i + 1].x - previousPoints[i].x
            var Y_vector2 = previousPoints[i + 1].y - previousPoints[i].y
            var Magnitude2 = Vector2f(X_vector2, Y_vector2).length()
            var Extension2 = Magnitude2 - normalLength

            var xv = (X_vector1 / Magnitude1 * Extension1) + (X_vector2 / Magnitude2 * Extension2)
            var yv = (Y_vector1 / Magnitude1 * Extension1) + (Y_vector2 / Magnitude2 * Extension2)

            velocities[i] = Vector2f(velocities[i].x * dampening + (xv * strength), velocities[i].y * dampening + (yv * strength))
            currentPoints[i] = Vector2f(previousPoints[i].x + (velocities[i].x * amount), previousPoints[i].y + (velocities[i].y * amount))
        }

        //Anchor to weapon
        currentPoints[0] = Vector2f(weapon.location)

        var combinedLength = 0f
        for (i in 0 until pointCount) {
            if (i == 0) continue
            var distance = MathUtils.getDistance(currentPoints[i], currentPoints[i-1])
            combinedLength += distance
        }

        if (target != null) {
            hadTarget = true

            var loc: Vector2f? = Vector2f(targetPoint)
            loc = Misc.rotateAroundOrigin(loc, target!!.facing)
            Vector2f.add(target!!.location, loc, loc)

            currentPoints[pointCount - 1] = Vector2f(loc)

            if ((combinedLength >= ((weapon.range * 1.05f) + resistance)) && destructible) {
                target = null
            }
        } else if (!projectile.isFading) {
            currentPoints[pointCount - 1] = Vector2f(projectile.location)
        }

        if ((hadTarget || projectile.isFading) && target == null) {
            fade -= amount// * 0.5f
            fade = fade.coerceIn(0f, 1f)
        }

        previousPoints.clear()
        previousPoints.addAll(currentPoints)
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        GL11.glColor4f(
            lineColor.red / 255f,
            lineColor.green / 255f,
            lineColor.blue / 255f,
            lineColor.alpha / 255f * (1f * fade))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        for (point in currentPoints) {
            GL11.glVertex2f(point.x, point.y)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }
}