package Jaydee8652.JaydeePiracy.campaign.missions.academy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.missions.academy.GABaseMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.world.TTBlackSite;
import Jaydee8652.JaydeePiracy.campaign.world.jdp_Hiroc;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

public class jdp_InvestigateMissile extends GABaseMission { //implements ShipRecoveryListener {

	public static enum Stage {
		GO_TO_NASCENT_WELL,
		INVESTIGATE_SITE,
		RETURN_TO_ACADEMY,
		COMPLETED,
	}

	protected PersonAPI sebestyen;
	protected NascentGravityWellAPI well;
	protected StarSystemAPI missileSite;
	
	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// If already accepted by the player, abort
		if (!setGlobalReference("$jdp_IMref", "$jdp_IMinProgress")) {
			return false;
		}

		//Protected things
		sebestyen = getImportantPerson(People.SEBESTYEN);
		if (sebestyen == null) return false;

		well = (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(jdp_Hiroc.JDP_NASCENT_WELL_KEY);
		if (well == null || !well.isAlive()) return false;

		missileSite = (StarSystemAPI) well.getTarget().getContainingLocation();
		requireSystemIs(missileSite);

		SectorEntityToken interfector = (SectorEntityToken) Global.getSector().getEntityById("derelict_missile");
		requireEntityMemoryFlags("$derelict_missile");

		//Start and end
		setStartingStage(Stage.GO_TO_NASCENT_WELL);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();

		//Set stages-

		//Go to well
		setStageOnEnteredLocation(Stage.INVESTIGATE_SITE, missileSite);

		//Return with data
		setStageOnGlobalFlag(Stage.RETURN_TO_ACADEMY, "$jdp_exploredHiroc");

		setStageOnGlobalFlag(Stage.COMPLETED, "$jdp_IMmissionCompleted");



		//Make Blacksite Well Important
		makeImportant(well, null, Stage.GO_TO_NASCENT_WELL);

		//Make Interfector Important
		makeImportant(interfector, "$jdp_interfector_important", Stage.INVESTIGATE_SITE);

		//Make Sebestyen Important
		makeImportant(sebestyen, "$jdp_IMreturnHere", Stage.RETURN_TO_ACADEMY);

		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValuePermanent("$jdp_IMmissionCompleted", true);
		triggerSetGlobalMemoryValuePermanent("$jdp_exploredHiroc", true);
		endTrigger();

		return true;
	}

	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_NASCENT_WELL) {
			info.addPara("The Department of Hyperspace Physics at Galatia have discovered "
					+ "an anomalous \"Driessig Distortion\" near Arcadia. Academician Alviss Sebestyen has "
					+ "tasked you with investigating the hyperspace coordinates off the books to satiate his curiosity.", opad);
			if (well.isInCurrentLocation() && Misc.getDistanceToPlayerLY(well) < 0.2f) {
				info.addPara("Use %s to traverse the nascent gravity well located at the coordinates.",
						opad, Misc.getHighlightColor(), "Transverse Jump");
			}
		} else if (currentStage == Stage.INVESTIGATE_SITE) {
			info.addPara("Investigate the Domain-era megastructure in system", opad);
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy with the data and report your findings to " +
					 getPerson().getNameString() + ".", opad);
		}
	}

	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.GO_TO_NASCENT_WELL) {
			info.addPara("Investigate the hyperspace area where the anomaly was found", tc, pad);
			return true;
		} else if (currentStage == Stage.INVESTIGATE_SITE) {
			info.addPara("Investigate Derelict", tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_TO_ACADEMY) {
			info.addPara("Return to the Galatia Academy and report to Academician Alviss Sebestyen", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "A History Mystery";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}





