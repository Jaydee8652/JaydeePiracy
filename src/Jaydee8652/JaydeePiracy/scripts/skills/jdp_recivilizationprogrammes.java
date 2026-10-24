package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_DelayedDepletionScript;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.*;

import static Jaydee8652.JaydeePiracy.campaign.econ.jdp_recivilizedMinority.STABILITY_PENALTY;
import static Jaydee8652.JaydeePiracy.utils.jdp_Conditions.JDP_RECIVILIZEDMINORITY;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonAdmin;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.addTerraformingDisplay;


public class jdp_recivilizationprogrammes {


	private static final List<TerraformingProcessData> terraformingProcessMap = new ArrayList<>();
	private static final List<List<TerraformingProcessData>> primaryProcessLines = new ArrayList<>();

	static {
		List<TerraformingProcessData> line;

		line = new ArrayList<>();
		line.add(new TerraformingProcessData(null, JDP_RECIVILIZEDMINORITY, Conditions.DECIVILIZED_SUBPOP));
		primaryProcessLines.add(line);

		for(List<TerraformingProcessData> processLine: primaryProcessLines) terraformingProcessMap.addAll(processLine);
	}

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if (!market.hasTag(id) && terraformingProcessMap.stream().anyMatch(p -> p.getWeight() == null && p.isAllowed(market))) Global.getSector().addScript(new jdp_DelayedDepletionScript(0f, market, id, 5, 1, terraformingProcessMap));
		}

		public void unapply(MarketAPI market, String id) {
		}

		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {
			float opad = 3f;
			Color high = Misc.getHighlightColor();
			Color bad = Misc.getNegativeHighlightColor();
			Color good = Misc.getPositiveHighlightColor();

			MarketAPI market = null;
			var admin = getPersonAdmin(stats);
			if (admin != null) market = admin.getMarket();
			if (Global.getSector().getCurrentlyOpenMarket() != null) market = Global.getSector().getCurrentlyOpenMarket();

			info.addPara("Over four months, converts Decivilized Subpopulation into Recivilized Minority", 3f,  Misc.getTextColor(), Misc.getHighlightColor(), "Decivilized Subpopulation", "Recivilized Minority");
			info.addPara("*Recivilized Minority grants bonuses to one industry based on the experience of the markets previous population.",  Misc.getGrayColor(), 3f);
			info.addSpacer(10f);
			info.addPara("Primary process:", Misc.getBasePlayerColor(),3f);
			info.addSpacer(3f);

			CustomPanelAPI previous = addTerraformingDisplay(market, primaryProcessLines.get(0), Misc.getBasePlayerColor(), info, width, 40f, 6f);

			MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(JDP_RECIVILIZEDMINORITY);

			info.addPara(spec.getName(), high, 3f).getPosition().rightOfTop(previous, 6f);;
			info.addPara("%s population growth (based on colony size)", opad, Misc.getHighlightColor(), "+3");
			info.addPara("Variable bonus, see codex", opad, Misc.getTextColor(), Misc.getHighlightColor(),"Variable bonus");
			info.setHeightSoFar(info.getHeightSoFar() - 50f);

			var space = info.addPara("", high, 3f);
			space.getPosition().setXAlignOffset(-(2 * 40f + 5 * 6f));
			info.addPara("Recivilized Minority can be relocated to other markets by clicking on the condition.", 3f, Misc.getTextColor(), Misc.getHighlightColor(), "relocated to other markets");
			info.addPara("*Causes " + STABILITY_PENALTY + " point of unrest to both markets.", 3f, Misc.getGrayColor(), Misc.getHighlightColor(), STABILITY_PENALTY + "");
		}

		public String getEffectDescription(float level) {return null;}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}