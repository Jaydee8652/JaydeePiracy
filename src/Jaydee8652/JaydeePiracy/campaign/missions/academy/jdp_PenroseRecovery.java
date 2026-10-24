package Jaydee8652.JaydeePiracy.campaign.missions.academy;

import java.awt.Color;
import java.util.Arrays;

import Jaydee8652.JaydeePiracy.utils.jdp_Factions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.People;
import com.fs.starfarer.api.impl.campaign.missions.academy.GABaseMission;
import com.fs.starfarer.api.impl.campaign.missions.askonia.TheUsurpers;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import Jaydee8652.JaydeePiracy.utils.jdp_People;


public class jdp_PenroseRecovery extends GABaseMission {
	public static enum Stage {
		INVESTIGATE_DORY,
		DEFEAT_PENROSE,
		INVESTIGATE_DERELICT,
		FIND_ALPHA,
		FIND_MAERULA,
		RETRIEVE_MAERULA,
		RETRIEVE_GARGOYLE,
		RETURN_DATA,
		COMPLETED,
	}

	public static String PENROSE_FOUND = "$jdp_penroseFound";
	public static String PENROSE_DEFEATED = "$jdp_penroseDefeated";
	public static String NO_ALPHA = "$jdp_penroseNoAlpha";
	public static String HUNTING_MAERULA = "$jdp_penroseHuntingMaerula";
	public static String MAERULA_COMM = "$jdp_PRmaerulaComm";
	public static String HUNTING_GARGOYLE = "$jdp_penroseHuntingGargoyle";
	public static String HAS_DATA = "$jdp_penroseHasData";


	//Protected
	protected PersonAPI jdp_maerula;
	protected PersonAPI gargoyle;

