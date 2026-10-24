package Jaydee8652.JaydeePiracy.scripts.systems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

public class jdp_AncientLidarStats extends BaseShipSystemScript {

	public static String LIDAR_WINDUP = "lidar_windup";
	
	public static Color WEAPON_GLOW = new Color(255,50,50,155);
	
	public static float RANGE_BONUS = 50f;
	public static float PASSIVE_RANGE_BONUS = 10f;
	public static float ROF_BONUS = 2f;
	public static float RECOIL_BONUS = 75f;
	public static float PROJECTILE_SPEED_BONUS = 50f;
	
	
	public static class LidarDishData {
		public float turnDir;
		public float turnRate;
		public float angle;
		public float phase;
		public float count;
		public WeaponAPI w;
	}

	protected List<LidarDishData> dishData = new ArrayList<jdp_AncientLidarStats.LidarDishData>();
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
				data.turnRate = 0.5f;
				data.turnRate = 0.1f;
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
		ShipAPI ship = (ShipAPI)stats.getEntity();

		for (jdp_AncientLidarStats.LidarDishData data : dishData) {
			for (WeaponAPI weapon : ship.getAllWeapons()) {
				float dist = Misc.getDistance(weapon.getSlot().getLocation(), data.w.getSlot().getLocation());

				if ((dist == 0) && !weapon.isDecorative()) {
					data.w.setFacing(weapon.getCurrAngle());
					data.w.updateBeamFromPoints();
				}
			}
		}

		if (ship == null || ship.isHulk()) {
			if (needsUnapply) {
				unmodify(id, stats);
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (!w.isDecorative() && (w.getSize() != WeaponAPI.WeaponSize.SMALL) && !w.isBeam() &&
							(w.getType() == WeaponType.BALLISTIC || w.getType() == WeaponType.ENERGY)) {
						w.setGlowAmount(0, null);
					}
				}
				needsUnapply = false;
			}
			return;
		}
		
		init(ship);

		boolean active = state == State.IN || state == State.ACTIVE || state == State.OUT;

		if (active) {
			modify(id, stats, effectLevel);
			needsUnapply = true;
		} else {
			if (needsUnapply) {
				unmodify(id, stats);
				for (WeaponAPI w : ship.getAllWeapons()) {
					if (w.getSlot().isSystemSlot()) continue;
					if (!w.isDecorative() && (w.getSize() != WeaponAPI.WeaponSize.SMALL) && !w.isBeam() &&
							(w.getType() == WeaponType.BALLISTIC || w.getType() == WeaponType.ENERGY)) {
						w.setGlowAmount(0, null);
					}
				}
				needsUnapply = false;
			}
		}
		
		if (!active) return;
		

		for (WeaponAPI w : ship.getAllWeapons()) {
			if (w.getSlot().isSystemSlot()) continue;
			if (w.getType() == WeaponType.MISSILE) continue;
			if (state == State.IN) {
				if (!(w.isDecorative() && w.getSpec().hasTag(Tags.LIDAR))) {
					w.setForceNoFireOneFrame(true);
				}
			} else {
				if (!(!w.isDecorative() && (w.getSize() != WeaponAPI.WeaponSize.SMALL) && !w.isBeam() &&
						(w.getType() == WeaponType.BALLISTIC || w.getType() == WeaponType.ENERGY))) {
					w.setForceNoFireOneFrame(true);
				}
			}
		}
		
		Color glowColor = WEAPON_GLOW;
		
		float lidarRange = 500;
		for (WeaponAPI w : ship.getAllWeapons()) {
			if (!w.isDecorative() && (w.getSize() != WeaponAPI.WeaponSize.SMALL) && !w.isBeam() &&
					(w.getType() == WeaponType.BALLISTIC || w.getType() == WeaponType.ENERGY)) {
				lidarRange = Math.max(lidarRange, w.getRange());
				w.setGlowAmount(effectLevel, glowColor);
			}
		}
		lidarRange += 100f;
		stats.getBeamWeaponRangeBonus().modifyFlat("lidararray", lidarRange);
		
		// always wait a quarter of a second before starting to fire the targeting lasers
		// this is the worst-case turn time required for the dishes to face front
		// doing this to keep the timing of the lidar ping sounds consistent relative
		// to when the windup sound plays
		float fireThreshold = 0.25f / 3.25f;
		fireThreshold += 0.02f; // making sure there's only 4 lidar pings; lines up with the timing of the lidardish weapon
		for (LidarDishData data : dishData) {
			boolean skip = data.phase % 1f > 1f / data.count;
			skip = false;
			if (skip) continue;
			if (data.w.isDecorative() && data.w.getSpec().hasTag(Tags.LIDAR)) {
				if (state == State.IN && Math.abs(data.angle) < 5f && effectLevel >= fireThreshold) {
					data.w.setForceFireOneFrame(true);
				}
			}
		}
		
		if (((state == State.IN && effectLevel > 0.67f) || state == State.ACTIVE) && !playedWindup) {
			Global.getSoundPlayer().playSound(LIDAR_WINDUP, 1f, 1f, ship.getLocation(), ship.getVelocity());
			playedWindup = true;
		}
	}
	
	
	protected void modify(String id, MutableShipStatsAPI stats, float effectLevel) {
		float mult = 1f + ROF_BONUS * effectLevel;
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS);
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getEnergyRoFMult().modifyMult(id, mult);
		stats.getMaxRecoilMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		stats.getRecoilPerShotMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		stats.getRecoilDecayMult().modifyMult(id, 1f - (0.01f * RECOIL_BONUS));
		
		stats.getBallisticProjectileSpeedMult().modifyPercent(id, PROJECTILE_SPEED_BONUS);
		stats.getEnergyProjectileSpeedMult().modifyPercent(id, PROJECTILE_SPEED_BONUS);
	}
	protected void unmodify(String id, MutableShipStatsAPI stats) {
		stats.getBallisticWeaponRangeBonus().modifyPercent(id, PASSIVE_RANGE_BONUS);
		stats.getEnergyWeaponRangeBonus().modifyPercent(id, PASSIVE_RANGE_BONUS);

		stats.getBallisticRoFMult().unmodifyMult(id);
		stats.getEnergyRoFMult().unmodifyMult(id);
		stats.getMaxRecoilMult().unmodifyMult(id);
		stats.getRecoilPerShotMult().unmodifyMult(id);
		stats.getRecoilDecayMult().unmodifyMult(id);
		
		stats.getBallisticProjectileSpeedMult().unmodifyPercent(id);
		stats.getEnergyProjectileSpeedMult().unmodifyPercent(id);
		
		playedWindup = false;
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
		// never called due to runScriptWhileIdle:true in the .system file
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (state == State.IDLE || state == State.COOLDOWN) {
			if (index == 3) {
				return new StatusData("weapon range +" + (int) PASSIVE_RANGE_BONUS + "%", false);
			}	
		}
		if (effectLevel <= 0f) return null;
		
		float mult = 1f + ROF_BONUS;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 3) {
			return new StatusData("weapon range +" + (int) RANGE_BONUS + "%", false);
		}
		if (index == 2) {
			return new StatusData("rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 1) {
			return new StatusData("weapon recoil -" + (int) RECOIL_BONUS + "%", false);
		}
		if (index == 0 && PROJECTILE_SPEED_BONUS > 0) {
			return new StatusData("projectile speed +" + (int) PROJECTILE_SPEED_BONUS + "%", false);
		}
		return null;
	}
	
}
