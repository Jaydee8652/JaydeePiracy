package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_Stats;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class jdp_counterintelligence {
	public static float INTEREST_MULT = 0f;

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			//market.getAdmin().getMemoryWithoutUpdate().set("anti_luddic_admin", true);//TODO: NOT REAL YET
			market.getStats().getDynamic().getStat(jdp_Stats.PATHER_INTEREST_MULT).modifyMult(id, INTEREST_MULT);
		}

		public void unapply(MarketAPI market, String id) {
			//market.getAdmin().getMemoryWithoutUpdate().unset("anti_luddic_admin");
			market.getStats().getDynamic().getStat(jdp_Stats.PATHER_INTEREST_MULT).unmodifyMult(id);
		}

		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {
			info.addPara("Nullfies pather interest from industries and AI cores", Misc.getHighlightColor(), 3f);
			//info.addPara("Luddic Majority is permanently negated",  Misc.getHighlightColor(), 3f);
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}