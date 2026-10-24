package Jaydee8652.JaydeePiracy.scripts.intel.admin

import Jaydee8652.JaydeePiracy.campaign.econ.jdp_recivilizedMinority.appendDescription
import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.INDUSTRY_NAMES
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.ui.Alignment

class SubpopulationTooltipCreator(var type: String, var market: MarketAPI, var full: Boolean) : BaseTooltipCreator() {
    override fun getTooltipWidth(tooltipParam: Any?): Float {
        return 400f
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
        if(full) {
            tooltip!!.addPara("Gauss rifle, schoolhouse and charter have forcefully reintegrated " + market.name + "'s indigenous population, scattered into subcultures by the collapse of the previous governing authority.", 3f);
            tooltip.addPara("%s population growth (based on colony size)", 10f, Misc.getHighlightColor(), "+" + market.size);
            tooltip.addSpacer(10f);
        }
        tooltip!!.addSectionHeading("Specialises in " + INDUSTRY_NAMES.get(type), market.faction.baseUIColor, market.faction.darkUIColor, Alignment.MID, 10f);
        appendDescription(tooltip, market, type);
    }
}