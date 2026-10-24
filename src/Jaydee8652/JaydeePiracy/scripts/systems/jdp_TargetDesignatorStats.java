package Jaydee8652.JaydeePiracy.scripts.systems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class jdp_TargetDesignatorStats extends BaseShipSystemScript {

	public static String LIDAR_WINDUP = "lidar_windup";
	public static Color WEAPON_GLOW = new Color(255,50,50,155);

	public static Object KEY_SHIP = new Object();
	public static Object KEY_TARGET = new Object();

	public static float DAM_MULT = 1.5f;
	protected static float RANGE = 1500f;

	public static Color TEXT_COLOR = new Color(255,55,55,255);

	public static Color JITTER_COLOR = new Color(255,50,50,75);
	public static Color JITTER_UNDER_COLOR = new Color(255,100,100,155);


	public static class TargetData {
		public ShipAPI ship;
		public ShipAPI target;
		public EveryFrameCombatPlugin targetEffectPlugin;
		public float currDamMult;
		public float elaspedAfterInState;
		public TargetData(ShipAPI ship, ShipAPI target) {
			this.ship = ship;
			this.target = target;
		}
	}


	public static class LidarDishData {
		public float turnDir;
		public float turnRate;
		public float angle;
		public float phase;
		public float count;
		public WeaponAPI w;
	}

	protected List<LidarDishData> dishData = new ArrayList<jdp_TargetDesignatorStats.LidarDishData>();
	protected boolean needsUnapply = false;
	protected boolean playedWindup = false;
	
	protected boolean inited = false;
	public void init(ShipAPI ship) {
		if (inited) return;
		inited = true;
		
		needsUnapply = true;
		
		int turnDir = 1;
		float index = 0f;
		float count = 0f;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR)) {
				count++;
			}
		}
		List<WeaponAPI> lidar = new ArrayList<WeaponAPI>();
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR)) {
				lidar.add(w);
			}
		}
		Collections.sort(lidar, new Comparator<WeaponAPI>() {
			public int compare(WeaponAPI o1, WeaponAPI o2) {
				return (int) Math.signum(o1.getSlot().getLocation().x - o2.getSlot().getLocation().x);
			}
		});
		for (WeaponAPI w : lidar) {
			if (w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR)) {
				w.setSuspendAutomaticTurning(true);
				LidarDishData data = new LidarDishData();
				data.turnDir = Math.signum(turnDir);
				data.turnRate = 1f;
				data.w = w;
				data.angle = 0f;
				data.phase = index / count;
				data.count = count;
				dishData.add(data);
				turnDir = -turnDir;
				index++;
			}
		}
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = (ShipAPI) stats.getEntity();

		for (jdp_TargetDesignatorStats.LidarDishData data : dishData) {
			float targetAngle = ship.getFacing();
			if (findTarget(ship) != null) targetAngle = (Misc.getAngleInDegrees(ship.getLocation(), findTarget(ship).getLocation()));

			float turn = Misc.getClosestTurnDirection(data.w.getCurrAngle(), targetAngle);
			float difference = Misc.getAngleDiff(data.w.getCurrAngle(), targetAngle);

			if (difference > 1f) data.w.setFacing(data.w.getCurrAngle() + turn);
			data.w.updateBeamFromPoints();
		}

		init(ship);

		boolean active = state == State.IN || state == State.ACTIVE || state == State.OUT;
		if (!active) return;

		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getSlot().isSystemSlot()) continue;
			if (!(w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR))) w.setForceNoFireOneFrame(true);
		}

		// always wait a quarter of a second before starting to fire the targeting lasers
		// this is the worst-case turn time required for the dishes to face front
		// doing this to keep the timing of the lidar ping sounds consistent relative
		// to when the windup sound plays
		float fireThreshold = 0.25f / 3.25f;
		fireThreshold += 0.02f; // making sure there's only 4 lidar pings; lines up with the timing of the lidardish weapon
		for (LidarDishData data : dishData) {
			if (data.w.isDecorative() && data.w.getSpec().hasTag(Tags.LIDAR)) {
				if (state == State.IN && effectLevel >= fireThreshold) {
					data.w.setForceFireOneFrame(true);
				}
			}
		}
		
		if (((state == State.IN && effectLevel > 0.67f) || state == State.ACTIVE) && !playedWindup) {
			Global.getSoundPlayer().playSound(LIDAR_WINDUP, 1f, 1f, ship.getLocation(), ship.getVelocity());
			playedWindup = true;
		}

		final String targetDataKey = ship.getId() + "_designator_target_data";
		Object targetDataObj = Global.getCombatEngine().getCustomData().get(targetDataKey);

		if (state == State.IN && targetDataObj == null) {
			ShipAPI target = findTarget(ship);
			Global.getCombatEngine().getCustomData().put(targetDataKey, new jdp_TargetDesignatorStats.TargetData(ship, target));
		} else if (state == State.IDLE && targetDataObj != null) {
			Global.getCombatEngine().getCustomData().remove(targetDataKey);
			((jdp_TargetDesignatorStats.TargetData)targetDataObj).currDamMult = 1f;
			targetDataObj = null;
		}
		if (targetDataObj == null || ((jdp_TargetDesignatorStats.TargetData) targetDataObj).target == null) return;

		final jdp_TargetDesignatorStats.TargetData targetData = (jdp_TargetDesignatorStats.TargetData) targetDataObj;

		if (targetData.targetEffectPlugin == null) {
			targetData.targetEffectPlugin = new BaseEveryFrameCombatPlugin() {
				float angle = 0;
				final float baserad = 32f;

				@Override
				public void advance(float amount, List<InputEventAPI> events) {
					if (Global.getCombatEngine().isPaused()) return;
					targetData.currDamMult = 1f + (DAM_MULT - 1f) * amount;

					if (targetData.target == Global.getCombatEngine().getPlayerShip()) {
						Global.getCombatEngine().maintainStatusForPlayerShip(KEY_TARGET, targetData.ship.getSystem().getSpecAPI().getIconSpriteName(), targetData.ship.getSystem().getDisplayName(), "" + (int)((targetData.currDamMult - 1f) * 100f) + "% more damage taken", true);
					}

					if (targetData.currDamMult <= 1f || !targetData.ship.isAlive()) {
						targetData.target.getMutableStats().getHullDamageTakenMult().unmodify(id);
						targetData.target.getMutableStats().getArmorDamageTakenMult().unmodify(id);
						targetData.target.getMutableStats().getShieldDamageTakenMult().unmodify(id);
						targetData.target.getMutableStats().getEmpDamageTakenMult().unmodify(id);
						Global.getCombatEngine().removePlugin(targetData.targetEffectPlugin);
					} else {
						targetData.target.getMutableStats().getHullDamageTakenMult().modifyMult(id, targetData.currDamMult);
						targetData.target.getMutableStats().getArmorDamageTakenMult().modifyMult(id, targetData.currDamMult);
						targetData.target.getMutableStats().getShieldDamageTakenMult().modifyMult(id, targetData.currDamMult);
						targetData.target.getMutableStats().getEmpDamageTakenMult().modifyMult(id, targetData.currDamMult);
					}

					angle += amount * 20f;

					float radius = Math.min(Math.max(targetData.target.getCollisionRadius(), baserad), baserad * 8f);
					MagicRender.singleframe(Global.getSettings().getSprite("ui", "jdp_icon_reticle"), targetData.target.getLocation(), new Vector2f(radius,radius), angle, Color.WHITE, false, CombatEngineLayers.ABOVE_PARTICLES);
				}
			};
			Global.getCombatEngine().addPlugin(targetData.targetEffectPlugin);
		}


		if (effectLevel > 0) {
			if (state != State.IN) {
				targetData.elaspedAfterInState += Global.getCombatEngine().getElapsedInLastFrame();
			}
			float shipJitterLevel = 0;
			if (state == State.IN) {
				shipJitterLevel = effectLevel;
			} else {
				float durOut = 0.5f;
				shipJitterLevel = Math.max(0, durOut - targetData.elaspedAfterInState) / durOut;
			}

			float maxRangeBonus = 50f;
			float jitterRangeBonus = shipJitterLevel * maxRangeBonus;

			Color color = JITTER_COLOR;
			if (shipJitterLevel > 0) {
				ship.setJitter(KEY_SHIP, color, shipJitterLevel, 4, 0f, 0 + jitterRangeBonus * 1f);
			}
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		// never called due to runScriptWhileIdle:true in the .system file
	}

	protected ShipAPI findTarget(ShipAPI ship) {
		float range = getMaxRange(ship);
		boolean player = ship == Global.getCombatEngine().getPlayerShip();
		ShipAPI target = ship.getShipTarget();

		if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.TARGET_FOR_SHIP_SYSTEM)){
			target = (ShipAPI) ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.TARGET_FOR_SHIP_SYSTEM);
			if (target != null && target.getOriginalOwner() == ship.getOriginalOwner()) target = null;
		}

		if (target != null) {
			float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
			float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
			if (dist > range + radSum) target = null;
		} else {
			if (target == null || target.getOwner() == ship.getOwner()) {
				if (player) {
					target = Misc.findClosestShipEnemyOf(ship, ship.getMouseTarget(), ShipAPI.HullSize.FRIGATE, range, true);
				} else {
					Object test = ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET);
					if (test instanceof ShipAPI) {
						target = (ShipAPI) test;
						float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
						float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
						if (dist > range + radSum || target.isFighter()) target = null;
						if (target != null && target.getOriginalOwner() == ship.getOriginalOwner()) target = null;
					}
				}
			}
		}

		if (target != null && target.isFighter()) target = null;
		if (target == null) {
			target = Misc.findClosestShipEnemyOf(ship, ship.getLocation(), ShipAPI.HullSize.FRIGATE, range, true);
		}

		return target;
	}


	public static float getMaxRange(ShipAPI ship) {
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
	}


	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (effectLevel > 0) {
			if (index == 0) {
				float damMult = 1f + (DAM_MULT - 1f) * effectLevel;
				return new StatusData("" + (int)((damMult - 1f) * 100f) + "% more damage to target", false);
			}
		}
		return null;
	}


	@Override
	public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
		if (system.isOutOfAmmo()) return null;
		if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

		ShipAPI target = findTarget(ship);
		if (target != null && target != ship) {
			return "READY";
		}
		if ((target == null) && ship.getShipTarget() != null) {
			return "OUT OF RANGE";
		}
		return "NO TARGET";
	}


	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		//if (true) return true;
		ShipAPI target = findTarget(ship);
		return target != null && target != ship;
	}


}
