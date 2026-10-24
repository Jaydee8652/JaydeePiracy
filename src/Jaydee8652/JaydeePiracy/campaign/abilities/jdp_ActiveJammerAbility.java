package Jaydee8652.JaydeePiracy.campaign.abilities;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.JumpPointAPI.JumpDestination;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.DelayedFleetEncounter;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_MEDIUM;
import static com.fs.starfarer.api.impl.campaign.ids.FleetTypes.PATROL_SMALL;

public class jdp_ActiveJammerAbility extends BaseDurationAbility {

	public static class AbilityUseData {
		public long timestamp;
		public Vector2f location;
		public AbilityUseData(long timestamp, Vector2f location) {
			this.timestamp = timestamp;
			this.location = location;
		}

	}

	protected boolean performed = false;

	protected TimeoutTracker<AbilityUseData> uses = new TimeoutTracker<AbilityUseData>();

	protected Object readResolve() {
		super.readResolve();
		if (uses == null) {
			uses = new TimeoutTracker<AbilityUseData>();
		}
		return this;
	}

	@Override
	protected void activateImpl() {
		if (entity.isInCurrentLocation()) {
			VisibilityLevel level = entity.getVisibilityLevelToPlayerFleet();
			if (level != VisibilityLevel.NONE) {
				//Global.getSector().addPing(entity, SotfIDs.PING_COURSERPROTOCOL);
			}

			performed = false;
		}

	}

	protected String getActivationText() {
		//return Misc.ucFirst(spec.getName().toLowerCase());
		return "Broadcasting...";
	}

	@Override
	protected void applyEffect(float amount, float level) {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;

		if (!performed) {
				float delay = 1f + 1f * (float) Math.random();
				addResponseScript(delay);
			performed = true;
		}
	}

	@Override
	public void advance(float amount) {
		super.advance(amount);

		float days = Global.getSector().getClock().convertToDays(amount);
		uses.advance(days);
	}

	protected void addResponseScript(float delayDays) {
		final CampaignFleetAPI player = getFleet();
		if (player == null) return;
		if (!(player.getContainingLocation() instanceof StarSystemAPI)) return;

		final StarSystemAPI system = (StarSystemAPI) player.getContainingLocation();

		final JumpPointAPI inner = Misc.getDistressJumpPoint(system);
		if (inner == null) return;

		JumpPointAPI outerTemp = null;
		if (inner.getDestinations().size() >= 1) {
			SectorEntityToken test = inner.getDestinations().get(0).getDestination();
			if (test instanceof JumpPointAPI) {
				outerTemp = (JumpPointAPI) test;
			}
		}
		final JumpPointAPI outer = outerTemp;
		if (outer == null) return;

		addHelpScript(delayDays, system, inner, outer);

	}

    // creates the fleet
	protected void addHelpScript(float delayDays,
								 final StarSystemAPI system,
								 final JumpPointAPI inner,
								 final JumpPointAPI outer) {
		Global.getSector().addScript(new DelayedActionScript(delayDays) {
			@Override
			public void doAction() {
				CampaignFleetAPI player = Global.getSector().getPlayerFleet();
				if (player == null) return;
			}
		});
	}

	public boolean isUsable() {
		if (!super.isUsable()) return false;
		if (getFleet() == null) return false;

		CampaignFleetAPI fleet = getFleet();
		if (fleet.isInHyperspace() || fleet.isInHyperspaceTransition()) return false;

		if (fleet.getContainingLocation() != null && fleet.getContainingLocation().hasTag(Tags.SYSTEM_ABYSSAL)) {
			return false;
		}

		return true;
	}

	@Override
	protected void deactivateImpl() {
		cleanupImpl();
	}

	@Override
	protected void cleanupImpl() {
		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;
	}

	@Override
	public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {

		CampaignFleetAPI fleet = getFleet();
		if (fleet == null) return;

		Color gray = Misc.getGrayColor();
		Color highlight = Misc.getHighlightColor();
		Color bad = Misc.getNegativeHighlightColor();

		LabelAPI title = tooltip.addTitle(spec.getName());

		float pad = 10f;

		int followDur = 10;
		int joinRange = 10;
		// be generous with our range description
		if (joinRange > 1000) {
			joinRange -= 250;
		}

		LabelAPI label = tooltip.addPara("Emits an encrypted communication pulse, calling for aid. " +
				"Outrider-Annex-Courser will arrive to the system via a transverse jump and escort you " +
				"for " + followDur + " days, joining you in combat " +
				"as long as they are within " + joinRange + " units.", pad);
		label.setHighlight("Outrider-Annex-Courser", followDur + " days", joinRange + " units");
		label.setHighlightColors(Misc.getHighlightColor(), highlight, highlight);

		if (expanded) {
			tooltip.addPara("Expect a capable fleet commander at the head of an automated hunter-killer " +
							"fleet %s. They are ready to take losses and will salvage more drones for the next " +
							"time you need them.", pad, highlight,
					"of similar size to your own fleet");

			tooltip.addPara("Time taken for broadcast, arrival, escort, and reinforcement " +
					"adds up to %s before another call can be made.", pad, highlight, "180 days");
		}
		if (fleet.isInHyperspace()) {
			tooltip.addPara("Can not be used in hyperspace.", bad, pad);
		}
		if (isOnCooldown()) {
			label = tooltip.addPara("On cooldown for another " + Math.round(getCooldownLeft()) + " days", bad, pad);
			label.setHighlight("" + Math.round(getCooldownLeft()));
			label.setHighlightColors(highlight);
		}

		tooltip.addPara("*2000 units = 1 map grid cell", gray, pad);

		addIncompatibleToTooltip(tooltip, expanded);

	}

	public boolean hasTooltip() {
		return true;
	}

}





