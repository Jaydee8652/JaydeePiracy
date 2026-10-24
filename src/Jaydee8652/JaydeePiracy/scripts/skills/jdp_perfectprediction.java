package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.scripts.skills.flowerfish.jdp_flowerfishQuadrupolarCoupling;
import Jaydee8652.JaydeeProficiencies.patch.WeaponFireListenerPatch;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import static Jaydee8652.JaydeeProficiencies.utils.combat.jdp_CombatUtils.getProjectilesFromSource;

public class jdp_perfectprediction {
	public static String KEY = "$jdp_predicted";

	public static float INACCURACY = 10f;
	public static float MAX_DELAY = 120f;
	public static float MIN_DELAY = 15f;

	public static float CHANCE = 0.5f;
	public static float LENIENCE = 0.10f;
	public static float DAMAGE_MALUS = 0.75f;

	public static class jdp_perfectpredictionAdvance implements AdvanceableListener, WeaponFireListenerPatch.WeaponFireListener {
		protected ShipAPI ship;
		protected String id;
		public jdp_perfectpredictionAdvance(ShipAPI ship, String id) {
			this.ship = ship;
			this.id = id;
		}

		@Override
		public void reportProjectileSpawn(Object projectile, WeaponAPI weaponAPI, CombatEngineAPI combatEngineAPI) {
			if (!(projectile instanceof MissileAPI) && (projectile instanceof DamagingProjectileAPI damagingProjectileAPI)) {
				projectiles.add(damagingProjectileAPI);
			}
		}

		@Override
		public void reportOnFire(int burst, WeaponAPI weaponAPI, CombatEngineAPI combatEngineAPI) {

		}

		public static class ProjectileBuffer {
			private float duration;
			private final DamagingProjectileAPI proj;

			public ProjectileBuffer(float duration, DamagingProjectileAPI proj) {
				this.duration = duration;
				this.proj = proj;
			}
		}

		ArrayList<DamagingProjectileAPI> projectiles = new ArrayList<>();
		ArrayList<ProjectileBuffer> buffers = new ArrayList<>();

		public static float getDelay() {
			return (float) (MIN_DELAY + Math.random() * (MAX_DELAY - MIN_DELAY));
		}

		public void advance(float amount) {
			if (!ship.isAlive()) return;

			for (DamagingProjectileAPI proj : new ArrayList<>(projectiles)) {
				if (proj.isExpired()) {
					if (CombatUtils.getShipsWithinRange(proj.getLocation(), proj.getWeapon().getRange() * LENIENCE).stream().anyMatch(s -> s.getOwner() != ship.getOwner())) buffers.add(new ProjectileBuffer(getDelay(), proj));
					projectiles.remove(proj);
				}
				if (proj.didDamage() || proj.wasRemoved()) {
					projectiles.remove(proj);
				}
			}

			for (ProjectileBuffer buffer : new ArrayList<>(buffers)) {
				buffer.duration -= 1 * amount;

				var target = ship.getShipTarget();
				if (buffer.duration < 0f) {
					if (target == null) {
						buffer.duration = getDelay();
						continue;
					}

					if (Math.random() <= CHANCE) {
						float angleOffset = MathUtils.getRandomNumberInRange(-INACCURACY / 2, INACCURACY / 2) + MathUtils.getRandomNumberInRange(-INACCURACY / 2, INACCURACY / 2);
						WeaponAPI weapon = buffer.proj.getWeapon();
						float distance = Math.round((Math.random() * weapon.getRange() * 0.6) + (weapon.getRange() * 0.3));

						Vector2f location = null;
						while (location == null) {
							Vector2f potentialPoint = MathUtils.getRandomPointInCircle(target.getLocation(), distance);
							if (CombatUtils.getEntitiesWithinRange(potentialPoint, 25f).stream().noneMatch(e -> CollisionUtils.isPointWithinBounds(potentialPoint, e))) location = potentialPoint;
						}

						DamagingProjectileAPI newProj = (DamagingProjectileAPI) Global.getCombatEngine().spawnProjectile(ship, weapon, weapon.getId(), location,  Misc.getAngleInDegrees(location, target.getLocation()) + angleOffset, new Vector2f(0, 0));
						newProj.setCustomData(KEY, true);
						newProj.getDamage().setSoftFlux(true);
						newProj.getDamage().getModifier().modifyMult(id, DAMAGE_MALUS);
					}
					buffers.remove(buffer);
				}
			}
		}
	}

	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship != null && !ship.hasListenerOfClass(jdp_perfectpredictionAdvance.class)) {
				ship.addListener(new jdp_perfectpredictionAdvance(ship, id));
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship != null && ship.hasListenerOfClass(jdp_perfectpredictionAdvance.class)) {
				ship.removeListenerOfClass(jdp_perfectpredictionAdvance.class);
			}
		}


		@Override
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
			info.addPara("Projectiles that fade within range of an enemy have a %s chance to return (does not include missiles)", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), Math.round(CHANCE * 100)+"%");
			info.addPara(BaseIntelPlugin.BULLET+"Returning projectiles strike the current target from a random unimpeded angle", 0f, Misc.getTextColor(), Misc.getHighlightColor());
			info.addPara(BaseIntelPlugin.BULLET+"Returning projectiles deal %s of their base damage as soft flux", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round(100 * DAMAGE_MALUS)+"%");
			info.addPara(BaseIntelPlugin.BULLET+"Projectiles can take between %s and %s seconds to return", 0f, Misc.getTextColor(), Misc.getHighlightColor(), ""+Math.round(MIN_DELAY), ""+Math.round(MAX_DELAY));

		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}