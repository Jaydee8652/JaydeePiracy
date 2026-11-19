package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.*;

import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_ATOMICEXTRACTION;

public class jdp_radicalOpportunism {
	public static float BONUS = 3f;
	public static String NAME = Global.getSettings().getSkillSpec(jdp_Skills.JDP_RADICAL).getName();

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			var unrest = Global.getSector().getEconomy().getMarketsCopy().stream().map(RecentUnrest::getPenalty).mapToInt(Integer::intValue).sum();
			market.getIncomeMult().modifyPercent(id, BONUS * unrest, NAME);
		}

		public void unapply(MarketAPI market, String id) {
			market.getIncomeMult().unmodifyPercent(id);
		}

		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {
			info.addPara("Increases colony income by %s per point of Recent Unrest in the sector", 3f, Misc.getHighlightColor(), Misc.getHighlightColor(), Math.round(BONUS)+"%");
		}

        public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.CUSTOM;
		}
	}
}