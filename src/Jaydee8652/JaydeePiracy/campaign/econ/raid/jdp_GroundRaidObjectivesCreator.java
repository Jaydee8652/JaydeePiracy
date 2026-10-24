package Jaydee8652.JaydeePiracy.campaign.econ.raid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.GroundRaidObjectivesListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.graid.CommodityGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.graid.SolarArrayGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;


public class jdp_GroundRaidObjectivesCreator implements GroundRaidObjectivesListener {
	public void init() {
		if(Global.getSector().getPersistentData().get("jdp_GroundRaidObjectivesCreator") == null) {
			Global.getSector().getPersistentData().put("jdp_GroundRaidObjectivesCreator", this);
			Global.getSector().getListenerManager().addListener(this);
		}
	}

	public void modifyRaidObjectives(MarketAPI market, SectorEntityToken entity, List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
		if (priority != 0) return;
		if (market == null) return;

		if (type == RaidType.VALUABLE) {
			if (market.hasCondition(Conditions.VOLTURNIAN_LOBSTER_PENS)) {
				objectives.add(new jdp_LobsterGroundRaidObjectivePluginImpl(market));
			}
		}
	}
	public void reportRaidObjectivesAchieved(RaidResultData data, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
	}
}