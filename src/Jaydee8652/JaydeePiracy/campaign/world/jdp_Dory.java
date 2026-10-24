package Jaydee8652.JaydeePiracy.campaign.world;

import java.awt.Color;

import Jaydee8652.JaydeePiracy.scripts.jdp_DemonLeashAssignmentAI;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeePiracy.utils.jdp_Factions;
import Jaydee8652.JaydeePiracy.utils.jdp_Skills;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.BaseTiledTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import Jaydee8652.JaydeePiracy.utils.jdp_Ranks;
import org.magiclib.util.MagicCampaign;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils.shuffleLocation;

public class jdp_Dory {
	public static String NOT_RANDOM_MISSION_TARGET = "$not_random_mission_target";
	public static String JDP_PENROSE_DERELICT_KEY = "$jdp_penrosetag";


	//Makes the Penrose interaction fire
	public static class jdp_PenroseFIDConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {
		public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
			FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

			config.showTransponderStatus = false;
			config.showEngageText = false;
			config.alwaysLetGo = true;
			config.dismissOnLeave = false;
			config.withSalvage = false;
			config.printXPToDialog = true;

			config.noSalvageLeaveOptionText = "Continue";

			config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
					new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.postPlayerSalvageGeneration(dialog, context, salvage);
				}

				public void notifyLeave(InteractionDialogAPI dialog) {
					SectorEntityToken other = dialog.getInteractionTarget();
					if (!(other instanceof CampaignFleetAPI)) {
						dialog.dismiss();
						return;
					}
					CampaignFleetAPI fleet = (CampaignFleetAPI) other;

					if (!fleet.isEmpty()) {
						dialog.dismiss();
						return;
					}

					ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData("jdp_penrose_Omega", ShipRecoverySpecial.ShipCondition.BATTERED, 0f);
					ship.shipName = "Sa-Matra";
					DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);
					CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(fleet.getContainingLocation(), Entities.WRECK, Factions.OMEGA, params);

					entity.getMemoryWithoutUpdate().set("$jdp_penrosetag", true);
					entity.setId("jdp_penrose_derelict");

					// Make it spawn in an orbit around Hrjove.
					// Feel free to steal this code for whatever you want!
					SectorEntityToken hrvoje = Global.getSector().getEntityById("jdp_hrvoje");

