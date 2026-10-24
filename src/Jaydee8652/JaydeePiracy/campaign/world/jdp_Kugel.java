package Jaydee8652.JaydeePiracy.campaign.world;

import java.awt.Color;
import java.util.*;

import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.*;
import com.fs.starfarer.api.impl.campaign.enc.AbyssalLocationDespawner;
import com.fs.starfarer.api.impl.campaign.enc.AbyssalRogueStellarObjectEPEC;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator;
import com.fs.starfarer.api.impl.campaign.shared.WormholeManager;
import com.fs.starfarer.api.impl.campaign.terrain.*;
import com.fs.starfarer.api.impl.campaign.world.GateHaulerLocation;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.loading.specs.PlanetSpec;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager.RemnantFleetInteractionConfigGen;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipRecoverySpecialData;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import org.magiclib.util.MagicCampaign;


import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils.*;
import static com.fs.starfarer.api.impl.campaign.world.GateHaulerLocation.ABYSS_AMBIENT_LIGHT_COLOR;

public class jdp_Kugel {
	public static String NOT_RANDOM_MISSION_TARGET = "$not_random_mission_target";
	protected Random random = new Random();


	public void generate() {
		StarAge age = StarAge.OLD;
		SectorAPI sector = Global.getSector();

		StarSystemAPI system = sector.createStarSystem("Deep Storage");
		system.setProcgen(false);
		system.setName("Deep Storage"); // to get rid of "Star System" at the end of the name
		system.setOptionalUniqueId("jdp_deep_storage");
		system.setType(StarSystemType.DEEP_SPACE);
		system.addTag(Tags.THEME_HIDDEN);
		system.addTag(Tags.THEME_SPECIAL);
		system.addTag(Tags.THEME_UNSAFE);
		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

		StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, StarTypes.BLACK_HOLE, false);
		system.getLocation().set(50000f,50000f);
		SectorEntityToken center = system.initNonStarCenter();
		system.setLightColor(starData.getLightColorMin());
		center.addTag(Tags.AMBIENT_LS);

		StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(age);
		StarSystemGenerator.random = random;

		StarSystemGenerator gen = new StarSystemGenerator(params);
		gen.init(system,age);

