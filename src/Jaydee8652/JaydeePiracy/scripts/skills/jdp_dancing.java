package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeeProficiencies.patch.WeaponFireListenerPatch;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import static Jaydee8652.JaydeeProficiencies.utils.combat.jdp_CombatUtils.getMissilesFromSource;

public class jdp_dancing {
	public static Object STATUS_KEY = new Object();

	public static float DAMAGE_MALUS = 15f;


	public static float DURATION = 10f;
	public static float CAP = 30f;
	public static float BONUS = 0.5f;
/*
	public static class jdp_dancingAdvance implements AdvanceableListener {
		protected ShipAPI ship;
		protected String id;
		public jdp_dancingAdvance(ShipAPI ship, String id) {
			this.ship = ship;
			this.id = id;
		}

		public static class Stack {
			private float duration;

			public Stack(float duration) {
				this.duration = duration;
			}
		}

		ArrayList<DamagingProjectileAPI> tracked = new ArrayList<>();
		ArrayList<Stack> stacks = new ArrayList<>();

		public void advance(float amount) {
			if (!ship.isAlive()) return;

			var missiles = getMissilesFromSource(ship);
			tracked.addAll(missiles.stream().filter(p -> ship.getAllWeapons().contains(p.getWeapon()) && !p.isMine() && !p.getCustomData().containsKey("$jdp_dancingTriggered")).toList());

			for (DamagingProjectileAPI missile : new ArrayList<>(tracked)) {
				missile.setCustomData("$jdp_dancingTriggered", true);
				stacks.add(new Stack(DURATION));
				tracked.remove(missile);
			}

			for (Stack stack : new ArrayList<>(stacks)) {
				stack.duration -= 1 * amount;
				if (stack.duration < 0f) {
					stacks.remove(stack);
				}
			}

			var time = Math.min((stacks.size() * BONUS), CAP) * 0.01f;
			var player = (ship == Global.getCombatEngine().getPlayerShip());
			if (player) {
				Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / (1f + time));
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY, Global.getSettings().getSpriteName("ui", "jdp_icon_time"), "DANCING", Math.round(time * 100) + "% timeflow", false);
			}
			ship.getMutableStats().getTimeMult().modifyMult(id, 1 + time);
		}
	}
	*/

	public static class jdp_dancingAdvance implements AdvanceableListener, WeaponFireListenerPatch.WeaponFireListener {
		protected ShipAPI ship;
		protected String id;
		public jdp_dancingAdvance(ShipAPI ship, String id) {
			this.ship = ship;
			this.id = id;
		}

		@Override
		public void reportProjectileSpawn(Object projectile, WeaponAPI weaponAPI, CombatEngineAPI combatEngineAPI) {
			if (projectile instanceof MissileAPI) {
				stacks.add(new Stack(DURATION));
			}
		}

		@Override
		public void reportOnFire(int burst, WeaponAPI weaponAPI, CombatEngineAPI combatEngineAPI) {

		}

		public static class Stack {
			private float duration;

			public Stack(float duration) {
				this.duration = duration;
			}
		}

		ArrayList<Stack> stacks = new ArrayList<>();

		public void advance(float amount) {
			if (!ship.isAlive()) return;

			for (Stack stack : new ArrayList<>(stacks)) {
				stack.duration -= 1 * amount;
				if (stack.duration < 0f) {
					stacks.remove(stack);
				}
			}

			var time = Math.min((stacks.size() * BONUS), CAP) * 0.01f;
			if (ship == Global.getCombatEngine().getPlayerShip()) {
				Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / (1f + time));
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY, Global.getSettings().getSpriteName("ui", "jdp_icon_time"), "DANCING", Math.round(time * 100) + "% timeflow", false);
			}
			ship.getMutableStats().getTimeMult().modifyMult(id, 1 + time);
		}
	}

	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			ShipAPI ship = (ShipAPI) stats.getEntity();

			if (ship != null && !ship.hasListenerOfClass(jdp_dancingAdvance.class)) {
				ship.addListener(new jdp_dancingAdvance(ship, id));
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			ShipAPI ship = (ShipAPI) stats.getEntity();

			if (ship != null && ship.hasListenerOfClass(jdp_dancingAdvance.class)) {
				ship.removeListenerOfClass(jdp_dancingAdvance.class);
			}
		}

		@Override
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
			info.addPara("Firing a missile increases timeflow by %s for %s seconds, up to %s", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), "+"+BONUS+"%", ""+Math.round(DURATION), "+"+Math.round(CAP)+"%");
			info.addPara("*Individual missiles are counted, not missile weapons", 0f, Misc.getGrayColor(),Misc.getGrayColor());
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}