					float xchange = fleet.getLocation().x - hrvoje.getLocation().x;
					float ychange = fleet.getLocation().y - hrvoje.getLocation().y;
					float distance = (float) Math.sqrt((xchange * xchange) + (ychange * ychange));
					float angle = (float) Math.toDegrees(Math.atan2(ychange, xchange));
					entity.setCircularOrbit(hrvoje, angle, distance, 40);

					ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);

					data.notNowOptionExits = true;
					data.noDescriptionText = true;

					DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
					ShipRecoverySpecial.PerShipData copy = dsep.getData().ship.clone();

					copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
					copy.variantId = null;

					copy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
					copy.variant.addTag(Tags.SHIP_UNIQUE_SIGNATURE);
					data.addShip(copy);

					Misc.setSalvageSpecial(entity, data);

					dialog.setInteractionTarget(entity);
					RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("jdp_AfterPenroseDefeat");
					dialog.setPlugin(plugin);
					plugin.init(dialog);
				}

				public void battleContextCreated(InteractionDialogAPI dialog, BattleCreationContext bcc) {
					bcc.aiRetreatAllowed = false;
					bcc.objectivesAllowed = false;
					bcc.fightToTheLast = true;
					bcc.enemyDeployAll = true;
				}
			};
			return config;
		}
	}

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Dory");
		system.setOptionalUniqueId("jdp_Dory");
		system.setType(StarSystemGenerator.StarSystemType.SINGLE);
		system.addTag(Tags.THEME_SPECIAL);
		LocationAPI hyper = Global.getSector().getHyperspace();

		system.setBackgroundTextureFilename("graphics/backgrounds/background2.jpg");

		//In the corner
		system.getLocation().set(74834, 44166);

		HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyperTerrain);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, 500, 0, 360f);

		PlanetAPI jdp_dorystar = system.initStar("jdp_Dory", // unique id for this star
				"star_yellow",  // id in planets.json
				700f, 		  // radius (in pixels at default zoom)
				500); // corona radius, from star edge

		system.setLightColor(new Color(255, 220, 190)); // light color in entire system, affects all entities

		//Lukatela
		PlanetAPI lukatela = system.addPlanet("jdp_lukatela", jdp_dorystar, "Lukatela", "lava", 30, 90, 1600, 90);
		lukatela.getMarket().addCondition(jdp_Conditions.JDP_DEPLETEDATMOSPHERE);
		lukatela.getMarket().addCondition(Conditions.VERY_HOT);
		lukatela.getMarket().addCondition(Conditions.ORE_ABUNDANT);
		lukatela.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
		lukatela.getMarket().getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);

		//Asteroid Belt
		system.addRingBand(jdp_dorystar, "misc", "rings_dust0", 256f, 0, Color.white, 256f, 3100, 70f, null, null);
		system.addAsteroidBelt(jdp_dorystar, 150, 3100, 128, 60, 80, Terrain.ASTEROID_BELT, "The Scattering");

		//Hrvoje
		PlanetAPI hrvoje = system.addPlanet("jdp_hrvoje", jdp_dorystar, "Hrvoje", "terran-eccentric", 60, 180, 4200, 300);
		hrvoje.getMarket().addCondition(Conditions.HABITABLE);
		hrvoje.getMarket().addCondition(Conditions.MILD_CLIMATE);
		hrvoje.getMarket().addCondition(Conditions.ORGANICS_ABUNDANT);
		hrvoje.getMarket().addCondition(Conditions.FARMLAND_BOUNTIFUL);
		hrvoje.getMarket().addCondition(Conditions.ORE_MODERATE);
		hrvoje.getMarket().addCondition(Conditions.RUINS_WIDESPREAD);
		hrvoje.getMarket().getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		system.addRingBand(hrvoje, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 500, 33f, Terrain.RING, null);
		hrvoje.getMemoryWithoutUpdate().set("$jdp_hrvojetag", true);

		//Jump Point
		JumpPointAPI hrvoje_jump_point = Global.getFactory().createJumpPoint("jdp_hrvoje_jump_point", "Hrvoje Inner System Jump-point");
		OrbitAPI orbit = Global.getFactory().createCircularOrbit(jdp_dorystar, 0, 4200, 300);
		hrvoje_jump_point.setOrbit(orbit);
		hrvoje_jump_point.setRelatedPlanet(hrvoje);
		hrvoje_jump_point.setStandardWormholeToHyperspaceVisual();
		system.addEntity(hrvoje_jump_point);

		// Dory Gate
		SectorEntityToken gate = system.addCustomEntity("jdp_dory_gate", // unique id
				"Dory Gate", // name - if null, defaultName from custom_entities.json will be used
				"inactive_gate", // type of object, defined in custom_entities.json
				null); // faction
		gate.setCircularOrbit(system.getEntityById("jdp_Dory"), 120, 4200, 300);

		// L4 & L5 mini-nebulas
		SectorEntityToken tlalocan_L4_nebula = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
				"  x   " +
						"  xx x" +
						"xxxxx " +
						" xxx  " +
						" x  x " +
						"   x  ",
				6, 6, // size of the nebula grid, should match above string
				"terrain", "nebula", 4, 4, null));

		SectorEntityToken tlalocan_L5_nebula = system.addTerrain(Terrain.NEBULA, new BaseTiledTerrain.TileParams(
				"  x   " +
						" xx xx" +
						"x  xx " +
						" xxxx " +
						" x x x" +
						"  x   ",
				6, 6, // size of the nebula grid, should match above string
				"terrain", "nebula", 4, 4, null));

		tlalocan_L5_nebula.setCircularOrbit(jdp_dorystar, 130 - 60, 7500, 500);
		tlalocan_L4_nebula.setCircularOrbit(jdp_dorystar, 130 + 60, 7500, 500);

		//Stable Location
		SectorEntityToken stable_location = system.addCustomEntity(null, null, "stable_location", Factions.NEUTRAL);
		stable_location.setCircularOrbitPointingDown(jdp_dorystar, 130 + 180 , 7500, 500);

		system.autogenerateHyperspaceJumpPoints(true, false);

		shuffleLocation(system, false, 72000f, 78000f, 42000f, 48000f);

		MagicCampaign.hyperspaceCleanup(system);
		system.updateAllOrbits();
	}

	// Add Penrose
	public static void addPenroseFleet() {
		SectorEntityToken hrvoje = Global.getSector().getEntityById("jdp_hrvoje");

		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.OMEGA, FleetTypes.PATROL_LARGE, null);
		fleet.setName("Hyperwave Inducing *Game*");
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, false);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, false);
		fleet.getMemoryWithoutUpdate().set("$clearCommands_no_remove", true);
		fleet.getMemoryWithoutUpdate().set("$jdp_penrose", true);
		fleet.setId("jdp_penrosefleet");

		fleet.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);

		PersonAPI person = createOmegaCaptain();
		fleet.setCommander(person);

		//Sa-Matra
		FleetMemberAPI samatra = fleet.getFleetData().addFleetMember("jdp_penrose_Omega");
		//FleetMemberAPI samatra = fleet.getFleetData().addFleetMember("wayfarer_Standard");//For Testing Purposes
		samatra.setCaptain(person);
		samatra.setShipName("Sa-Matra");
		samatra.updateStats();
		samatra.getRepairTracker().setCR(samatra.getRepairTracker().getMaxCR());
		samatra.setVariant(samatra.getVariant().clone(), false, false);
		samatra.getVariant().setSource(VariantSource.REFIT);
		samatra.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

		fleet.getFleetData().ensureHasFlagship();
		fleet.clearAbilities();
		fleet.setTransponderOn(true);

		Vector2f loc = new Vector2f(hrvoje.getLocation().x + 300 * ((float) Math.random() - 0.5f),
				hrvoje.getLocation().y + 300 * ((float) Math.random() - 0.5f));
		fleet.setLocation(loc.x, loc.y);
		hrvoje.getContainingLocation().addEntity(fleet);

		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
				new jdp_Dory.jdp_PenroseFIDConfig());

		fleet.addScript(new jdp_DemonLeashAssignmentAI(fleet, hrvoje));
	}

	//Omega Captain
	public static PersonAPI createOmegaCaptain() {
		PersonAPI person = Global.getFactory().createPerson();
		person.setName(new FullName("*Finger*", "Prismata", FullName.Gender.ANY));
		person.setFaction(Factions.OMEGA);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "jdp_omega"));
		person.setPersonality(Personalities.RECKLESS);
		person.setRankId(jdp_Ranks.JDP_DANCER);
		person.setPostId(jdp_Ranks.POST_JDP_GAMEMASTER);

		person.getStats().setSkipRefresh(true);

		person.getStats().setLevel(14);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2);
		person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);

		person.getStats().setSkillLevel(jdp_Skills.JDP_DEEPEUTECTICGRID, 1);
		person.getStats().setSkillLevel(jdp_Skills.JDP_PERFECTPREDICTION, 1);
		person.getStats().setSkillLevel(jdp_Skills.JDP_MATERIELSUBSUMPTION, 1);
		person.getStats().setSkillLevel(jdp_Skills.JDP_DANCING, 1);
		person.getStats().setSkillLevel(jdp_Skills.JDP_APPLIEDCONTRAVENTION, 1);

		person.getStats().setSkillLevel(Skills.OMEGA_ECM, 1);

		person.getStats().setSkillLevel(Skills.NAVIGATION, 1);
		person.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
		person.getStats().setSkillLevel(Skills.FLUX_REGULATION, 1);
		person.getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 1);
		person.getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 1);
		person.getStats().setSkillLevel(Skills.TACTICAL_DRILLS, 1);
		person.getStats().setSkillLevel(Skills.CREW_TRAINING, 1);

		person.getStats().setSkipRefresh(false);
		return person;
	}
}