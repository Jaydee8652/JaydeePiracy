package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import second_in_command.SCData;
import second_in_command.specs.SCBaseSkillPlugin;

import java.util.List;

public class jdp_flowerfishPerpetualPursuit extends SCBaseSkillPlugin {
    int SPEED = 40;



    @Override
    public String getAffectsString() {
        return "fleet";
    }

    @Override
    public void addTooltip(SCData data, TooltipMakerAPI tooltip) {
        tooltip.addPara("Ships deployed by the flanks gain an increase in speed", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("   - The ships maximum speed is increased by 40", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "40");
        tooltip.addPara("   - The increase in speed is nullified the moment its weapons are in range of a hostile ship", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "");
        tooltip.addSpacer(10f);

        tooltip.addPara("If two or more combat objectives are captured, all ship sizes can be deployed by the flanks", 0f, Misc.getHighlightColor(), Misc.getHighlightColor());
        tooltip.addPara("   - Applies to all combat scenarios", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "");
        tooltip.addPara("   - Requirement is met by default in engagements without combat objectives", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "");
        tooltip.addSpacer(10f);

        tooltip.addPara("\"It is so quiet, right before a battle is set into motion. A lot can be accomplished in that interstitial space.\"", 0f, Misc.getTextColor(), Misc.getHighlightColor());
        tooltip.addPara("  -Flowerfish First Wasabi", 0f, jdp_AptitudeFlowerfish.JDP_FLOWERFISH_COLOUR, Misc.getHighlightColor());
        tooltip.addSpacer(10f);
    }

    @Override
    public void applyEffectsAfterShipCreation(SCData data, ShipAPI ship, ShipVariantAPI variant, String id) {
    }

    @Override
    public void advance(SCData data, Float amunt) {
        data.getCommander().getStats().getDynamic().getMod(Stats.CAN_DEPLOY_LEFT_RIGHT_MOD).unmodify("jdp_flowerfishPerpetualPursuit");
    }

    @Override
    public void advanceInCombat(SCData data, ShipAPI ship, Float amount) {
        if (!ship.hasTag("jdp_flowerfishPerpetualPursuitFlank") && (ship.getLocation().getX() >= (Global.getCombatEngine().getMapWidth() / 2) || (ship.getLocation().getX() <= -(Global.getCombatEngine().getMapWidth() / 2)))) {
            ship.getMutableStats().getMaxSpeed().modifyFlat("jdp_flowerfishPerpetualPursuit", 40f);
            ship.getMutableStats().getAcceleration().modifyFlat("jdp_flowerfishPerpetualPursuit", 20f);
            ship.getMutableStats().getDeceleration().modifyFlat("jdp_flowerfishPerpetualPursuit", 20f);
        }

        ship.addTag("jdp_flowerfishPerpetualPursuitFlank");

        if (ship.areAnyEnemiesInRange()) {
            ship.getMutableStats().getMaxSpeed().unmodify("jdp_flowerfishPerpetualPursuit");
            ship.getMutableStats().getAcceleration().unmodify("jdp_flowerfishPerpetualPursuit");
            ship.getMutableStats().getDeceleration().unmodify("jdp_flowerfishPerpetualPursuit");
        }
    }

    @Override
    public void onActivation(SCData data) {
        data.getCommander().addTag("jdp_flowerfishPerpetualPursuit");
    }
    @Override
    public void onDeactivation(SCData data) {
        data.getCommander().removeTag("jdp_flowerfishPerpetualPursuit");
    }
}