	@Override
	protected boolean create(MarketAPI createdAt, boolean barEvent) {
		// if already accepted by the player, abort
		if (!setGlobalReference("$jdp_PRref", "$jdp_PRinProgress")) {
			return false;
		}

		jdp_maerula = getImportantPerson(jdp_People.JDP_MAERULA);
		gargoyle = getImportantPerson(People.GARGOYLE);
		if (gargoyle == null) return false;
		if (jdp_maerula == null) return false;

		MarketAPI ailmar;
		ailmar = getMarket("ailmar");
		if (ailmar == null) return false;
		if (!ailmar.getFactionId().equals("independent")) return false;



		SectorEntityToken penrose_fleet = (SectorEntityToken) Global.getSector().getEntityById("jdp_penrosefleet");

		//Start and End
		setStartingStage(Stage.INVESTIGATE_DORY);
		addSuccessStages(Stage.COMPLETED);
		
		setStoryMission();

		//Set stages-

		//Defeat
		connectWithGlobalFlag(Stage.INVESTIGATE_DORY, Stage.DEFEAT_PENROSE, PENROSE_FOUND);

		//Investigate
		connectWithGlobalFlag(Stage.DEFEAT_PENROSE, Stage.INVESTIGATE_DERELICT, PENROSE_DEFEATED);

		//Find an Alpha
		connectWithGlobalFlag(Stage.INVESTIGATE_DERELICT, Stage.FIND_ALPHA, NO_ALPHA);

		//Speak to the Flowerfish Fleet
		connectWithGlobalFlag(Stage.FIND_ALPHA, Stage.FIND_MAERULA, HUNTING_MAERULA);
		connectWithGlobalFlag(Stage.INVESTIGATE_DERELICT, Stage.FIND_MAERULA, HUNTING_MAERULA);

		//Speak to Maerula
		connectWithGlobalFlag(Stage.FIND_MAERULA, Stage.RETRIEVE_MAERULA, MAERULA_COMM);

		//Speak to Gargoyle
		connectWithGlobalFlag(Stage.INVESTIGATE_DERELICT, Stage.RETRIEVE_GARGOYLE, HUNTING_GARGOYLE);
		connectWithGlobalFlag(Stage.FIND_ALPHA, Stage.RETRIEVE_GARGOYLE, HUNTING_GARGOYLE);
		connectWithGlobalFlag(Stage.RETRIEVE_MAERULA, Stage.RETRIEVE_GARGOYLE, HUNTING_GARGOYLE);

		//Return with data
		connectWithGlobalFlag(Stage.RETRIEVE_GARGOYLE, Stage.RETURN_DATA, HAS_DATA);

		setStageOnGlobalFlag(Stage.COMPLETED, "$jdp_PRCompleted");


		//Make Penrose Important
		makeImportant(penrose_fleet, "$jdp_penrose_important", Stage.INVESTIGATE_DORY);
		makeImportant(penrose_fleet, "$jdp_penrose_important", Stage.DEFEAT_PENROSE);

		//Make Derelict Important Initial
		beginStageTrigger(Stage.INVESTIGATE_DERELICT);
		jdp_triggerEntityMakeImportant("$jdp_penrose_importantInitial", "jdp_penrose_derelict", Stage.INVESTIGATE_DERELICT);
		endTrigger();

		//Make Derelict Important Alpha
		beginStageTrigger(Stage.FIND_ALPHA);
		jdp_triggerEntityMakeImportant("$jdp_penrose_importantAlpha", "jdp_penrose_derelict", Stage.FIND_ALPHA);
		endTrigger();

		//Flowerfish Fleet Important
		beginStageTrigger(Stage.FIND_MAERULA);
		triggerCreateFleet(FleetSize.SMALL, FleetQuality.SMOD_1, jdp_Factions.JDP_FLOWERFISH, FleetTypes.PATROL_MEDIUM, ailmar.getStarSystem());
		triggerMakeNonHostile();
		triggerMakeFleetIgnoredByOtherFleets();
		triggerMakeFleetIgnoreOtherFleets();
		triggerPickLocationAroundEntity(ailmar.getPlanetEntity(), 200f);
		triggerSetFleetMissionRef("$jdp_PRref"); // so they can be made unimportant
		triggerFleetMakeImportant(null, Stage.FIND_MAERULA);
		triggerSaveGlobalFleetRef("$jdp_PRflowerfishFleet");
		triggerSetPatrol();
		triggerOrderFleetPatrol(ailmar.getStarSystem());
		triggerSpawnFleetAtPickedLocation("$jdp_PRflowerfishFleetTalk", null);
		endTrigger();

		//Make Maerula Important
		makeImportant(jdp_maerula, "$jdp_PRreturnHere", Stage.RETRIEVE_MAERULA);

		//Make Gargoyle Important
		makeImportant(gargoyle, "$jdp_PRreturnHere", Stage.RETRIEVE_GARGOYLE);

		//Make Derelict Important Return
		beginStageTrigger(Stage.RETURN_DATA);
		jdp_triggerEntityMakeImportant("$jdp_penrose_importantData", "jdp_penrose_derelict", Stage.RETURN_DATA);
		endTrigger();

		beginStageTrigger(Stage.COMPLETED);
		triggerSetGlobalMemoryValuePermanent("$jdp_PRmissionCompleted", true);
		endTrigger();
		
		return true;
	}

	public void jdp_triggerEntityMakeImportant(String flag, String entityID, Enum ... stages) {
		triggerCustomAction(new jdp_EntityMakeImportantAction(flag, entityID, stages));
	}
	
	public static class jdp_EntityMakeImportantAction implements MissionTrigger.TriggerAction {
		protected String flag;
		protected String entityID;
		protected Enum[] stages;

		public jdp_EntityMakeImportantAction(String flag, String entityID, Enum[] stages) {
			this.flag = flag;
			this.entityID = entityID;
			this.stages = stages;
		}

		public void doAction(MissionTrigger.TriggerActionContext context) {
			context.entity = (SectorEntityToken) Global.getSector().getEntityById(entityID);

			BaseHubMission bhm = (BaseHubMission) context.mission;
			bhm.makeImportant(context.entity, flag, stages);

			if (Arrays.asList(stages).contains(bhm.getCurrentStage())) {
				Misc.makeImportant(context.entity.getMemoryWithoutUpdate(), bhm.getReason());
				bhm.getChanges().add(new MadeImportant(context.entity.getMemoryWithoutUpdate(), bhm.getReason()));

				if (flag != null) {
					context.entity.getMemoryWithoutUpdate().set(flag, true);
					bhm.getChanges().add(new VariableSet(context.entity.getMemoryWithoutUpdate(), flag, true));
				}
			}
		}
	}

