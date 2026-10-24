package Jaydee8652.JaydeePiracy.scripts.skills;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class jdp_omegakin {
	public static Map<HullSize, Float> BONUS = new HashMap<HullSize, Float>();
	public static float DAMAGE_BONUS_PERCENT = 25f;

	public static class jdp_omegakinListener implements DamageDealtModifier {
		public String modifyDamageDealt(Object param,
										CombatEntityAPI target, DamageAPI damage,
										Vector2f point, boolean shieldHit) {
			WeaponAPI weapon = null;
			if (param instanceof DamagingProjectileAPI proj) {
				weapon = proj.getWeapon();
			} else if (param instanceof BeamAPI beam) {
				weapon = beam.getWeapon();
			}

			if (weapon == null) return null;
			if (!weapon.getSpec().hasTag("omega") && !weapon.getSpec().hasTag("jdp_omega_auxiliary")) return null;

			String id = "jdp_omegakin_damagemod";
			damage.getModifier().modifyPercent(id, DAMAGE_BONUS_PERCENT);

			return id;
		}
	}

	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship != null && !ship.hasListenerOfClass(jdp_omegakinListener.class)) {
				ship.addListener(new jdp_omegakinListener());
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship != null && ship.hasListenerOfClass(jdp_omegakinListener.class)) {
				ship.removeListenerOfClass(jdp_omegakinListener.class);
			}
		}

		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
			init(stats, skill);
			info.addPara("+" + Math.round(DAMAGE_BONUS_PERCENT) + "% damage dealt by omega-type weaponry", hc, 0f);
		}

		public String getEffectDescription(float level) {
			return null;
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}