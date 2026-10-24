package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_Stats;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class jdp_techmortician {

	public static float TECH_BONUS = 1f;


	public static class Level1 implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).modifyMult(id, 1f + TECH_BONUS);
		}

		public void unapply(MarketAPI market, String id) {
			market.getStats().getDynamic().getStat(Stats.TECH_MINING_MULT).unmodifyMult(id);
		}

		public String getEffectDescription(float level) {
			return "+" + (int) Math.round(TECH_BONUS * 100f) + "% finds from tech-mining";
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}