	//Descriptions
	@Override
	public void addDescriptionForNonEndStage(TooltipMakerAPI info, float width, float height) {
		float opad = 10f;
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.INVESTIGATE_DORY) {
			info.addPara("An unknown entity has challenged us to a *game*. Something has been deployed in orbit around "
					+ "Hrvoje in the Dory System for us to *dance* with. ", opad);
		} else if (currentStage == Stage.DEFEAT_PENROSE) {
			info.addPara("An unknown entity has challenged us to a *game*. A Penrose-Class Tesseron has been deployed in orbit around "
					+ "Hrvoje in the Dory System for us to *dance* with. "
					+ "The ship is surprisingly docile, or perhaps it is more accurate to say honourable, "
					+ "and will allow us to construct an orbital station to aid in the coming battle.", opad);
		} else if (currentStage == Stage.INVESTIGATE_DERELICT) {
			info.addPara("Salvage the derelict, picking through the iridescent corpse of a Penrose-Class Tesseron.", opad);
		} else if (currentStage == Stage.FIND_ALPHA) {
			info.addPara("Locate and return to the Penrose derelict with an Alpha Level AI Core, hopefully capable of decrypting the files " +
							"recieved via extradimensional commlink.", opad);
		} else if (currentStage == Stage.FIND_MAERULA) {
			info.addPara("Find Maerula, an exceptional human hacker who leads the Flowerfish Mercenary Company. "
							+ "They are highly secretive, but your tactical officer has a connection with a Flowerfish patrol fleet we can leverage "
							+ "to hopefully get a meeting.", opad);
		} else if (currentStage == Stage.RETRIEVE_MAERULA) {
			info.addPara("Ask Maerula on Ailmar to crack the file recieved by extradimensional commlink.", opad);
		} else if (currentStage == Stage.RETRIEVE_GARGOYLE) {
			info.addPara("Ask Gargoyle to crack the file recieved by extradimensional commlink.", opad);
		} else if (currentStage == Stage.RETURN_DATA) {
			info.addPara("Return to the derelict now you have decrypted the Penrose Specifications.", opad);
		}
	}

	//Tooltip
	@Override
	public boolean addNextStepText(TooltipMakerAPI info, Color tc, float pad) {
		Color h = Misc.getHighlightColor();
		if (currentStage == Stage.INVESTIGATE_DORY) {
			info.addPara("Investigate the Dory System", tc, pad);
			return true;
		} else if (currentStage == Stage.DEFEAT_PENROSE) {
			info.addPara("Defeat the Penrose", tc, pad);
			return true;
		} else if (currentStage == Stage.INVESTIGATE_DERELICT) {
			info.addPara("Investigate the derelict Penrose", tc, pad);
			return true;
		} else if (currentStage == Stage.FIND_ALPHA) {
			info.addPara("Return to the Penrose with an Alpha Core", tc, pad);
			return true;
		} else if (currentStage == Stage.FIND_MAERULA) {
			info.addPara("Find Maerula", tc, pad);
			return true;
		} else if (currentStage == Stage.RETRIEVE_MAERULA) {
			info.addPara("Speak to Maerula", tc, pad);
			return true;
		} else if (currentStage == Stage.RETRIEVE_GARGOYLE) {
			info.addPara("Find Gargoyle", tc, pad);
			return true;
		} else if (currentStage == Stage.RETURN_DATA) {
			info.addPara("Return to the Penrose with the specifications.", tc, pad);
			return true;
		}
		return false;
	}

	@Override
	public String getBaseName() {
		return "The Fractal Theorem";
	}

	@Override
	public String getPostfixForState() {
		if (startingStage != null) {
			return "";
		}
		return super.getPostfixForState();
	}
}





