package Jaydee8652.JaydeePiracy.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.skills.CombatEndurance;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Map;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.playerShip;

public class jdp_scwarzsechHide extends BaseHullMod {
	static float RATE = 20f;
	static float THRESHOLD = 5f;
	public static Object STATUS_KEY = new Object();
	public static String KEY = "jdp_scwarzsechHide";

	public void advanceInCombat(ShipAPI ship, float amount) {
		if (!ship.isAlive()) return;

		Map<String, Object> customCombatData = Global.getCombatEngine().getCustomData();
		String id = ship.getId();
		float total = 0f;

		if (customCombatData.get(KEY+id) instanceof Float) total = (float) customCombatData.get(KEY+id);
		String icon = Global.getSettings().getSpriteName("ui", "jdp_icon_cuttlefish");

		float slowdown = 1f;
		if (total > (ship.getArmorGrid().getArmorRating() * THRESHOLD)) {
			slowdown = (ship.getArmorGrid().getArmorRating() * THRESHOLD) / total;
			if (playerShip(ship)) Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY, icon, "SUBSISTENT HIDE - DEGRADING", Math.round(total) + " / " + Math.round(ship.getArmorGrid().getArmorRating() * THRESHOLD) + " total armor repaired", true);
		} else {
			if (playerShip(ship)) Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY, icon, "SUBSISTENT HIDE - HEALTHY", Math.round(total) + " / " + Math.round(ship.getArmorGrid().getArmorRating() * THRESHOLD) + " total armor repaired", false);
		}
		var change = RATE * amount * slowdown;

		var grid = ship.getArmorGrid();
		ArrayList<Vector2f> toRepair = new ArrayList<>();

		for (var x = 0; x < grid.getGrid().length; x++) {
			for (var y = 0; y < grid.getGrid()[0].length; y++) {
				if (grid.getArmorValue(x, y) < grid.getMaxArmorInCell()) toRepair.add(new Vector2f(x, y));
			}
		}

		for (Vector2f cell : toRepair) {
			var cellChange = change / toRepair.size();
			if ((ship.getFluxTracker().getHardFlux() - cellChange) > 0) {
				grid.setArmorValue((int) cell.x, (int) cell.y, (grid.getArmorValue((int) cell.x, (int) cell.y) + cellChange));
				ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getHardFlux() - cellChange);
				total += cellChange;
			}
		}

		customCombatData.put(KEY+id, total);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "hard flux";
		if (index == 1) return "repair armor";
		if (index == 2) return Math.round(THRESHOLD) + "";
        return null;
    }
}
