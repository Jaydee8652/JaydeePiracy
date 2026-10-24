package Jaydee8652.JaydeePiracy.campaign.world;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.AsteroidFieldTerrainPlugin;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.CoreLifecyclePluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.WarningBeaconEntityPlugin;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager.RemnantFleetInteractionConfigGen;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import org.magiclib.util.MagicCampaign;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils.shuffleLocation;
import static com.fs.starfarer.api.impl.campaign.world.GateHaulerLocation.ABYSS_AMBIENT_LIGHT_COLOR;

public class jdp_Hiroc {
	public static String JDP_NASCENT_WELL_KEY = "$jdp_hiroc_well";
	public static String JDP_MISSILE_KEY = "$derelict_missile";
	public static String NOT_RANDOM_MISSION_TARGET = "$not_random_mission_target";

	public void generate(SectorAPI sector) {
		StarSystemAPI system = sector.createStarSystem("Unknown Location (Hiroc)");
		system.setOptionalUniqueId("jdp_Hiroc");
		system.setName("Unknown Location"); // to get rid of "Star System" at the end of the name
		system.setType(StarSystemType.DEEP_SPACE);
		system.addTag(Tags.THEME_UNSAFE);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");
		
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

		//Below Galatia Eventually
		system.getLocation().set(-2640, -16750);
		
		HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyperTerrain);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, 200, 0, 360f);

		SectorEntityToken center = system.initNonStarCenter();

		system.setLightColor(ABYSS_AMBIENT_LIGHT_COLOR); // light color in entire system, affects all entities
		center.addTag(Tags.AMBIENT_LS);


		//Convair Gas Giant
		PlanetAPI convair = system.addPlanet("jdp_convair", center, "Convair", "ice_giant", 230, 350, 2000, 250);
		convair.getSpec().setPlanetColor(new Color(108, 105, 227,255));
		system.addRingBand(convair, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 650, 30, Terrain.RING, null);
		convair.setCustomDescriptionId("jdp_convair");
		convair.applySpecChanges();
		convair.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);
		convair.getMarket().addCondition(Conditions.COLD);
		convair.getMarket().addCondition(Conditions.DARK);
		convair.getMarket().addCondition(Conditions.VOLATILES_TRACE);
		convair.getMarket().addCondition(Conditions.HIGH_GRAVITY);
		convair.getMarket().addCondition(Conditions.RUINS_SCATTERED);
		convair.getMarket().getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		convair.setOrbit(null);
		convair.setLocation(1200, 300);

		//Mining Station
		SectorEntityToken blacksite_mining = system.addCustomEntity("blacksite_abandoned_mining",
				"Abandoned Automated Mining Station", // name - if null, defaultName from custom_entities.json will be used
				"station_mining", // type of object, defined in custom_entities.json
				"neutral"); // faction
		blacksite_mining.setCircularOrbitPointingDown(system.getEntityById("jdp_convair"), 245, 650, 50);
		blacksite_mining.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		blacksite_mining.setCustomDescriptionId("jdp_blacksite_mining");

		//Siphon Station
		SectorEntityToken blacksite_siphon = system.addCustomEntity("blacksite_abandoned_siphon",
				"Abandoned Automated Siphon Station", // name - if null, defaultName from custom_entities.json will be used
				"jdp_station_siphon", // type of object, defined in custom_entities.json
				"neutral"); // faction
		blacksite_siphon.setCircularOrbitPointingDown(system.getEntityById("jdp_convair"), 45, 500, 50);
		blacksite_siphon.setCustomDescriptionId("jdp_blacksite_siphon");
		blacksite_siphon.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		blacksite_siphon.setInteractionImage("illustrations", "jdp_station1");

		//Relay
		SectorEntityToken blacksite_relay = system.addCustomEntity("blacksite_relay", // unique id
				"Blacksite Relay", // name - if null, defaultName from custom_entities.json will be used
				"comm_relay", // type of object, defined in custom_entities.json
				"neutral"); // faction
		blacksite_relay.setCircularOrbit(convair, 40 -60, 1475, 41);
		blacksite_relay.setCustomDescriptionId("jdp_blacksite_relay");
		blacksite_relay.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		blacksite_relay.setInteractionImage("illustrations", "jdp_comm_relay");
		blacksite_relay.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);

		//Gate
		SectorEntityToken blacksite_gate = system.addCustomEntity("blacksite_gate", // unique id
				"Blacksite Gate", // name - if null, defaultName from custom_entities.json will be used
				"inactive_gate", // type of object, defined in custom_entities.json
				null); // faction
		blacksite_gate.setCircularOrbit(convair, 210, 2250, 200);
		blacksite_gate.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		blacksite_gate.setCustomDescriptionId("jdp_blacksite_gate");

		//Gate Station
		SectorEntityToken blacksite_gatehouse = system.addCustomEntity("blacksite_gatehouse",
				"Gatehouse Station", // name - if null, defaultName from custom_entities.json will be used
				"orbital_habitat", // type of object, defined in custom_entities.json
				"neutral"); // faction
		blacksite_gatehouse.setCustomDescriptionId("jdp_blacksite_customs");
		blacksite_gatehouse.setInteractionImage("illustrations", "jdp_station2");
		blacksite_gatehouse.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		blacksite_gatehouse.setCircularOrbitPointingDown(blacksite_gate, 270+60, 200, 500);

		//Hiroc Barren World
		PlanetAPI hiroc = system.addPlanet("jdp_hiroc", convair, "Hiroc", "barren-bombarded", 50, 80, 1200, 45);
		hiroc.setCustomDescriptionId("jdp_hiroc");
		hiroc.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "barren03"));
		hiroc.applySpecChanges();
		hiroc.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
		hiroc.getMarket().addCondition(Conditions.LOW_GRAVITY);
		hiroc.getMarket().addCondition(Conditions.COLD);
		hiroc.getMarket().addCondition(Conditions.DARK);
		hiroc.getMarket().addCondition(Conditions.RUINS_VAST);
		hiroc.getMarket().addCondition(Conditions.RARE_ORE_SPARSE);
		hiroc.getMarket().addCondition(Conditions.ORE_SPARSE);
		hiroc.getMarket().addCondition(jdp_Conditions.JDP_CRYOSANCTUM);
		hiroc.getMarket().getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		Misc.setDefenderOverride(hiroc, new DefenderDataOverride("derelict", 1f, 100, 150, 3));


		//Derelict Missile
		SectorEntityToken derelict_missile = system.addCustomEntity("derelict_missile",
				null, // name - if null, defaultName from custom_entities.json will be used
				"jdp_derelictmissile", // type of object, defined in custom_entities.json
				"neutral"); // faction
		derelict_missile.setCircularOrbit(hiroc, 210, 200, 200);
		derelict_missile.getMemoryWithoutUpdate().set(JDP_MISSILE_KEY, true);
		Misc.setDefenderOverride(derelict_missile, new DefenderDataOverride("derelict", 1f, 150, 150, 3));


		//Sensor
		SectorEntityToken blacksite_sensor = system.addCustomEntity("blacksite_sensor",
				"Blacksite Sensor Array", // name - if null, defaultName from custom_entities.json will be used
				"sensor_array", // type of object, defined in custom_entities.json
				"neutral"); // faction
		blacksite_sensor.setCircularOrbit(hiroc, 310, 200, 200);
		blacksite_sensor.setCustomDescriptionId("jdp_blacksite_sensor");
		blacksite_sensor.getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		blacksite_sensor.setInteractionImage("illustrations", "jdp_sensor_array");

		//Phase Distortion
		SectorEntityToken field = system.addTerrain(Terrain.MAGNETIC_FIELD,
						new MagneticFieldParams(150f, // terrain effect band width 
						140, // terrain effect middle radius
						derelict_missile, // entity that it's around
						80f, // visual band start
						180f, // visual band end
						new Color(140, 60, 150, 89), // base color
						1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
						new Color(130, 60, 150, 130),
						new Color(150, 30, 120, 150), 
						new Color(200, 50, 130, 190),
						new Color(250, 70, 150, 240),
						new Color(200, 80, 130, 255),
						new Color(75, 0, 160, 255), 
						new Color(127, 0, 255, 255)));


		field.setCircularOrbit(derelict_missile, 0, 0, 75);

		system.generateAnchorIfNeeded();
		
		NascentGravityWellAPI well = Global.getSector().createNascentGravityWell(convair, 50f);
		well.addTag(Tags.NO_ENTITY_TOOLTIP);
		well.setColorOverride(new Color(125, 50, 255));
		hyper.addEntity(well);

		shuffleLocation(system, false, -6000f, 0f, -16000f, -20000f);

		MagicCampaign.hyperspaceCleanup(system);
		system.updateAllOrbits();

		well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(convair, 0);
		Global.getSector().getMemoryWithoutUpdate().set(JDP_NASCENT_WELL_KEY, well);
	}
	
	public static NascentGravityWellAPI getWell() {
		return (NascentGravityWellAPI) Global.getSector().getMemoryWithoutUpdate().get(JDP_NASCENT_WELL_KEY);
	}
	
}













