package Jaydee8652.JaydeePiracy.scripts;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class jdp_DemonLeashAssignmentAI extends BaseAssignmentAI {
    protected float elapsed = 0.0F;
    protected float dur = 30.0F + (float)Math.random() * 20.0F;
    protected SectorEntityToken toGuard;
    protected IntervalUtil moteSpawn = new IntervalUtil(0.01F, 0.1F);

    public jdp_DemonLeashAssignmentAI(CampaignFleetAPI fleet, SectorEntityToken toGuard) {
        this.fleet = fleet;
        this.toGuard = toGuard;
        this.giveInitialAssignments();
    }

    protected void giveInitialAssignments() {
        this.pickNext();
    }

    protected void pickNext() {
        this.fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, this.toGuard, 100.0F);
    }

    public void advance(float amount) {
        super.advance(amount);
        if (this.toGuard != null) {
            float dist = Misc.getDistance(this.fleet.getLocation(), this.toGuard.getLocation());
            if (dist > this.toGuard.getRadius() + this.fleet.getRadius() + 1500.0F && this.fleet.getAI().getCurrentAssignmentType() == FleetAssignment.ORBIT_AGGRESSIVE) {
                this.fleet.addAssignmentAtStart(FleetAssignment.ORBIT_PASSIVE, this.toGuard, 1.0F, (Script)null);
                CampaignFleetAIAPI ai = this.fleet.getAI();

                if (ai instanceof ModularFleetAIAPI) {
                    ModularFleetAIAPI m = (ModularFleetAIAPI)ai;
                    m.getStrategicModule().getDoNotAttack().add(m.getTacticalModule().getTarget(), 1.0F);
                    m.getTacticalModule().setTarget((SectorEntityToken)null);
                }
            }
        }
    }
}

