package Jaydee8652.JaydeePiracy.scripts.skills;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.util.C;
import lunalib.lunaUI.elements.LunaElement;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import static Jaydee8652.JaydeePiracy.scripts.skills.jdp_omegakin.DAMAGE_BONUS_PERCENT;
import static Jaydee8652.JaydeePiracy.scripts.skills.jdp_omegakin.jdp_omegakinListener;
import static Jaydee8652.JaydeeProficiencies.utils.combat.jdp_CombatUtils.getProjectilesFromSource;

public class jdp_appliedcontravention {
	public static Map<HullSize, Float> BONUS = new HashMap<HullSize, Float>();
	public static float PERCENT = 0.1f;

	public static float ARMOR = 1.0f;
	public static float SHIELD = 1.0f;
	public static float HULL = 2.0f;
	public static String NAME = "Shatter";
	public static String DESC = Math.round(ARMOR * 100)+"% vs shields and armor, "+Math.round(HULL * 100)+"% vs hull";

	public static class jdp_true implements DamageDealtModifier {
		protected ShipAPI ship;
		protected String id;
		public jdp_true(ShipAPI ship, String id) {
			this.ship = ship;
			this.id = id;
		}

		public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
			if (damage.getType() == DamageType.OTHER) return id;
			if (target instanceof ShipAPI ship) {
				damage.getModifier().modifyPercent(id, 1 - PERCENT);
				var stats = ship.getMutableStats();

				//Store real stats
				var armor = DamageType.OTHER.getArmorMult();
				var shield = DamageType.OTHER.getShieldMult();
				var hull = DamageType.OTHER.getHullMult();
				var name = DamageType.OTHER.getDisplayName();
				var desc = DamageType.OTHER.getDescription();

				//Apply new stats
				DamageType.OTHER.setArmorMult(ARMOR);
				DamageType.OTHER.setShieldMult(SHIELD);
				DamageType.OTHER.setHullMult(HULL);
				DamageType.OTHER.setDisplayName(NAME);
				DamageType.OTHER.setDescription(DESC);
				stats.getEffectiveArmorBonus().modifyMult(id, 0f);

				//Apply damage
				Global.getCombatEngine().applyDamage(target, point, (damage.getBaseDamage() * PERCENT), DamageType.OTHER, 0f, false, damage.isSoftFlux(), ship, false);
				Global.getCombatEngine().spawnExplosion(point, target.getVelocity(), new Color(235, 235, 235), 10f, 0.05f);
				Global.getSoundPlayer().playSound("rift_lightning_explosion", MathUtils.getRandomNumberInRange(0.96f, 1.04f), 0.5f, ship.getLocation(), ship.getVelocity());

				//Reapply real stats
				DamageType.OTHER.setArmorMult(armor);
				DamageType.OTHER.setShieldMult(hull);
				DamageType.OTHER.setHullMult(shield);
				DamageType.OTHER.setDisplayName(name);
				DamageType.OTHER.setDescription(desc);
				stats.getEffectiveArmorBonus().unmodify(id);
			}

			return id;
		}
	}

	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship != null && !ship.hasListenerOfClass(jdp_true.class)) {
				ship.addListener(new jdp_true(ship, id));
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			if (ship != null && ship.hasListenerOfClass(jdp_true.class)) {
				ship.removeListenerOfClass(jdp_true.class);
			}
		}

		@Override
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
			info.addPara("%s of damage dealt is instead dealt as %s", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), Math.round(100 * PERCENT) + "%",NAME + " Damage");
			info.addSpacer(10f);
			int HEIGHT = 80;
			float PAD = 10;

			TooltipMakerAPI icon = info.beginImageWithText(Global.getSettings().getSpriteName("ui", "jdp_icon_true"), HEIGHT);
			icon.beginGrid(info.getTextWidthOverride(), 1);
			var START = -1;

			icon.addToGrid(0, START, "Damage type", "");
			icon.addToGrid(40, START, "", NAME);
			icon.addToGrid(40, START+1, "", DESC);
			icon.addToGrid(0, START+3, "Special property", "");
			icon.addToGrid(40, START+3, "", "Damage calculation ignores armor");

			icon.addGrid(PAD);
			info.addImageWithText(PAD);
			info.addSpacer(10f);

		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}
