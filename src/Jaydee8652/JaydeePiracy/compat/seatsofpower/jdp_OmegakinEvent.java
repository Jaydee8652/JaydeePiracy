package Jaydee8652.JaydeePiracy.compat.seatsofpower;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.events.LuddicChurchHostileActivityFactor;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.models.TimelineEventType;
import data.scripts.timelineevents.templates.DefeatMajorFactionCrisisEvent;

public class jdp_OmegakinEvent extends DefeatMajorFactionCrisisEvent {
    public jdp_OmegakinEvent() {
        super(Factions.OMEGA);
    }

    @Override
    public String getImagePath() {
        return Global.getSettings().getSpriteName("illustrations", "jdp_derelictpenrose");
    }

    @Override
    public String getTitleOfEvent() {
        return "Deus Ex Machina";
    }

    @Override
    public TimelineEventType getEventType() {
        return TimelineEventType.UNIQUE;
    }

    @Override
    public int getPointsForGoal() {
        return 50;
    }

    @Override
    public boolean checkForCondition() {
        return Global.getSector().getMemory().getBoolean("$jdp_hasOmegakin");
    }

    @Override
    public void createDetailedTooltipOnHover(TooltipMakerAPI tooltip) {
        super.createDetailedTooltipOnHover(tooltip);
        tooltip.addPara(
                "%s of the %s has communed with Omega, the Machine God, and been chosen as its \"kin\". The wider implications of this pact are unknown, but its is surely a good pain.",
                5f,
                Misc.getHighlightColor(),
                "Captain " + Global.getSector().getPlayerPerson().getNameString(),
                Global.getSector().getPlayerFaction().getDisplayNameLong()
        );
    }

    @Override
    public void createSmallNoteForEvent(TooltipMakerAPI tooltip) {
        tooltip.addPara(
                "Captain " + Global.getSector().getPlayerPerson().getNameString() + " has communed with Omega",
                Misc.getTextColor(),
                0f
        ).setAlignment(Alignment.MID);
    }
}
