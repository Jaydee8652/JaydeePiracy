package Jaydee8652.JaydeePiracy.scripts.systems;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class jdp_BanisherStats extends BaseShipSystemScript {

	public static float SPEED_BONUS = 125f;
	public static float TURN_BONUS = 20f;
	public static float WEAPON_TURN_BONUS = 40f;


	private Color color = new Color(100,255,100,255);
	
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_BONUS);
			stats.getAcceleration().modifyPercent(id, SPEED_BONUS * 2f * effectLevel);
			stats.getDeceleration().modifyPercent(id, SPEED_BONUS * 2f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, TURN_BONUS * effectLevel);
			stats.getTurnAcceleration().modifyPercent(id, TURN_BONUS * 2f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 10f);
			stats.getMaxTurnRate().modifyPercent(id, 100f);

			stats.getWeaponTurnRateBonus().modifyPercent(id, WEAPON_TURN_BONUS);
		}
		
		if (stats.getEntity() instanceof ShipAPI) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			
			ship.getEngineController().fadeToOtherColor(this, color, new Color(0,0,0,0), effectLevel, 0.67f);
			ship.getEngineController().extendFlame(this, 2f * effectLevel, 0f * effectLevel, 0f * effectLevel);
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 2) {
			return new StatusData("+" + (int)SPEED_BONUS + " top speed", false);
		}
		if (index == 1) {
			return new StatusData("+" + (int)WEAPON_TURN_BONUS + " weapon turn rate", false);
		}
		return null;
	}
}
