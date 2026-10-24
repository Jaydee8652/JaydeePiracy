package Jaydee8652.JaydeePiracy.scripts.skills;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.input.Keyboard;


import java.awt.*;

import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_AI;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.addBrackets;

public class jdp_ai {
	public static String NAME = Global.getSettings().getSkillSpec(JDP_AI).getName();
	public static int MOD = 1;

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {

		public void apply(MarketAPI market, String id, float level) {
			var industries = market.getIndustries().stream().filter(x->x.getAICoreId() != null).toList();
			for(Industry industry : industries) {
				if (industry.getSupplyBonus().getFlatMods().values().stream().anyMatch(x -> x.desc == "Alpha core")) {
					unapply(market, id);
				} else {
					CommoditySpecAPI spec = Global.getSettings().getCommoditySpec(industry.getAICoreId());
					industry.getSupplyBonusFromOther().modifyFlat(id, MOD, NAME + addBrackets(spec.getName()));
				}
			}
		}

		public void unapply(MarketAPI market, String id) {
			for(Industry industry : market.getIndustries()) {
				industry.getSupplyBonusFromOther().unmodify(id);
			}
		}

		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {
			float pad = 3f;
			float opad = 10f;
			Color high = Misc.getHighlightColor();
			Color bad = Misc.getNegativeHighlightColor();
			Color good = Misc.getPositiveHighlightColor();

			Color neutral = Misc.getGrayColor();

			info.addPara("Industries with AI cores supply one more unit of all the commodities they produce", Misc.getHighlightColor(), 3f);
			info.addPara("*Does not apply if the installed AI core already increases production", Misc.getGrayColor(), 3f);
			info.addSpacer(10f);
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.CUSTOM;
		}
	}
}