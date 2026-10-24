package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_DisplayElement;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_Stats;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;

import java.util.List;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.*;

public class jdp_hacking {
	public static float HACKING_MULT = 3f;
	public static float COMM_SNIFFER_FAILURE_MULT = 0.5f;

	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			if(market.isPlayerOwned()) {

				Industry military = getIndustryByTag(market, MILITARY_TAGS, Industries.TAG_PATROL);

				SpecialItemData special = null;
				if(military != null) special = military.getSpecialItem();

				if ((military != null) && (military.isFunctional()) && (special != null) && (special.getId().equals(Items.CRYOARITHMETIC_ENGINE))) {
					Global.getSector().getPlayerPerson().getStats().getDynamic().getStat(jdp_Stats.COMM_SNIFFER_FAILURE_MULT).modifyMult(id, 0f);
					Global.getSector().getPlayerPerson().getStats().getDynamic().getStat(jdp_Stats.HACKING_MULT).modifyMult(id, HACKING_MULT);
				} else {
					Global.getSector().getPlayerPerson().getStats().getDynamic().getStat(jdp_Stats.COMM_SNIFFER_FAILURE_MULT).modifyMult(id, COMM_SNIFFER_FAILURE_MULT);
				}
				Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().set(jdp_Stats.HACKING_INVERSION_ACTIVE, true);

				var script = new DelayedActionScript(5f) {
					@Override
					public void doAction() {
						market.removeTag(id);
						if (market.getAdmin().getStats().hasSkill(id.replace("_GO_0", ""))) return;

						Global.getSector().getPlayerPerson().getStats().getDynamic().getStat(jdp_Stats.HACKING_MULT).unmodifyMult(id);
						Global.getSector().getPlayerPerson().getStats().getDynamic().getStat(jdp_Stats.COMM_SNIFFER_FAILURE_MULT).unmodifyMult(id);
						Global.getSector().getPlayerPerson().getMemoryWithoutUpdate().unset(jdp_Stats.HACKING_INVERSION_ACTIVE);
					}
				};
				if(!market.hasTag(id)) {
					market.addTag(id);
					Global.getSector().addScript(script);
				}
			}
		}

		public void unapply(MarketAPI market, String id) {
		}

		@Override
		public void createCustomDescription(
				MutableCharacterStatsAPI stats,
				SkillSpecAPI skill,
				TooltipMakerAPI info,
				float width) {

			info.addPara("Hacked nav buoys and sensor arrays instead reduce the stats of affected fleets (excluding the hacker)", 3f, Misc.getHighlightColor(), "reduce the stats of affected fleets");
			info.addPara("The chance for comm sniffers to be detected and removed is halved", 3f, Misc.getHighlightColor(), "halved");
			info.addSpacer(10f);
			info.addPara("Affects: governed colony faction (with Cryoarithmetic Engine)", 0f, Misc.getGrayColor(), Misc.getBasePlayerColor(), "governed colony faction (with Cryoarithmetic Engine)");
			info.addSpacer(10f);
			info.addPara("Increases nav buoy and sensor array hack duration by " +Math.round(HACKING_MULT)+ "x", 3f, Misc.getHighlightColor(), Math.round(HACKING_MULT)+ "x");
			info.addPara("Comm sniffers can never be detected and removed", 3f, Misc.getHighlightColor(), "never be detected and removed");
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.CUSTOM;
		}
	}
}