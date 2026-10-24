package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeePiracy.scripts.jdp_CreateFlowerfishExecutive;
import com.fs.starfarer.D.M;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.PlayerFleetPersonnelTracker;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

public class jdp_marinetraining implements EconomyTickListener {
	static String MARINE_TRAINING_KEY = "jdp_marineTraining";
	static Float EXPERIENCE_GAIN = 2f;

	public void init() {
		if(Global.getSector().getPersistentData().get("jdp_marinetraining") == null) {
			Global.getSector().getPersistentData().put("jdp_marinetraining", this);
			Global.getSector().getListenerManager().addListener(this);
		}
	}

	@Override
	public void reportEconomyTick(int iterIndex) {
		List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy().stream().filter(m -> m.hasTag(MARINE_TRAINING_KEY)).toList();
		if (markets.isEmpty()) return;

		for (MarketAPI market : markets) {
			if (!market.isPlayerOwned()) return;

			PlayerFleetPersonnelTracker.PersonnelAtEntity personnel = PlayerFleetPersonnelTracker.getInstance().getDroppedOffAt(Commodities.MARINES, market.getPrimaryEntity(), Misc.getStorage(market).getSubmarket(), true);

			if (personnel != null) {
				SubmarketPlugin cargo = Misc.getStorage(market);
				float marineCount = personnel.data.num;
				if (marineCount == 0f && cargo.getCargo().getMarines() > 0) {
					personnel.data.add(cargo.getCargo().getMarines());
				}

				float experience = (EXPERIENCE_GAIN * market.getHazard().getModifiedValue());
				personnel.data.addXP(experience);
			}
		}
	}

	@Override
	public void reportEconomyMonthEnd() {

	}


	public static class Level1 extends BaseSkillEffectDescription implements MarketSkillEffect {
		public void apply(MarketAPI market, String id, float level) {
			market.addTag(MARINE_TRAINING_KEY);
		}

		public void unapply(MarketAPI market, String id) {
			market.removeTag(MARINE_TRAINING_KEY);
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

			info.addPara("Stored marines passively gain experience at a rate proportional to hazard rating", Misc.getHighlightColor(), 3f);
		}

		public String getEffectDescription(float level) {
			return null;
		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.GOVERNED_OUTPOST;
		}
	}
}