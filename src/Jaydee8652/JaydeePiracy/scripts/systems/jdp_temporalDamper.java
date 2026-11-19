package Jaydee8652.JaydeePiracy.scripts.systems;

import java.util.ArrayList;
import java.util.Collections;

import Jaydee8652.JaydeePiracy.scripts.skills.jdp_perfectprediction;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.kotlin.MiscExtKt;

public class jdp_temporalDamper extends BaseShipSystemScript {
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = (ShipAPI) stats.getEntity();

		var mult = 0.25f;
		stats.getHullDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getArmorDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);
		stats.getEmpDamageTakenMult().modifyMult(id, 1f - (1f - mult) * effectLevel);

		if (ship != null && !ship.hasListenerOfClass(jdp_TemporalDamperListener.class)) {
			ship.addListener(new jdp_TemporalDamperListener(ship));
		}
	}

	public static class jdp_TemporalDamperListener implements AdvanceableListener, DamageListener, DamageTakenModifier {
		protected ShipAPI ship;
		protected Object STATUSKEY1 = new Object();

		protected float armorDamage;
		protected float hullDamage;

		protected IntervalUtil interval;

		float ARMOR_PER_TICK = 100f;
		float HULL_PER_TICK = 100f;

		public jdp_TemporalDamperListener(ShipAPI ship) {
			this.ship = ship;
			interval = new IntervalUtil(0.8f, 1f);
			interval.forceIntervalElapsed();
		}

		public void advance(float amount) {
			if (Global.getCombatEngine().isPaused()) return;
			if (!ship.isAlive()) ship.removeListener(this);

			var spec = ship.getSystem().getSpecAPI();
			if (ship == Global.getCombatEngine().getPlayerShip() && (armorDamage > 0 || hullDamage > 0)) Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY1, spec.getIconSpriteName(), spec.getName(), Math.round(armorDamage) + " armor and " + Math.round(hullDamage) + " hull damage absorbed.", !ship.getSystem().isActive());


			if (!ship.getSystem().isActive()) {
				interval.advance(amount);
			}
			if (interval.intervalElapsed()) {
				CombatEngineAPI engine = Global.getCombatEngine();

				float queuedArmorDamage = (float) Math.min(armorDamage, (ARMOR_PER_TICK * 0.9) + Math.round(Math.random() * ARMOR_PER_TICK * 0.2));
				float queuedHullDamage = (float) Math.min(hullDamage, (HULL_PER_TICK * 0.9) + Math.round(Math.random() * HULL_PER_TICK * 0.2));

				if (queuedArmorDamage > 0) {
					ArmorGridAPI grid = ship.getArmorGrid();
					ArrayList<Vector2f> toDamage = new ArrayList<>();
					for (var x = 0; x < grid.getGrid().length; x++) {
						for (var y = 0; y < grid.getGrid()[0].length; y++) {
							if (grid.getArmorValue(x, y) > 0f) toDamage.add(new Vector2f(x, y));
						}
					}

					Collections.shuffle(toDamage);
					for (Vector2f cell : toDamage) {
						var current = grid.getArmorValue((int) cell.x, (int) cell.y);
						var cellChange = Math.min(queuedArmorDamage / toDamage.size(), current);

						grid.setArmorValue((int) cell.x, (int) cell.y, Math.max(current - cellChange, 0f));
						armorDamage -= cellChange;
					}

					if (!toDamage.isEmpty() && Misc.shouldShowDamageFloaty(ship, ship)) engine.addFloatingDamageText(MathUtils.getPointOnCircumference(ship.getShieldCenterEvenIfNoShield(), 30f, ship.getFacing()), queuedArmorDamage, ship.getCollisionRadius() * 0.5f, Misc.FLOATY_ARMOR_DAMAGE_COLOR, ship, ship);
					ship.syncWithArmorGridState();
				}

				if (queuedHullDamage > 0) {
					float change = Math.min(queuedHullDamage, ship.getHitpoints());

					if (change >= 0) {
						ship.setHitpoints(ship.getHitpoints() - change);
						if (ship.getHitpoints() <= 0f && !ship.isHulk()) engine.applyDamage(ship, ship.getShieldCenterEvenIfNoShield(), 100f, DamageType.ENERGY, 0f, true, false, ship, false);
						if (Misc.shouldShowDamageFloaty(ship, ship)) engine.addFloatingDamageText(ship.getShieldCenterEvenIfNoShield(), change, ship.getCollisionRadius() * 0.5f, Misc.FLOATY_HULL_DAMAGE_COLOR, ship, ship);
						hullDamage -= change;
					}
				}
			}
		}

		protected float armorPrevious;
		protected float hullPrevious;

		public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
			if (ship.getSystem().isActive()) {
				var hull = result.getDamageToHull() * 4;
				var armor = result.getTotalDamageToArmor() * 4;

				if (hull > hullPrevious) hullDamage += armor - hullPrevious;
				if (armor > armorPrevious) armorDamage += armor - armorPrevious;
			}
		}

		@Override
		public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
			return "";
		}
	}
}
