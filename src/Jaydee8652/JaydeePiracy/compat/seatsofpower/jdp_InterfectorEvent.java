package Jaydee8652.JaydeePiracy.compat.seatsofpower;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.models.TimelineEventType;
import data.scripts.timelineevents.templates.FirstItemInstalled;

public class jdp_InterfectorEvent extends FirstItemInstalled {
    public jdp_InterfectorEvent() {
        super(Items.PLANETKILLER);
    }

    @Override
    public String getImagePath() {
        return Global.getSettings().getSpriteName("illustrations", "jdp_missile");
    }

    @Override
    public String getTitleOfEvent() {
        return "Assured Destruction";
    }

    @Override
    public TimelineEventType getEventType() {
        return TimelineEventType.MILITARY;
    }

    @Override
    public int getPointsForGoal() {
        return 100;
    }

    @Override
    public void createDetailedTooltipOnHover(TooltipMakerAPI tooltip) {
        super.createDetailedTooltipOnHover(tooltip);
        tooltip.addPara(
                "A \"functional\" Planetkiller Launch Vehicle has been activated on %s, an installation representing the greatest bluff the Sector has ever seen. %s now stand protected by the threat of punitive Terracide.",
                10f,
                Misc.getHighlightColor(),
                getName(),
                Global.getSector().getPlayerFaction().getDisplayNameLong()
        );
    }

    @Override
    public void createSmallNoteForEvent(TooltipMakerAPI tooltip) {
        tooltip.addPara(
                "A \"functional\" Planetkiller Launch Vehicle has been activated on " + this.getName(),
                Misc.getTextColor(),
                0f).setAlignment(Alignment.MID);
    }
}