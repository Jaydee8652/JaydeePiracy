package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.skills.Sensors
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import second_in_command.SCData
import second_in_command.specs.SCBaseSkillPlugin

class jdp_flowerfishUrbanCamo : SCBaseSkillPlugin() {

    override fun getAffectsString(): String {
        return "fleet"
    }

    override fun addTooltip(data: SCData, tooltip: TooltipMakerAPI) {

        tooltip.addPara("-50%% detected-at range", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addSpacer(10f)

        tooltip.addPara("Affects: ground operations", 0f, Misc.getGrayColor(), Misc.getBasePlayerColor(), "ground operations")
        tooltip.addSpacer(10f)

        tooltip.addPara("+25%% effectiveness of ground operations such as raids", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        tooltip.addPara("-25%% marine casualties during ground operations such as raids", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        tooltip.addSpacer(10f)

        tooltip.addPara("\"Hide amongst those you wish to decieve, in so doing you already accomplish your objective.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltip.addPara("  -Flowerfish Infiltration Manual", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltip.addSpacer(10f);

    }

    override fun applyEffectsBeforeShipCreation(data: SCData, stats: MutableShipStatsAPI?, variant: ShipVariantAPI, hullSize: ShipAPI.HullSize?, id: String?) {


    }

    override fun applyEffectsAfterShipCreation(data: SCData, ship: ShipAPI?, variant: ShipVariantAPI, id: String?) {



    }

    override fun advance(data: SCData, amount: Float) {

    }

    override fun onActivation(data: SCData) {
        data.fleet.stats.detectedRangeMod.modifyMult("jdp_flowerfishUrbanCamo", 0.5f, "Urban Camouflage")
        data.fleet.stats.dynamic.getMod(Stats.PLANETARY_OPERATIONS_MOD).modifyPercent("jdp_flowerfishUrbanCamo", 25f, "Urban Camouflage")
        data.fleet.stats.dynamic.getMod(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).modifyMult("jdp_flowerfishUrbanCamo", 0.75f, "Urban Camouflage")

    }

    override fun onDeactivation(data: SCData) {
        data.fleet.stats.detectedRangeMod.unmodify("jdp_flowerfishUrbanCamo")
        data.fleet.stats.dynamic.getMod(Stats.PLANETARY_OPERATIONS_MOD).unmodify("jdp_flowerfishUrbanCamo")
        data.fleet.stats.dynamic.getMod(Stats.PLANETARY_OPERATIONS_CASUALTIES_MULT).unmodify("jdp_flowerfishUrbanCamo")

    }
}