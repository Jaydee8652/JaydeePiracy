package Jaydee8652.JaydeePiracy.scripts.skills;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeePiracy.scripts.skills.jdp_omegakin.DAMAGE_BONUS_PERCENT;
import static Jaydee8652.JaydeePiracy.scripts.skills.jdp_omegakin.jdp_omegakinListener;

public class jdp_deepeutecticgrid {
	public static float DISSIPATION_BONUS = 2f;
	public static float CAPACITY_BONUS = 20f;

	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			var vents = stats.getVariant().getNumFluxVents();
			var caps = stats.getVariant().getNumFluxCapacitors();

			var bonus = Math.min(caps, vents);

			stats.getFluxDissipation().modifyFlat(id, DISSIPATION_BONUS * bonus);
			stats.getFluxCapacity().modifyFlat(id, CAPACITY_BONUS * bonus);
		}
		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
			stats.getFluxDissipation().unmodifyFlat(id);
			stats.getFluxCapacity().unmodifyFlat(id);
		}

		@Override
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
			info.addPara("Capacitors and Vents provide more flux capacity and dissipation when paired", 3f, Misc.getHighlightColor(), Misc.getHighlightColor());
			info.addPara("%s additional flux capacity per pair", 3f, Misc.getHighlightColor(),Misc.getHighlightColor(), "+"+Math.round(CAPACITY_BONUS));
			info.addPara("%s additional flux dissipation per pair", 3f, Misc.getHighlightColor(),Misc.getHighlightColor(), "+"+Math.round(DISSIPATION_BONUS));
		}

		public String getEffectPerLevelDescription() {
			return null;
		}
		
		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}
