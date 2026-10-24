package Jaydee8652.JaydeePiracy.scripts;

import Jaydee8652.JaydeePiracy.plugins.jdp_lunaSettings;
import Jaydee8652.JaydeePiracy.utils.jdp_Factions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;

import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;

public class jdp_CreateFlowerfishExecutive implements EconomyTickListener {
    String prev;

    public void init() {
        if(Global.getSector().getPersistentData().get("jdp_CreateFlowerfishExecutive") == null) {
            Global.getSector().getPersistentData().put("jdp_CreateFlowerfishExecutive", this);
            Global.getSector().getListenerManager().addListener(this);
            reportEconomyMonthEnd();
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }

    @Override
    public void reportEconomyMonthEnd() {
        if(jdp_lunaSettings.jdp_experimentalFlowerfish().equals(false) || !Global.getSettings().getModManager().isModEnabled("second_in_command")) return;

        MarketAPI market = Global.getSector().getEconomy().getMarket("ailmar");
        if(market == null) return; // If the market doesn't exist, just quit

        if(prev != null) market.getCommDirectory().removeEntry(prev); // Clear last month's XO
        prev = null;

        // Make a random Flowerfish
        PersonAPI person = market.getFaction().createRandomPerson(FullName.Gender.FEMALE);
        person.setFaction(jdp_Factions.JDP_FLOWERFISH);
        person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "jdp_wasabi"));
        person.getMemoryWithoutUpdate().set("$sc_officer_aptitude","jdp_flowerfish");
        person.getMemoryWithoutUpdate().set("$sc_hireable", true);
        person.setPostId("executive_officer_jdp_flowerfish");

        prev = market.getCommDirectory().addPerson(person);
    }
}
