package Jaydee8652.JaydeePiracy.campaign.econ.raid;

import java.util.Random;

import Jaydee8652.JaydeePiracy.utils.jdp_Items;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.graid.AbstractGoalGroundRaidObjectivePluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class jdp_LobsterGroundRaidObjectivePluginImpl extends AbstractGoalGroundRaidObjectivePluginImpl {

	public static int XP_GAIN = 10000;
	
	protected MarketConditionAPI condition = null;
	public jdp_LobsterGroundRaidObjectivePluginImpl(MarketAPI market) {
		super(market, RaidDangerLevel.EXTREME);
		condition = market.getCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
	}

	public String getName() {
		return "Raid the " + condition.getName();
	}
	@Override
	public String getIconName() {
		return condition.getSpec().getIcon();
	}
	@Override
	public int getCargoSpaceNeeded() {
		return 3;
	}

	public int performRaid(CargoAPI loot, Random random, float lootMult, TextPanelAPI text) {
		if (marinesAssigned <= 0) return 0;

		market.removeCondition(Conditions.VOLTURNIAN_LOBSTER_PENS);
		market.getIndustry(Industries.AQUACULTURE).setDisrupted(10);

		loot.addSpecial(new SpecialItemData(jdp_Items.JDP_LOBSTER_EGGS, null), 3);

		if (market.getId().equals("volturn")) Global.getSector().getMemoryWithoutUpdate().set("$jdp_stole_sindrian_lobsters", true);

		int xpGained = XP_GAIN;
		return xpGained;
	}
	
	@Override
	public boolean hasTooltip() {
		return true;
	}

	@Override
	public void createTooltip(TooltipMakerAPI t, boolean expanded) {
		t.addPara("Raid the volturnian lobster pens on " + market.getName() + ", stealing 3 clutches of eggs and functionally destroying " +
				"lobster breeding operations. While the aquaculture industry would remain nominally intact, reintroducing a wild volturnian lobster " +
				"population would be impossible without possessing the necessary gene-engineered eggs.", 0f);
	}

}









