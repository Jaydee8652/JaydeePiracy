package Jaydee8652.JaydeePiracy.scripts;

import Jaydee8652.JaydeePiracy.utils.jdp_Industries;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.Themes;

import java.util.*;

import static Jaydee8652.JaydeePiracy.campaign.econ.jdp_recivilizedMinority.EFFECT_TYPES;

public class jdp_IndustryPlacer {
    private static final List<IndustryPlacerData> industryPlacerMap = new ArrayList<>();
    public static Map<String, String> MAIN_INDUSTRY = new HashMap<String, String>();
    static String MAIN_INDUSTRY_PERSISTENT = "$jdp_mainIndustryPersistent";

    public static class IndustryPlacerData {
        private final String industry;
        private final String market;
        private final String item;

        public IndustryPlacerData(String industry, String market, String item) {
            this.industry = industry;
            this.market = market;
            this.item = item;
        }

        public IndustryPlacerData(String industry, String market) {
            this.industry = industry;
            this.market = market;
            this.item = null;
        }

        public String getIndustry() {
            return industry;
        }
        public String getMarket() {
            return market;
        }
        public String getItem() {
            return item;
        }
    }
    static {
        industryPlacerMap.add(new IndustryPlacerData(jdp_Industries.JDP_BURECHIRIPARK, "ailmar"));
        industryPlacerMap.add(new IndustryPlacerData(Industries.COMMERCE, "eochu_bres", Items.DEALMAKER_HOLOSUITE));
        if (!Global.getSettings().getModManager().isModEnabled("Mayasuran Navy")) industryPlacerMap.add(new IndustryPlacerData(Industries.TECHMINING, "mairaath"));

        MAIN_INDUSTRY.put("eochu_bres", Industries.COMMERCE);
        MAIN_INDUSTRY.put("baetis", Industries.COMMERCE);
        MAIN_INDUSTRY.put("maxios", Industries.COMMERCE);
        MAIN_INDUSTRY.put("nex_prismFreeport", Industries.COMMERCE);

        MAIN_INDUSTRY.put("eventide", Industries.MILITARYBASE);
        MAIN_INDUSTRY.put("ailmar", Industries.MILITARYBASE);

        MAIN_INDUSTRY.put("agreus", Industries.TECHMINING);
        MAIN_INDUSTRY.put("mairaath", Industries.TECHMINING);

        MAIN_INDUSTRY.put("jangala", Industries.FARMING);
        MAIN_INDUSTRY.put("gilead", Industries.FARMING);

        MAIN_INDUSTRY.put("raesvelg", Industries.REFINING);

        MAIN_INDUSTRY.put("chicomoztoc", Industries.HEAVYINDUSTRY);
        MAIN_INDUSTRY.put("culann", Industries.HEAVYINDUSTRY);
        MAIN_INDUSTRY.put("kazeron", Industries.HEAVYINDUSTRY);
        MAIN_INDUSTRY.put("new_maxios", Industries.HEAVYINDUSTRY);
        MAIN_INDUSTRY.put("asher", Industries.HEAVYINDUSTRY);
        MAIN_INDUSTRY.put("station_keptyen", Industries.HEAVYINDUSTRY);

        MAIN_INDUSTRY.put("sindria", Industries.FUELPROD);
        MAIN_INDUSTRY.put("nachiketa", Industries.FUELPROD);
        MAIN_INDUSTRY.put("madeira", Industries.FUELPROD);
    }

    public static void assignMainIndustry() {
        if (Global.getSector().getMemoryWithoutUpdate().contains(MAIN_INDUSTRY_PERSISTENT)) return;

        for(MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market.getContainingLocation().hasTag(Tags.THEME_CORE_POPULATED)) {
                var industries = market.getIndustries().stream().filter(i -> i.getSpec().hasTag("industry")).toList();
                if (industries.size() == 1 && EFFECT_TYPES.contains(industries.get(0).getId()) && !MAIN_INDUSTRY.containsKey(market.getId())) MAIN_INDUSTRY.put(market.getId(), industries.get(0).getId());
            }
        }
        Global.getSector().getMemoryWithoutUpdate().set(MAIN_INDUSTRY_PERSISTENT, MAIN_INDUSTRY);
    }

    public static void placeIndustries() {
        for (IndustryPlacerData data : industryPlacerMap) {
            var tag = "$jdp_added_" + data.industry + "_to_" + data.market;

            if (Global.getSector().getEconomy().getMarket(data.market) != null) {
                MarketAPI market = Global.getSector().getEconomy().getMarket(data.market);

                if (!market.isPlayerOwned() && !market.getFaction().getId().equals(Global.getSector().getPlayerFaction().getId()) && !Global.getSector().getMemoryWithoutUpdate().contains(tag)) {
                    Global.getSector().getMemoryWithoutUpdate().set(tag, true);

                    if (!market.hasIndustry(data.industry)) market.addIndustry(data.industry);
                    if (data.item != null && market.getIndustry(data.industry).getSpecialItem() == null) market.getIndustry(data.industry).setSpecialItem(new SpecialItemData(data.item, null));
                }
            }
        }
    }
}
