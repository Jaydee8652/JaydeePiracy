package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.ElectronicWarfareScript;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.magiclib.subsystems.MagicSubsystemsManager;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import java.util.List;

import static com.fs.starfarer.api.impl.campaign.skills.ElectronicWarfareScript.BASE_MAXIMUM;

public class jdp_flowerfishOurStrategicAdvantage extends SCBaseSkillPlugin {

    @Override
    public String getAffectsString() {
        return "all ships with human officers";
    }

    @Override
    public void addTooltip(SCData scData, TooltipMakerAPI tooltipMakerAPI) {
        tooltipMakerAPI.addPara("Combat objectives are captured 2x faster and can be captured from 300 units further away", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("   - Each captured combat objective increases this bonus by 1 and 100 units respectively", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "1", "100");
        tooltipMakerAPI.addSpacer(10f);

        tooltipMakerAPI.addPara("\"The expertise of Maerula is our strategic advantage.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltipMakerAPI.addPara("  -Flowerfish First Okonomi", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltipMakerAPI.addSpacer(10f);
    }


    @Override
    public void advance(SCData data, Float amount) {}

    @Override
    public void applyEffectsBeforeShipCreation(SCData data, MutableShipStatsAPI stats, ShipVariantAPI variant, ShipAPI.HullSize hullSize, String id) {
    }

    @Override
    public void onActivation(SCData data) {
    }

    @Override
    public void onDeactivation(SCData data) {}

    @Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {
        List<BattleObjectiveAPI> objectives = Global.getCombatEngine().getObjectives();
        int held = (int) objectives.stream().filter(it -> it.getOwner() == ship.getOwner()).count();

        if (jdp_AptitudeFlowerfish.isHumanOfficer(ship)) {
            ship.getMutableStats().getDynamic().getMod(Stats.SHIP_OBJECTIVE_CAP_RANGE_MOD).modifyFlat("jdp_flowerfishOurStrategicAdvantage", 300f + (held * 100f));
            ship.getMutableStats().getDynamic().getStat(Stats.SHIP_OBJECTIVE_CAP_RATE_MULT).modifyMult("jdp_flowerfishOurStrategicAdvantage", 2f + (held));

            //ship.getMutableStats().getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat("jdp_flowerfishOurStrategicAdvantage", PER_SHIP_BONUS);
        }
    }
}