		StarSystemGenerator.GenContext context = new StarSystemGenerator.GenContext(gen, system, system.getCenter(), starData, null, 0, age.name(), 0, 1000, null, -1);
		// just so it doesn't try to add things at the planet's lagrange points
		context.lagrangeParent = new StarSystemGenerator.GeneratedPlanet(null, null, false, 0, 0, 0);
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB5);
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB4);
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB3);
		context.excludeCategories.add(StarSystemGenerator.CAT_HAB2);

		PlanetAPI kugel = system.addPlanet("jdp_kugel", center, "Kugel", Planets.BARREN, 230, 75, 0, 0);
		kugel.getSpec().setPlanetColor(new Color(108, 105, 227,255));
		kugel.setCustomDescriptionId("jdp_kugel");
		kugel.applySpecChanges();
		kugel.getMarket().addCondition(Conditions.ORE_ULTRARICH);
		kugel.getMarket().addCondition(Conditions.THIN_ATMOSPHERE);
		kugel.getMarket().addCondition(Conditions.COLD);
		kugel.getMarket().addCondition(Conditions.LOW_GRAVITY);
		kugel.getMarket().addCondition(Conditions.RUINS_VAST);
		kugel.getMarket().getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		kugel.setOrbit(null);

		addStar(system, "jdp_alexandria",kugel,"Alexandria", StarTypes.BLACK_HOLE, 0, starData.getMinRadius(), 500, 100);
		addStar(system, "jdp_longyearbyen",kugel,"Longyearbyen", StarTypes.BLACK_HOLE, 180, starData.getMinRadius(), 500, 100);

		// add accretion disk
		Collection<TerrainGenDataSpec> terrainDataSpecs = Global.getSettings().getAllSpecs(TerrainGenDataSpec.class);
		TerrainGenDataSpec terrainData = null;
		for (TerrainGenDataSpec curr : terrainDataSpecs) {
			if (curr.getId().equals(Tags.ACCRETION_DISK)) {
				terrainData = curr;
				break;
			}
		}

		float bandWidth = 256f;
		float numBands = 12;
		float orbitRadius = 2500f;

		for (float i = 0; i < numBands; i++) {
			float radius = orbitRadius - i * bandWidth * 0.25f - i * bandWidth * 0.1f;

			AccretionDiskGenPlugin.TexAndIndex result = new AccretionDiskGenPlugin.TexAndIndex();
			WeightedRandomPicker<Integer> indexPicker = new WeightedRandomPicker<Integer>(StarSystemGenerator.random);

			WeightedRandomPicker<String> ringSet = new WeightedRandomPicker<String>(StarSystemGenerator.random);
			ringSet.add("ring_ice", 10f);
			ringSet.add("ring_dust", 10f);

			String set = ringSet.pick();

			if (set.equals("ring_ice")) {
				result.tex = "rings_ice0";
				indexPicker.add(0);
				indexPicker.add(1);
			} else if (set.equals("ring_dust")) {
				result.tex = "rings_dust0";
				indexPicker.add(0);
				indexPicker.add(1);
			}

			result.index = indexPicker.pick();

			AccretionDiskGenPlugin.TexAndIndex tex = result;
			float orbitDays = radius / (30f + 10f * StarSystemGenerator.random.nextFloat());
			Color color = StarSystemGenerator.getColor(terrainData.getMinColor(), terrainData.getMaxColor());
			RingBandAPI visual = system.addRingBand(kugel, "misc", tex.tex, 256f, tex.index, color, bandWidth,
					radius + bandWidth / 2f, -orbitDays);

			visual.setSpiral(true);
			visual.setMinSpiralRadius(0);
			visual.setSpiralFactor(2f + StarSystemGenerator.random.nextFloat() * 5f);
		}


		List<SectorEntityToken> rings = new ArrayList<SectorEntityToken>();
		SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(orbitRadius, orbitRadius * 0.5f, kugel, null));
		ring.addTag(Tags.ACCRETION_DISK);
		if (((CampaignTerrainAPI) ring).getPlugin() instanceof RingSystemTerrainPlugin) ((RingSystemTerrainPlugin) ((CampaignTerrainAPI) ring).getPlugin()).setNameForTooltip("Accretion Disk");


		ring.setCircularOrbit(kugel, 0, 0, -100);
		rings.add(ring);

		system.autogenerateHyperspaceJumpPoints();

		orbitRadius += 500;
		float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
		SectorEntityToken relay = system.addCustomEntity("jdp_kugelRelay", // unique id
				null, // name - if null, defaultName from custom_entities.json will be used
				"comm_relay", // type of object, defined in custom_entities.json
				Factions.DERELICT); // faction
		relay.setCircularOrbit(system.getCenter(), 90, orbitRadius, orbitDays);
		relay.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);

		orbitRadius -= 200;
		BaseThemeGenerator.EntityLocation loc = new BaseThemeGenerator.EntityLocation();
		loc.orbit = Global.getFactory().createCircularOrbit(kugel,
				StarSystemGenerator.random.nextFloat() * 360f, orbitRadius, orbitDays);
		BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.STABLE_LOCATION, Factions.NEUTRAL);

		var wormholeLoc = system.getEntitiesWithTag(Tags.STABLE_LOCATION).get(0);
		var itemData = new WormholeManager.WormholeItemData("jdp_KugelWormhole", "jdp_libra", "Libra");
		var item = new SpecialItemData(Items.WORMHOLE_ANCHOR, itemData.toJsonStr());
		var wormhole = WormholeManager.get().addWormhole(item, wormholeLoc, null);
		wormhole.getMemoryWithoutUpdate().unset(JumpPointInteractionDialogPluginImpl.UNSTABLE_KEY);

		hideSystem(system);
	}
}













