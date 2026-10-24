package Jaydee8652.JaydeePiracy.scripts.systems;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;

public class jdp_ModularJitterStats extends BaseShipSystemScript {

	//public static final Color JITTER_COLOR = new Color(255,155,255,75);
	//public static final Color JITTER_UNDER_COLOR = new Color(255,155,255,155);

	public static final Color JITTER_COLOR = new Color(100,100,255,150);
	public static final Color JITTER_UNDER_COLOR = new Color(100,100,255,150);


	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;

		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}


		float jitterLevel = effectLevel;
		if (state == State.ACTIVE) {
			if (player) {
				Global.getSoundPlayer().playSound("mote_attractor_targeted_empty_space", 1f, 0.15f, ship.getLocation(), ship.getVelocity());
				Global.getSoundPlayer().playSound("mote_attractor_system_activated", 2f, 0.3f, ship.getLocation(), ship.getVelocity());
			} else {
				Global.getSoundPlayer().playSound("mote_attractor_targeted_empty_space", 5f, 0.8f, ship.getLocation(), ship.getVelocity());
				Global.getSoundPlayer().playSound("mote_attractor_system_activated", 6f, 1f, ship.getLocation(), ship.getVelocity());
			}
		} else if (state == State.OUT) {
			jitterLevel *= jitterLevel;
		}



		float maxRangeBonus = 25f;
		float jitterRangeBonus = jitterLevel * maxRangeBonus;

		//Modules
		for (ShipAPI module : ship.getChildModulesCopy()) {
			module.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 5, 0f, 3f + jitterRangeBonus);
			module.setJitter(this, JITTER_COLOR, jitterLevel, 4, 0f, 0 + jitterRangeBonus);
		}
	}
}








