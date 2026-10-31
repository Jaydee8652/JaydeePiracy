package Jaydee8652.JaydeePiracy.campaign.world;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_ColonyFlicker;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils;
import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IsolatedEconomyListener;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.themes.*;

import Jaydee8652.JaydeePiracy.plugins.jdp_lunaSettings;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeePiracy.utils.jdp_Factions;
import Jaydee8652.JaydeePiracy.utils.jdp_Planets;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.ThemeGenContext;
import com.fs.starfarer.api.impl.campaign.procgen.themes.Themes;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.world.TTBlackSite;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicCampaign;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_ConditionUtils.*;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils.*;
import static com.fs.starfarer.api.impl.campaign.ids.Tags.NOT_RANDOM_MISSION_TARGET;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator.makeDiscoverable;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.addRemnantStationInteractionConfig;

public class jdp_MiscEventGenerator extends BaseThemeGenerator {
	public static Logger log = Global.getLogger(JaydeePiracyPlugin.class);

	public String getThemeId() {
		return Themes.MISC;
	}

	@Override
	public float getWeight() {
		return 0f;
	}

	@Override
	public void generateForSector(ThemeGenContext context, float allowedSectorFraction) {
		if (jdp_lunaSettings.jdp_campaignColonyItemsGlobal().equals(true)) {
			if (jdp_lunaSettings.jdp_campaignDynamo().equals(true)) {
				addPlasmaDynamo();
			}
			if (jdp_lunaSettings.jdp_campaignBiofactory().equals(true)) {
				addBiofactoryEmbryo();
			}
			if (jdp_lunaSettings.jdp_campaignSoil().equals(true)) {
				addSoilNanites();
			}
			if (jdp_lunaSettings.jdp_campaignLamp().equals(true)) {
				addOrbitalFusionLamp();
			}
			if (jdp_lunaSettings.jdp_campaignBore().equals(true)) {
				addAutonomousMantleBore();
			}
			if (jdp_lunaSettings.jdp_campaignFullerene().equals(true)) {
				addFullereneSpool();
			}
			if (jdp_lunaSettings.jdp_campaignReplicator().equals(true)) {
				addCombatDroneReplicator();
			}
			if (jdp_lunaSettings.jdp_campaignCryo().equals(true)) {
				addCryoarithmeticEngine();
			}
			if (jdp_lunaSettings.jdp_campaignHolosuite().equals(true)) {
				addDealmakerHolosuite();
			}
		}

		if (jdp_lunaSettings.jdp_campaignMadoka().equals(true)) addMadokaBuffalo();
		if (jdp_lunaSettings.jdp_campaignSubsurface().equals(true)) addSubsurfaceEcosystems();
		if (!Global.getSettings().getModManager().isModEnabled("IndEvo")) addKantaMinefield();
	}

	@Override
	public int getOrder() {
		return 1000000;
	}

	public void addMadokaBuffalo(){
		String source = "Madoka Buffalo Easter Egg";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_madokaBuffaloKey")) {
			List<StarSystemAPI> validStars = new ArrayList<>(Global.getSector().getStarSystems().stream().filter(s -> s.isProcgen() && !s.getPlanets().isEmpty()).toList());

			Collections.shuffle(validStars);
			StarSystemAPI targetstar = validStars.get(0);

			Global.getSector().getMemoryWithoutUpdate().set("$jdp_madokaBuffaloKey", targetstar);

			SectorEntityToken entity = targetstar.getPlanets().get(0);
			TTBlackSite.addDerelict(targetstar, entity, "jdp_buffalo_pmmm_Standard", "Wehihihi", "jdp_buffalo_pmmm", ShipRecoverySpecial.ShipCondition.BATTERED, entity.getRadius() + 300, true);
			log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + targetstar.getName() + "]");
		} else {
		log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	public void addKantaMinefield(){
		String source = "Kanta Minefield Visual";
		log.info("JDP_RETROGEN_EVENTS: Looking for Kanta's Den to apply " + source + " to.");

		//If it was already applied, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_kantaMinefieldKey")) {
			SectorEntityToken den = Global.getSector().getEconomy().getMarket("kantas_den").getPrimaryEntity();
			if (den != null) {
				//Minefield
				float radius = den.getRadius() + 20;
				float minOrbitDays = radius / 5;
				float maxOrbitDays = minOrbitDays + 5f;

				den.getContainingLocation().addOrbitalJunk(den,
						"jdp_orbital_mines", // from custom_entities.json
						50, // num of junk
						12, 20, // min/max sprite size (assumes square)
						radius, // orbit radius
						40, // orbit width
						minOrbitDays, // min orbit days
						maxOrbitDays, // max orbit days
						20f, // min spin (degress/day)
						25f); // max spin (degrees/day)
				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + den.getName() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Kanta's Den not found. Failed to add " + source);
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}


	protected void addSubsurfaceEcosystems() {
		String source = "Subsurface Ecosystems";
		log.info("JDP_RETROGEN_EVENTS: Looking for systems to hide " + source + " in.");

		WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_subsurfaceEcosystemsKey")) {
			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;

					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;
					if (curr.hasCondition(Conditions.HABITABLE)) continue;

					PlanetGenDataSpec spec = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, curr.getSpec().getPlanetType(), true);
					if (spec == null) {
						Global.getLogger(this.getClass()).warn("JDP_RETROGEN_EVENTS: 		[WARNING] " + source + " encountered missing PlanetGenDataSpec on [" + curr.getName() + "] [" + curr.getId() + "] in [" + curr.getStarSystem().getNameWithLowercaseType() + "] [" + curr.getStarSystem().getId() + "] for type [" + curr.getSpec().getPlanetType() + "]");
						continue;
					}

					float w = 1f;
					if (spec.getCategory().equals("cat_frozen")) {w *= 50f;}
					if (curr.getStarSystem().hasPulsar()) {w *= 25f;}
					if (curr.getCircularOrbitRadius() >= 5000) {w *= 25f;}

					picker.add(curr, w);
				}
			}

			int numEcosystems = 5 + random.nextInt(2);
			int added = 0;
			List<PlanetAPI> subsurfaceecosystems = new ArrayList<>();

			while (added < numEcosystems) {
				if (picker.isEmpty()) {
					added++;
					log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + " number " + added + ".");
				}

				PlanetAPI planet = picker.pickAndRemove();
				if (planet != null) {
					if (!planet.getTypeId().equals(Planets.FROZEN)) {
						planet.changeType(Planets.FROZEN, random);
						planet.getMarket().getConditions().clear();
						StarAge age = planet.getStarSystem().getAge();
						if (age == null) {
							log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] System with null age found when adding " + source + "  to [" + planet.getName() + "] [" + planet.getId() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
						} else {
							PlanetConditionGenerator.generateConditionsForPlanet(null, planet, age);
						}
					}
					planet.getMarket().addCondition(jdp_Conditions.JDP_SUBSHEETECOSYSTEM);

					long seed = StarSystemGenerator.random.nextLong();
					added++;
					subsurfaceecosystems.add(planet);
					log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " number " + added + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
				}
			}
			Global.getSector().getMemoryWithoutUpdate().set("$jdp_subsurfaceEcosystemsKey", subsurfaceecosystems);

			log.info("JDP_RETROGEN_EVENTS: 		Finished adding " + source + " total number " + added);
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exist. No action taken");
		}
	}



	protected void addBiofactoryEmbryo() {
		String source = "Infested World (Biofactory Embryo)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_biofactoryEmbryoKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;

				if (system.hasPulsar()) continue;
				if (system.isNebula()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//Not Star, Giant or Moon
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;

					if (curr.hasCondition(Conditions.IRRADIATED)) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;

					if (curr.getRadius() <= 150) continue;

					if (curr.getOrbitFocus() != null) {
						if (curr.getCircularOrbitRadius() > curr.getOrbitFocus().getRadius() + 6000) continue;
						if (curr.getCircularOrbitRadius() < curr.getOrbitFocus().getRadius() + 2000) continue;
					}

					picker.add(curr);
				}
			}

			if (picker.isEmpty()) {
				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params = normaliseParams(params);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 800);
				float orbitRadius = 4000;

				if (!gaps.isEmpty()) {
					orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
				}

				float radius = 100f + random.nextFloat() * 50f;
				float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);

				String name = (ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, system.getCenter().getName(), null).nameWithRomanSuffixIfAny);
				PlanetAPI ideal = system.addPlanet("jdp_biofactoryEmbryo", system.getCenter(), name, Planets.BARREN, Math.round(Math.random() * 360), radius, orbitRadius, orbitDays);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());

				system.updateAllOrbits();

				shuffleLocation(system, true, null, null, null, null);
				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				List<CampaignTerrainAPI> terrain = planet.getStarSystem().getTerrainCopy().stream().filter(t -> t.getType().equals(Terrain.MAGNETIC_FIELD) && t.getOrbitFocus().equals(planet)).toList();
				if (!terrain.isEmpty()) {
					terrain.get(0).setExpired(true);
				}

				Global.getSector().getMemoryWithoutUpdate().set("$jdp_biofactoryEmbryoKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_biofactoryEmbryo", true);
				planet.getMemoryWithoutUpdate().set("$jdp_biofactoryEmbryoBlockFirstSurvey", true);

				Boolean solarShades = false;
				if (planet.hasCondition(Conditions.SOLAR_ARRAY)) solarShades = true;

				planet.changeType(jdp_Planets.JDP_INFESTED, random);
				planet.getMarket().getConditions().clear();
				PlanetConditionGenerator.generateConditionsForPlanet(null, planet, planet.getStarSystem().getAge());

				planet.getMarket().removeCondition(Conditions.DECIVILIZED);
				planet.getMarket().removeCondition(Conditions.DECIVILIZED_SUBPOP);

				planet.getMarket().removeCondition(Conditions.RUINS_SCATTERED);
				planet.getMarket().removeCondition(Conditions.RUINS_WIDESPREAD);
				planet.getMarket().removeCondition(Conditions.RUINS_EXTENSIVE);
				planet.getMarket().removeCondition(Conditions.RUINS_VAST);

				planet.getMarket().removeCondition(Conditions.INIMICAL_BIOSPHERE);
				planet.getMarket().removeCondition(Conditions.MILD_CLIMATE);

				planet.getMarket().removeCondition(Conditions.FARMLAND_POOR);
				planet.getMarket().removeCondition(Conditions.FARMLAND_ADEQUATE);
				planet.getMarket().removeCondition(Conditions.FARMLAND_RICH);
				planet.getMarket().removeCondition(Conditions.FARMLAND_BOUNTIFUL);

				planet.getMarket().addCondition(Conditions.FARMLAND_RICH);
				planet.getMarket().addCondition(Conditions.RUINS_EXTENSIVE);
				planet.getMarket().addCondition(Conditions.POLLUTION);
				planet.getMarket().addCondition(jdp_Conditions.JDP_ENCROACHINGBIOFACTORY);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				if (solarShades == true) planet.getMarket().addCondition(Conditions.SOLAR_ARRAY);


				long seed = StarSystemGenerator.random.nextLong();
				planet.addTag(NOT_RANDOM_MISSION_TARGET);
				planet.getStarSystem().addTag(Tags.THEME_SPECIAL);

				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addSoilNanites() {
		String source = "Eutrophicated World (Soil Nanites)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_soilNanitesKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;

				if (system.hasPulsar()) continue;
				if (system.isNebula()) continue;
				if (system.hasBlackHole()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//Not Star, Giant or Moon
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;

					if (curr.hasCondition(Conditions.IRRADIATED)) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;

					if (curr.getRadius() < 150) continue;

					if (curr.getOrbitFocus() != null) {
						if (curr.getCircularOrbitRadius() > curr.getOrbitFocus().getRadius() + 6000) continue;
						if (curr.getCircularOrbitRadius() < curr.getOrbitFocus().getRadius() + 2000) continue;
					}
					picker.add(curr);
				}
			}

			if (picker.isEmpty()) {				
				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params = normaliseParams(params);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 800);
				float orbitRadius = 4000;
				if (!gaps.isEmpty()) {
					orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
				}

				float radius = 100f + random.nextFloat() * 50f;
				float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);

				String name = (ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, system.getCenter().getName(), null).nameWithRomanSuffixIfAny);
				PlanetAPI ideal = system.addPlanet("jdp_soilNanites", system.getCenter(), name, Planets.BARREN, Math.round(Math.random() * 360), radius, orbitRadius, orbitDays);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());

				system.updateAllOrbits();

				shuffleLocation(system, true, null, null, null, null);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				List<CampaignTerrainAPI> terrain = planet.getStarSystem().getTerrainCopy().stream().filter(t -> t.getType().equals(Terrain.MAGNETIC_FIELD) && t.getOrbitFocus().equals(planet)).toList();
				if (!terrain.isEmpty()) {
					terrain.get(0).setExpired(true);
				}

				Global.getSector().getMemoryWithoutUpdate().set("$jdp_soilNanitesKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_soilNanites", true);
				planet.getMemoryWithoutUpdate().set("$jdp_soilNanitesBlockFirstSurvey", true);

				planet.changeType(jdp_Planets.JDP_EUTROPHICATED, random);
				planet.getMarket().getConditions().clear();
				PlanetConditionGenerator.generateConditionsForPlanet(null, planet, planet.getStarSystem().getAge());

				planet.getMarket().removeCondition(Conditions.DECIVILIZED);
				planet.getMarket().removeCondition(Conditions.DECIVILIZED_SUBPOP);

				removeConditions(planet.getMarket(), RUINS);
				removeConditions(planet.getMarket(), FARMLAND);
				removeConditions(planet.getMarket(), VOLATILES);
				removeConditions(planet.getMarket(), RARE_ORES);
				removeConditions(planet.getMarket(), DECIVILIZED);

				planet.getMarket().removeCondition(Conditions.INIMICAL_BIOSPHERE);
				planet.getMarket().removeCondition(Conditions.MILD_CLIMATE);

				planet.getMarket().addCondition(Conditions.FARMLAND_BOUNTIFUL);
				planet.getMarket().addCondition(Conditions.RUINS_WIDESPREAD);
				planet.getMarket().addCondition(Conditions.INIMICAL_BIOSPHERE);
				planet.getMarket().addCondition(Conditions.SOLAR_ARRAY);

				planet.getMarket().addCondition(jdp_Conditions.JDP_EUTROPHICATEDBIOSPHERE);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				//Mirrors and shades
				boolean shade =
						planet.hasCondition(Conditions.VERY_HOT) ||
						planet.hasCondition(Conditions.HOT) ||
						planet.getStarSystem().getStar().getTypeId().equals(StarTypes.BLUE_GIANT) ||
						planet.getStarSystem().getStar().getTypeId().equals(StarTypes.BLUE_SUPERGIANT);
				boolean mirror =
						planet.hasCondition(Conditions.POOR_LIGHT) ||
						planet.getStarSystem().getStar().getTypeId().equals(StarTypes.RED_DWARF) ||
						planet.getStarSystem().getStar().getTypeId().equals(StarTypes.BROWN_DWARF);

				boolean forceFew = false;
				if (!shade && !mirror) {
					mirror = true;
					shade = true;
					forceFew = true;
				}

				String faction = Factions.NEUTRAL;
				float period = planet.getCircularOrbitPeriod();
				float angle = planet.getCircularOrbitAngle();
				float radius = 270f + planet.getRadius();

				float xp = 300f;
				float profile = 2000f;

				if (mirror) {
					SectorEntityToken mirror2 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Beta", Entities.STELLAR_MIRROR, faction);
					SectorEntityToken mirror3 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Gamma", Entities.STELLAR_MIRROR, faction);
					SectorEntityToken mirror4 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Delta", Entities.STELLAR_MIRROR, faction);
					mirror2.setCircularOrbitPointingDown(planet, angle - 30, radius, period);
					mirror3.setCircularOrbitPointingDown(planet, angle + 0, radius, period);
					mirror4.setCircularOrbitPointingDown(planet, angle + 30, radius, period);
					makeDiscoverable(mirror2, xp, profile);
					makeDiscoverable(mirror3, xp, profile);
					makeDiscoverable(mirror4, xp, profile);

					if (!forceFew) {
						SectorEntityToken mirror1 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Alpha", Entities.STELLAR_MIRROR, faction);
						SectorEntityToken mirror5 = planet.getStarSystem().addCustomEntity(null, "Stellar Mirror Epsilon", Entities.STELLAR_MIRROR, faction);
						mirror1.setCircularOrbitPointingDown(planet, angle - 60, radius, period);
						mirror5.setCircularOrbitPointingDown(planet, angle + 60, radius, period);
						makeDiscoverable(mirror1, xp, profile);
						makeDiscoverable(mirror5, xp, profile);
					}
				}

				if (shade) {
					SectorEntityToken shade2 = planet.getStarSystem().addCustomEntity(null, "Stellar Shade Psi", Entities.STELLAR_SHADE, faction);
					shade2.setCircularOrbitPointingDown(planet, angle + 180 + 0, radius + 25, period);
					makeDiscoverable(shade2, xp, profile);

					if (!forceFew) {
						SectorEntityToken shade1 = planet.getStarSystem().addCustomEntity(null, "Stellar Shade Omega", Entities.STELLAR_SHADE, faction);
						SectorEntityToken shade3 = planet.getStarSystem().addCustomEntity(null, "Stellar Shade Chi", Entities.STELLAR_SHADE, faction);
						shade1.setCircularOrbitPointingDown(planet, angle + 180 - 26, radius - 10, period);
						shade3.setCircularOrbitPointingDown(planet, angle + 180 + 26, radius - 10, period);
						makeDiscoverable(shade1, xp, profile);
						makeDiscoverable(shade3, xp, profile);
					}
				}

				long seed = StarSystemGenerator.random.nextLong();
				planet.addTag(NOT_RANDOM_MISSION_TARGET);
				planet.getStarSystem().addTag(Tags.THEME_SPECIAL);

				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addOrbitalFusionLamp() {
		String source = "Glass World (Orbital Fusion Lamp)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_orbitalFusionLampKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;

				if (!system.isNebula()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//Not Star, Giant or Moon
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;
					if (curr.getOrbit() != null) continue;

					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;
					if (curr.getMarket().hasCondition(Conditions.IRRADIATED)) continue;

					//No orbiting objects, they would be annihilated
					if (system.getAllEntities().stream().filter(e -> e.getOrbitFocus() != null).anyMatch(e -> e.getOrbitFocus().equals(curr))) continue;

					picker.add(curr);
				}
			}

			 
			if (picker.isEmpty()) {
				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.OLD);
				params.systemTypes = new ArrayList<>();
				params.systemTypes.add(StarSystemGenerator.StarSystemType.NEBULA);
				params.forceNebula = true;

				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				float radius = 100f + random.nextFloat() * 50f;

				String name = (ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, system.getCenter().getName(), null).nameWithRomanSuffixIfAny);
				PlanetAPI ideal = system.addPlanet("jdp_orbitalFusionLamp", system.getCenter(), name, Planets.FROZEN, Math.round(Math.random() * 360), radius, 100, 10);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());
				Vector2f locationIdeal = findSpace(system, ideal);
				ideal.setLocation(locationIdeal.x, locationIdeal.y);
				ideal.setOrbit(null);

				system.updateAllOrbits();

				shuffleLocation(system, true, null, null, null, null);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				List<CampaignTerrainAPI> terrain = planet.getStarSystem().getTerrainCopy().stream().filter(t -> t.getType().equals(Terrain.MAGNETIC_FIELD) && t.getOrbitFocus().equals(planet)).toList();
				if (!terrain.isEmpty()) {
					terrain.get(0).setExpired(true);
				}

				Global.getSector().getMemoryWithoutUpdate().set("$jdp_orbitalFusionLampKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_orbitalFusionLamp", true);
				planet.getMemoryWithoutUpdate().set("$jdp_orbitalFusionLampBlockFirstSurvey", true);

				planet.setName("Helianthus");
				planet.getMarket().setName("Helianthus");

				planet.changeType(jdp_Planets.JDP_GLASSED, random);
				planet.getMarket().getConditions().clear();
				PlanetConditionGenerator.generateConditionsForPlanet(null, planet, planet.getStarSystem().getAge());

				removeConditions(planet.getMarket(), DECIVILIZED);
				removeConditions(planet.getMarket(), RUINS);
				removeConditions(planet.getMarket(), ATMOSPHERE);

				removeConditions(planet.getMarket(), TEMPERATURE);
				removeConditions(planet.getMarket(), FARMLAND);
				removeConditions(planet.getMarket(), ORES);
				removeConditions(planet.getMarket(), RARE_ORES);

				planet.getMarket().removeCondition(Conditions.IRRADIATED);
				planet.getMarket().removeCondition(Conditions.DARK);
				planet.getMarket().removeCondition(Conditions.EXTREME_WEATHER);

				planet.getMarket().addCondition(Conditions.EXTREME_WEATHER);
				planet.getMarket().addCondition(Conditions.VERY_COLD);
				planet.getMarket().addCondition(Conditions.DARK);
				planet.getMarket().addCondition(jdp_Conditions.JDP_GLASSEDWORLD);
				planet.getMarket().addCondition(jdp_Conditions.JDP_SUBSHEETECOSYSTEM);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				//Lamp
				SectorEntityToken jdp_erratic_lamp = planet.getStarSystem().addCustomEntity("jdp_erratic_lamp",
						null, // name - if null, defaultName from custom_entities.json will be used
						"jdp_erratic_lamp", // type of object, defined in custom_entities.json
						jdp_Factions.JDP_FABRIQUEORBITALE); // faction
				jdp_erratic_lamp.setCircularOrbit(planet, (planet.getFacing() - 5f), (planet.getRadius() + 100f), 500);
				jdp_erratic_lamp.addTag("jdp_erratic_lamp_tag");
				Misc.setDefenderOverride(jdp_erratic_lamp, new DefenderDataOverride(Factions.REMNANTS, 1f, 150, 150, 3));
				jdp_erratic_lamp.setOrbit(null);

				//Gate
				if (planet.getStarSystem().getEntitiesWithTag(Tags.GATE).isEmpty()) {
					SectorEntityToken gate = planet.getStarSystem().addCustomEntity("jdp_fo_gate",
							null, // name - if null, defaultName from custom_entities.json will be used
							"inactive_gate", // type of object, defined in custom_entities.json
							null); // faction
					Vector2f locationGate = findSpace(planet.getStarSystem(), gate);
					gate.setLocation(locationGate.x, locationGate.y);
					gate.setOrbit(null);
				}

				long seed = StarSystemGenerator.random.nextLong();
				planet.addTag(NOT_RANDOM_MISSION_TARGET);
				planet.getStarSystem().addTag(Tags.THEME_SPECIAL);

				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addAutonomousMantleBore() {
		String source = "Depleted World (Autonomous Mantle Bore)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_autonomousMantleBoreKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;

				if (system.hasPulsar()) continue;
				if (system.isNebula()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//Not Star, Giant or Moon
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;

					if (curr.hasCondition(Conditions.HABITABLE)) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;

					if (curr.getOrbitFocus() != null) {
						if (curr.getCircularOrbitRadius() < curr.getOrbitFocus().getRadius() + 2000) continue;
					}

					picker.add(curr);
				}
			}

			 
			if (picker.isEmpty()) {
				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params = normaliseParams(params);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 800);
				float orbitRadius = 4000;

				if (!gaps.isEmpty()) {
					orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
				}

				float radius = 100f + random.nextFloat() * 50f;
				float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
				
				String name = (ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, system.getCenter().getName(), null).nameWithRomanSuffixIfAny);
				PlanetAPI ideal = system.addPlanet("jdp_autonomousMantleBore", system.getCenter(), name, Planets.BARREN, Math.round(Math.random() * 360), radius, orbitRadius, orbitDays);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());

				system.updateAllOrbits();

				shuffleLocation(system, true, null, null, null, null);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				Global.getSector().getMemoryWithoutUpdate().set("$jdp_autonomousMantleBoreKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_autonomousMantleBore", true);
				planet.getMemoryWithoutUpdate().set("$jdp_autonomousMantleBoreBlockFirstSurvey", true);

				planet.changeType(jdp_Planets.JDP_DEPLETED, random);
				planet.getMarket().getConditions().clear();
				PlanetConditionGenerator.generateConditionsForPlanet(null, planet, planet.getStarSystem().getAge());

				removeConditions(planet.getMarket(), DECIVILIZED);
				removeConditions(planet.getMarket(), RUINS);
				removeConditions(planet.getMarket(), ATMOSPHERE);

				planet.getMarket().removeCondition(Conditions.IRRADIATED);
				planet.getMarket().removeCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);

				removeConditions(planet.getMarket(), VOLATILES);
				removeConditions(planet.getMarket(), ORGANICS);
				removeConditions(planet.getMarket(), ORES);
				removeConditions(planet.getMarket(), RARE_ORES);
				removeConditions(planet.getMarket(), FARMLAND);

				planet.getMarket().addCondition(Conditions.HABITABLE);
				planet.getMarket().addCondition(Conditions.POLLUTION);
				planet.getMarket().addCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
				planet.getMarket().addCondition(Conditions.RUINS_EXTENSIVE);
				planet.getMarket().addCondition(jdp_Conditions.JDP_QUARRYLAKES);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				planet.addTag(NOT_RANDOM_MISSION_TARGET);
				planet.getStarSystem().addTag(Tags.THEME_SPECIAL);

				var penelope = Global.getSector().getStarSystem("Penelope's Star");
				if (penelope != null) {
					List<SectorEntityToken> stableLocations = penelope.getEntitiesWithTag(Tags.STABLE_LOCATION);
					if (!stableLocations.isEmpty()) {
						SectorEntityToken stableLoc = stableLocations.get(1);

						ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData("jdp_mercury_eutc_Standard", ShipRecoverySpecial.ShipCondition.WRECKED, 0f);
						ship.shipName = "EUTS Intelligent Designer";
						DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);
						CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(penelope, Entities.WRECK, Factions.NEUTRAL, params);

						entity.getMemoryWithoutUpdate().set("$jdp_autonomousMantleBorePointer", true);
						entity.setDiscoverable(true);

						Random random = StarSystemGenerator.random;
						float orbitRadius = stableLoc.getRadius() + 200f;
						float orbitDays = orbitRadius / (10f + random.nextFloat() * 5f);
						entity.setCircularOrbit(stableLoc, random.nextFloat() * 360f, orbitRadius, orbitDays);

						ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
						data.notNowOptionExits = true;
						data.noDescriptionText = true;
						DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
						ShipRecoverySpecial.PerShipData copy = (ShipRecoverySpecial.PerShipData) dsep.getData().ship.clone();
						copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
						copy.variantId = null;
						copy.nameAlwaysKnown = true;
						copy.pruneWeapons = false;
						data.addShip(copy);

						Misc.setSalvageSpecial(entity, data);
					} else {
						log.info("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add pointer to " + source + " (Penelope's Star does not contain a Stable Location)");
					}
				} else {
					log.info("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add pointer to " + source + " (Penelope's Star does not exist)");
				}

				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addFullereneSpool() {
		String source = "Logistics Complex (Fullerene Spool)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_fullereneSpoolKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;

				if (system.hasPulsar()) continue;
				if (!system.hasBlackHole()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//Not Star, Giant or Moon
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;

					if (curr.hasCondition(Conditions.IRRADIATED)) continue;
					if (curr.hasCondition(Conditions.TECTONIC_ACTIVITY)) continue;
					if (curr.hasCondition(Conditions.EXTREME_WEATHER)) continue;
					if (curr.hasCondition(Conditions.EXTREME_TECTONIC_ACTIVITY)) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;

					if (system.getAllEntities().stream().filter(e -> e.getOrbitFocus() != null).anyMatch(e -> e.getOrbitFocus().equals(curr))) continue;

					picker.add(curr);
				}
			}

			 
			if (picker.isEmpty()) {
				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params = normaliseParams(params);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 800);
				float orbitRadius = 4000;

				if (!gaps.isEmpty()) {
					orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
				}

				float radius = 100f + random.nextFloat() * 50f;
				float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);

				String name = (ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, system.getCenter().getName(), null).nameWithRomanSuffixIfAny);
				PlanetAPI ideal = system.addPlanet("jdp_fullereneSpool", system.getCenter(), name, Planets.BARREN, Math.round(Math.random() * 360), radius, orbitRadius, orbitDays);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());
				ideal.getMarket().removeCondition(Conditions.IRRADIATED);
				ideal.getMarket().removeCondition(Conditions.TECTONIC_ACTIVITY);
				ideal.getMarket().removeCondition(Conditions.EXTREME_WEATHER);
				ideal.getMarket().removeCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);

				system.updateAllOrbits();
				shuffleLocation(system, true, null, null, null, null);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				Global.getSector().getMemoryWithoutUpdate().set("$jdp_fullereneSpoolKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_fullereneSpool", true);
				planet.getMemoryWithoutUpdate().set("$jdp_fullereneSpoolBlockFirstSurvey", true);

				removeConditions(planet.getMarket(), DECIVILIZED);

				planet.getMarket().removeCondition(Conditions.EXTREME_WEATHER);
				planet.getMarket().removeCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
				planet.getMarket().removeCondition(Conditions.TECTONIC_ACTIVITY);

				removeConditions(planet.getMarket(), RUINS);

				planet.getMarket().addCondition(Conditions.RUINS_WIDESPREAD);
				planet.getMarket().addCondition(jdp_Conditions.JDP_LOGISTICSCOMPLEX);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				//Anchor
				SectorEntityToken jdp_skyhook_anchor = planet.getStarSystem().addCustomEntity("jdp_skyhook_anchor",
						null, // name - if null, defaultName from custom_entities.json will be used
						"jdp_skyhook_anchor", // type of object, defined in custom_entities.json
						null); // faction
				jdp_skyhook_anchor.setCircularOrbitPointingDown(planet, Math.round(Math.random() * 360f), (planet.getRadius() + 120f), planet.getCircularOrbitPeriod());
				jdp_skyhook_anchor.addTag("jdp_skyhook_anchor_tag");
				makeDiscoverable(jdp_skyhook_anchor, 1500f, 2000f);

				float radius = jdp_skyhook_anchor.getRadius() + 20;
				float minOrbitDays = radius / 5;
				float maxOrbitDays = minOrbitDays + 5f;

				//Minefield
				planet.getContainingLocation().addOrbitalJunk(jdp_skyhook_anchor,
						"jdp_orbital_mines", // from custom_entities.json
						50, // num of junk
						12, 20, // min/max sprite size (assumes square)
						radius, // orbit radius
						40, // orbit width
						minOrbitDays, // min orbit days
						maxOrbitDays, // max orbit days
						30f, // min spin (degress/day)
						35f); // max spin (degrees/day)

				//Gate
				if (planet.getStarSystem().getEntitiesWithTag(Tags.GATE).isEmpty()) {
					SectorEntityToken gate = planet.getStarSystem().addCustomEntity("jdp_complex_gate",
							null, // name - if null, defaultName from custom_entities.json will be used
							"inactive_gate", // type of object, defined in custom_entities.json
							null); // faction
					List<OrbitGap> gaps = BaseThemeGenerator.findGaps(planet.getStarSystem().getCenter(), 3000, 20000, 800);
					float orbitRadius = 7000;
					if (!gaps.isEmpty()) {
						orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
					}
					float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
					gate.setCircularOrbit(planet.getStarSystem().getStar(), Math.round(Math.random() * 360), orbitRadius, orbitDays);
				}

				long seed = StarSystemGenerator.random.nextLong();
				planet.addTag(NOT_RANDOM_MISSION_TARGET);
				planet.getStarSystem().addTag(Tags.THEME_SPECIAL);

				new jdp_Kugel().generate();
				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addCombatDroneReplicator() {
		String source = "Rebel Arsenal (Combat Drone Replicator)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_combatDroneReplicatorKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;

				if (system.hasPulsar()) continue;
				if (system.isNebula()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//Not Star, Giant or Moon
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;

					if (curr.hasCondition(Conditions.IRRADIATED)) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;
					if (!curr.getMarket().hasCondition(Conditions.HABITABLE)) continue;

					if (system.getAllEntities().stream().filter(e -> e.getOrbitFocus() != null).anyMatch(e -> e.getOrbitFocus().equals(curr))) continue;

					picker.add(curr);
				}
			}

			 
			if (picker.isEmpty()) {				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params = normaliseParams(params);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 800);
				float orbitRadius = 4000;

				if (!gaps.isEmpty()) {
					orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
				}

				float radius = 100f + random.nextFloat() * 50f;
				float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);

				String name = (ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, system.getCenter().getName(), null).nameWithRomanSuffixIfAny);
				PlanetAPI ideal = system.addPlanet("jdp_combatDroneReplicator", system.getCenter(), name, Planets.PLANET_TERRAN, Math.round(Math.random() * 360), radius, orbitRadius, orbitDays);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());

				system.updateAllOrbits();

				shuffleLocation(system, true, null, null, null, null);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				List<CampaignTerrainAPI> terrain = planet.getStarSystem().getTerrainCopy().stream().filter(t -> t.getType().equals(Terrain.MAGNETIC_FIELD) && t.getOrbitFocus().equals(planet)).toList();
				if (!terrain.isEmpty()) {
					terrain.get(0).setExpired(true);
				}

				Global.getSector().getMemoryWithoutUpdate().set("$jdp_combatDroneReplicatorKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_combatDroneReplicator", true);
				planet.getMemoryWithoutUpdate().set("$jdp_combatDroneReplicatorBlockFirstSurvey", true);

				planet.setName("Kalafina");
				planet.getMarket().setName("Kalafina");

				removeConditions(planet.getMarket(), DECIVILIZED);
				removeConditions(planet.getMarket(), RUINS);

				if (!hasConditions(planet.getMarket(), FARMLAND)) planet.getMarket().addCondition(Conditions.FARMLAND_POOR);

				planet.getMarket().addCondition(Conditions.RUINS_VAST);
				planet.getMarket().addCondition(Conditions.DECIVILIZED);
				planet.getMarket().addCondition(Conditions.POLLUTION);
				planet.getMarket().addCondition(jdp_Conditions.JDP_REBELARSENAL);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				//Suppression Ship
				SectorEntityToken jdp_suppression_ship = planet.getStarSystem().addCustomEntity("jdp_suppression_ship",
						"DSS \"Discurso Grande\"", // name - if null, defaultName from custom_entities.json will be used
						"jdp_suppression_ship", // type of object, defined in custom_entities.json
						Factions.DERELICT); // faction
				jdp_suppression_ship.setCircularOrbit(planet, Math.round(Math.random() * 360f), (planet.getRadius() + 200f), Math.round(Math.random() * 3600f));
				jdp_suppression_ship.addTag("jdp_suppression_ship_tag");
				makeDiscoverable(jdp_suppression_ship, 1500f, 2000f);


				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addCryoarithmeticEngine() {
		String source = "Remnant Locus (Cryoarithmetic Engine)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_cryoarithmeticEngineKey")) {
			WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;
				if (!system.getType().equals(StarSystemGenerator.StarSystemType.TRINARY_2CLOSE)) continue;

				if (system.hasPulsar()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//No Hypershunt Systems
					if (curr.hasTag(Entities.CORONAL_TAP)) continue;

					picker.add(system);
				}
			}

			 
			if (picker.isEmpty()) {
				//Generate ideal system if none fit the requirements.

				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params.systemTypes = new ArrayList<>();
				params.systemTypes.add(StarSystemGenerator.StarSystemType.TRINARY_2CLOSE);
				params.starTypes = new ArrayList<>();
				params.starTypes.add(StarTypes.BLUE_GIANT);
				params.starTypes.add(StarTypes.BLUE_GIANT);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				system.updateAllOrbits();

				shuffleLocation(system, true, null, null, null, null);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(system);
			}

			StarSystemAPI system = picker.pick();

			if (system != null) {
				Global.getSector().getMemoryWithoutUpdate().set("$jdp_cryoarithmeticEngineKey", system);

				//Locus
				CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.REMNANTS, FleetTypes.BATTLESTATION, null);

				FleetMemberAPI locus = fleet.getFleetData().addFleetMember("jdp_locus_Locus");
				locus.setShipName("TTC2I-03 Melchior");

				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
				fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
				fleet.addTag(Tags.NEUTRINO_HIGH);

				fleet.setStationMode(true);

				addRemnantStationInteractionConfig(fleet);

				system.addEntity(fleet);

				fleet.clearAbilities();
				fleet.addAbility(Abilities.TRANSPONDER);
				fleet.getAbility(Abilities.TRANSPONDER).activate();
				fleet.getDetectedRangeMod().modifyFlat("gen", 1000f);

				fleet.setAI(null);

				EntityLocation loc = new EntityLocation();
				loc.location = new Vector2f();

				setEntityLocation(fleet, loc, null);
				convertOrbitWithSpin(fleet, 5f);

				String coreId = Commodities.ALPHA_CORE;
				AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(coreId);
				PersonAPI commander = plugin.createPerson(coreId, fleet.getFaction().getId(), random);
				//commander.setName("Amaranth Heuristic"); -Feels kinda contrived?

				fleet.setCommander(commander);
				fleet.getFlagship().setCaptain(commander);

				fleet.setId("jdp_remnantLocus");
				fleet.setName("Locus");
				fleet.addTag("$jdp_cryoarithmeticEngine");

				RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.getFlagship());
				RemnantOfficerGeneratorPlugin.addCommanderSkills(commander, fleet, null, 3, random);

				locus.getRepairTracker().setCR(locus.getRepairTracker().getMaxCR());

				fleet.getFleetData().setSyncNeeded();
				fleet.getFleetData().syncIfNeeded();

				//Warning Beacon
				SectorEntityToken anchor = system.getHyperspaceAnchor();
				List<SectorEntityToken> points = Global.getSector().getHyperspace().getEntities(JumpPointAPI.class);

				float minRange = 600;

				float closestRange = Float.MAX_VALUE;
				JumpPointAPI closestPoint = null;
				for (SectorEntityToken entity : points) {
					JumpPointAPI point = (JumpPointAPI) entity;

					if (point.getDestinations().isEmpty()) continue;

					JumpPointAPI.JumpDestination dest = point.getDestinations().get(0);
					if (dest.getDestination().getContainingLocation() != system) continue;

					float dist = Misc.getDistance(anchor.getLocation(), point.getLocation());
					if (dist < minRange + point.getRadius()) continue;

					if (dist < closestRange) {
						closestPoint = point;
						closestRange = dist;
					}
				}

				CustomCampaignEntityAPI beacon = Global.getSector().getHyperspace().addCustomEntity(null, null, Entities.WARNING_BEACON, Factions.NEUTRAL);
				beacon.getMemoryWithoutUpdate().set("$remnantResurgent", true);

				beacon.addTag(Tags.BEACON_HIGH);

				if (closestPoint == null) {
					float orbitDays = minRange / (10f + StarSystemGenerator.random.nextFloat() * 5f);
					beacon.setCircularOrbitPointingDown(anchor, StarSystemGenerator.random.nextFloat() * 360f, minRange, orbitDays);
				} else {
					float angleOffset = 20f + StarSystemGenerator.random.nextFloat() * 20f;
					float angle = Misc.getAngleInDegrees(anchor.getLocation(), closestPoint.getLocation()) + angleOffset;
					float radius = closestRange;

					if (closestPoint.getOrbit() != null) {
						OrbitAPI orbit = Global.getFactory().createCircularOrbitPointingDown(anchor, angle, radius, closestPoint.getOrbit().getOrbitalPeriod());
						beacon.setOrbit(orbit);
					} else {
						Vector2f beaconLoc = Misc.getUnitVectorAtDegreeAngle(angle);
						beaconLoc.scale(radius);
						Vector2f.add(beaconLoc, anchor.getLocation(), beaconLoc);
						beacon.getLocation().set(beaconLoc);
					}
				}

				Color glowColor = new Color(250, 25, 0, 255);
				Color pingColor = new Color(250, 125, 0, 255);

				Misc.setWarningBeaconColors(beacon, glowColor, pingColor);

				// Scripted Graveyards
				StarSystemData data = BaseThemeGenerator.computeSystemData(system);

				float max = 0f;
				JumpPointAPI fringePoint = null;
				List<JumpPointAPI> fringePoints = system.getEntities(JumpPointAPI.class);
				for (JumpPointAPI curr : fringePoints) {
					float dist = curr.getCircularOrbitRadius();
					if (dist > max) {
						max = dist;
						fringePoint = curr;
					}
				}
				if (fringePoint != null) {
					WeightedRandomPicker<String> remnantShipFactions = new WeightedRandomPicker<String>(random);
					remnantShipFactions.add(Factions.REMNANTS);
					WeightedRandomPicker<String> hullsRemnant = new WeightedRandomPicker<String>(random);
					hullsRemnant.add("radiant", 0.75f);
					hullsRemnant.add("nova", 0.75f);
					hullsRemnant.add("brilliant", 1f);
					hullsRemnant.add("apex", 1f);
					hullsRemnant.add("scintilla", 1f);
					hullsRemnant.add("scintilla", 1f);
					hullsRemnant.add("fulgent", 1f);
					hullsRemnant.add("fulgent", 1f);
					hullsRemnant.add("glimmer", 1f);
					hullsRemnant.add("glimmer", 1f);
					hullsRemnant.add("lumen", 1f);
					hullsRemnant.add("lumen", 1f);
					addShipGraveyard(data, fringePoint, remnantShipFactions, hullsRemnant);
					addDebrisField(data, fringePoint, 400f);

					WeightedRandomPicker<String> derelictShipFactions = new WeightedRandomPicker<String>(random);
					derelictShipFactions.add(Factions.HEGEMONY);
					WeightedRandomPicker<String> hullsHegemony = new WeightedRandomPicker<String>(random);
					hullsHegemony.add("legion_xiv", 0.25f);
					hullsHegemony.add("jdp_arcubus", 0.5f);
					hullsHegemony.add("onslaught_xiv", 0.5f);
					hullsHegemony.add("dominator_xiv", 0.75f);
					hullsHegemony.add("eagle_xiv", 0.75f);
					hullsHegemony.add("falcon_xiv", 0.75f);
					hullsHegemony.add("enforcer_xiv", 0.75f);
					hullsHegemony.add("legion", 1f);
					hullsHegemony.add("onslaught", 1f);
					hullsHegemony.add("dominator", 1f);
					hullsHegemony.add("eagle", 1f);
					hullsHegemony.add("falcon", 1f);
					hullsHegemony.add("enforcer", 1f);
					hullsHegemony.add("eradicator", 1f);
					hullsHegemony.add("mora", 1f);
					hullsHegemony.add("wolf_hegemony", 1f);
					hullsHegemony.add("prometheus", 1f);
					hullsHegemony.add("buffalo_hegemony", 1f);
					hullsHegemony.add("atlas", 1f);
					addShipGraveyard(data, fleet, derelictShipFactions, hullsHegemony);
					addShipGraveyard(data, fringePoint, derelictShipFactions, hullsHegemony);

					for (AddedEntity ae : data.generated) {
						SalvageSpecialAssigner.assignSpecials(ae.entity, true);
						if (ae.entity.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
							DerelictShipEntityPlugin shipEntityPlugin = (DerelictShipEntityPlugin) ae.entity.getCustomPlugin();
							shipEntityPlugin.getData().ship.condition = ShipRecoverySpecial.ShipCondition.WRECKED;
						}
					}
				}

				//Procgen Things
				addShipGraveyard(data, 0.25f, 1, 1,
						createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));

				addMiningStations(data, 0.5f, 1, 1, createStringPicker(Entities.STATION_MINING_REMNANT, 10f));

				addDebrisFields(data, 0.75f, 1, 5);

				addDerelictShips(data, 0.75f, 0, 7,
						createStringPicker(Factions.TRITACHYON, 10f, Factions.HEGEMONY, 7f, Factions.INDEPENDENT, 3f));

				RemnantSeededFleetManager fleets = new RemnantSeededFleetManager(data.system, 9, 12, 4, 16, 0.25f);
				data.system.addScript(fleets);

                RemnantThemeGenerator remGen = new RemnantThemeGenerator();
				remGen.addBattlestations(data, 1f, 1, 1, createStringPicker("remnant_station2_Damaged", 10f));

					//Relay
				List<SectorEntityToken> stableLocations = data.system.getEntitiesWithTag(Tags.STABLE_LOCATION);
				if (!stableLocations.isEmpty()) {
					SectorEntityToken stableLoc = data.system.getEntitiesWithTag(Tags.STABLE_LOCATION).get(0);
					SectorEntityToken cryoRelay = system.addCustomEntity("jdp_cryoRelay", // unique id
							null, // name - if null, defaultName from custom_entities.json will be used
							"comm_relay", // type of object, defined in custom_entities.json
							Factions.REMNANTS); // faction

					cryoRelay.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);
					if (stableLoc.getOrbit() != null) {
						cryoRelay.setOrbit(stableLoc.getOrbit().makeCopy());
					}
					cryoRelay.setLocation(stableLoc.getLocation().x, stableLoc.getLocation().y);
					data.system.removeEntity(stableLoc);
				} else {
					List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 800);
					float orbitRadius = 4000;

					if (!gaps.isEmpty()) {
						orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
					}

					float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
					SectorEntityToken cryoRelay = system.addCustomEntity("jdp_cryoRelay", // unique id
							null, // name - if null, defaultName from custom_entities.json will be used
							"comm_relay", // type of object, defined in custom_entities.json
							Factions.REMNANTS); // faction
					cryoRelay.setCircularOrbit(system.getCenter(), Math.round(Math.random() * 360), orbitRadius, orbitDays);
					cryoRelay.getMemoryWithoutUpdate().set(MemFlags.OBJECTIVE_NON_FUNCTIONAL, true);
				}

				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [System Centre] in [" + system.getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addDealmakerHolosuite() {
		String source = "Beguiling World (Dealmaker Holosuite)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");

		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_dealmakerHolosuiteKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;
				if (!system.getType().equals(StarSystemGenerator.StarSystemType.TRINARY_2FAR)) continue;
				if (system.getPlanets().size() <= 5);

				if (system.hasPulsar()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					//Not Star, Giant or Moon
					if (curr.isStar()) continue;
					if (curr.isGasGiant()) continue;
					if (curr.isMoon()) continue;

					if (curr.hasCondition(Conditions.IRRADIATED)) continue;
					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;

					if (curr.getOrbitFocus() != null) {
						if (curr.getCircularOrbitRadius() > curr.getOrbitFocus().getRadius() + 6000) continue;
						if (curr.getCircularOrbitRadius() < curr.getOrbitFocus().getRadius() + 2000) continue;
					}
					picker.add(curr);
				}
			}

			 
			if (picker.isEmpty()) {				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params.systemTypes = new ArrayList<>();
				params.systemTypes.add(StarSystemGenerator.StarSystemType.TRINARY_2FAR);
				params = normaliseStarTypes(params);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 800);
				//TODO Sometimes a gap isn't found and the planet ends up inside the sun.
				float orbitRadius = 4000;

				if (!gaps.isEmpty()) {
					orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
				}

				float radius = 100f + random.nextFloat() * 50f;
				float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);

				String name = (ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, system.getCenter().getName(), null).nameWithRomanSuffixIfAny);
				PlanetAPI ideal = system.addPlanet("$jdp_dealmakerHolosuite", system.getCenter(), name, Planets.BARREN, Math.round(Math.random() * 360), radius, orbitRadius, orbitDays);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());

				system.updateAllOrbits();

				shuffleLocation(system, true, null, null, null, null);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				List<CampaignTerrainAPI> terrain = planet.getStarSystem().getTerrainCopy().stream().filter(t -> t.getType().equals(Terrain.MAGNETIC_FIELD) && t.getOrbitFocus().equals(planet)).toList();
				if (!terrain.isEmpty()) {
					terrain.get(0).setExpired(true);
				}

				Global.getSector().getMemoryWithoutUpdate().set("$jdp_dealmakerHolosuiteKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_dealmakerHolosuite", true);
				planet.getMemoryWithoutUpdate().set("$jdp_dealmakerHolosuiteBlockFirstSurvey", true);

				planet.changeType(jdp_Planets.JDP_BEGUILING, random);
				planet.getMarket().getConditions().clear();
				PlanetConditionGenerator.generateConditionsForPlanet(null, planet, planet.getStarSystem().getAge());

				removeConditions(planet.getMarket(), DECIVILIZED);
				removeConditions(planet.getMarket(), RUINS);

				planet.getMarket().removeCondition(Conditions.EXTREME_WEATHER);
				planet.getMarket().removeCondition(Conditions.EXTREME_TECTONIC_ACTIVITY);
				planet.getMarket().removeCondition(Conditions.TECTONIC_ACTIVITY);

				planet.getMarket().removeCondition(Conditions.INIMICAL_BIOSPHERE);
				planet.getMarket().removeCondition(Conditions.MILD_CLIMATE);

				removeConditions(planet.getMarket(), FARMLAND);

				planet.getMarket().addCondition(Conditions.EXTREME_WEATHER);
				planet.getMarket().addCondition(Conditions.MILD_CLIMATE);
				planet.getMarket().addCondition(Conditions.WATER_SURFACE);
				planet.getMarket().addCondition(Conditions.RUINS_VAST);
				planet.getMarket().addCondition(jdp_Conditions.JDP_METASTATICSENSORIUM);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				planet.addScript(new jdp_ColonyFlicker(planet, 0.2F,true, false, false));

				//Gate
				if (planet.getStarSystem().getEntitiesWithTag(Tags.GATE).isEmpty()) {
					SectorEntityToken gate = planet.getStarSystem().addCustomEntity("jdp_themepark_gate",
							null, // name - if null, defaultName from custom_entities.json will be used
							"inactive_gate", // type of object, defined in custom_entities.json
							null); // faction
					List<OrbitGap> gaps = BaseThemeGenerator.findGaps(planet.getStarSystem().getCenter(), 3000, 20000, 800);
					float orbitRadius = 7000;
					if (!gaps.isEmpty()) {
						orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
					}
					float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
					gate.setCircularOrbit(planet.getStarSystem().getStar(), Math.round(Math.random() * 360), orbitRadius, orbitDays);
				}

				//Probes
				SectorEntityToken scriptedprobe = planet.getStarSystem().addCustomEntity(Misc.genUID(), "Probe", "jdp_probe", Factions.NEUTRAL, null);

				scriptedprobe.setCircularOrbitWithSpin(planet, (float) Math.random() * 360f,  planet.getRadius() + 250f,  (planet.getRadius() + 250f) / 10f, 5f, 5f);
				scriptedprobe.addTag("jdp_adutainmentProbe");

				for (SectorEntityToken jumpPoint : planet.getStarSystem().getJumpPoints()){
					SectorEntityToken probe = planet.getStarSystem().addCustomEntity(Misc.genUID(), "Probe", "jdp_probe", Factions.NEUTRAL, null);

					float orbitRadius = jumpPoint.getRadius() + 250f;
					probe.setCircularOrbitWithSpin(jumpPoint, (float) Math.random() * 360f, orbitRadius, orbitRadius / 10f, 5f, 5f);
					probe.addTag("jdp_adutainmentProbe");
				}

				for (SectorEntityToken jumpPoint : planet.getStarSystem().getEntitiesWithTag(Tags.GATE)){
					SectorEntityToken probe = planet.getStarSystem().addCustomEntity(Misc.genUID(), "Probe", "jdp_probe", Factions.NEUTRAL, null);

					float orbitRadius = jumpPoint.getRadius() + 250f;
					probe.setCircularOrbitWithSpin(jumpPoint, (float) Math.random() * 360f, orbitRadius, orbitRadius / 10f, 5f, 5f);
					probe.addTag("jdp_adutainmentProbe");
				}

				for (PlanetAPI  giant : planet.getStarSystem().getPlanets()){
					if (!giant.isGasGiant()) continue;
					if (giant.isStar()) continue;

					SectorEntityToken probe = planet.getStarSystem().addCustomEntity(Misc.genUID(), "Probe", "jdp_probe", Factions.NEUTRAL, null);

					float orbitRadius = giant.getRadius() + 250f;
					probe.setCircularOrbitWithSpin(giant, (float) Math.random() * 360f, orbitRadius, orbitRadius / 10f, 5f, 5f);
					probe.addTag("jdp_adutainmentProbe");
				}

				long seed = StarSystemGenerator.random.nextLong();
				planet.addTag(NOT_RANDOM_MISSION_TARGET);
				planet.getStarSystem().addTag(Tags.THEME_SPECIAL);

				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	protected void addPlasmaDynamo() {
		String source = "Domain-era Meshugatron (Plasma Dynamo)";
		log.info("JDP_RETROGEN_EVENTS: Looking for system to hide " + source + " in.");


		//If it already exists, don't.
		if (!Global.getSector().getMemoryWithoutUpdate().contains("$jdp_plasmaDynamoKey")) {
			WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<PlanetAPI>(random);

			for (StarSystemAPI system : Global.getSector().getStarSystems()) {
				if (validateSystem(system)) continue;

				if ((system.getLocation().x >= -48000) || (system.getLocation().y >= 12000)) continue;

				if (!system.hasTag(Tags.THEME_MISC_SKIP) && !system.hasTag(Tags.THEME_MISC)) continue;
				if (system.hasTag(Tags.THEME_DERELICT_CRYOSLEEPER)) continue;
				if (system.getPlanets().size() <= 3);

				if (system.hasPulsar()) continue;
				if (system.hasBlackHole()) continue;
				if (system.isNebula()) continue;

				for (PlanetAPI curr : system.getPlanets()) {
					if (!curr.isGasGiant()) continue;
					if (curr.hasCondition(Conditions.IRRADIATED)) continue;

					if (!curr.getMarket().isPlanetConditionMarketOnly()) continue;
					if (curr.hasTag(NOT_RANDOM_MISSION_TARGET)) continue;

					picker.add(curr);
				}
			}
			 
			if (picker.isEmpty()) {
				//Generate ideal system if none fit the requirements.
				StarSystemGenerator.CustomConstellationParams params = new StarSystemGenerator.CustomConstellationParams(StarAge.ANY);
				params.systemTypes = new ArrayList<>();
				params.systemTypes.add(StarSystemGenerator.StarSystemType.BINARY_FAR);
				params = normaliseStarTypes(params);
				StarSystemAPI system = jdp_GenerationUtils.generateSystem(params);

				List<OrbitGap> gaps = BaseThemeGenerator.findGaps(system.getCenter(), 2000, 6000, 1200);
				float orbitRadius = 4000;

				if (!gaps.isEmpty()) {
					orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
				}

				float radius = 300f + random.nextFloat() * 50f;
				float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);

				PlanetAPI ideal = system.addPlanet("$jdp_plasmaDynamo", system.getCenter(), "Charon", Planets.GAS_GIANT, Math.round(Math.random() * 360), radius, orbitRadius, orbitDays);
				PlanetConditionGenerator.generateConditionsForPlanet(null, ideal, ideal.getStarSystem().getAge());

				system.updateAllOrbits();

				shuffleLocation(system, true, null, -48000f, null, 12000f);

				MagicCampaign.hyperspaceCleanup(system);
				system.updateAllOrbits();

				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] Created system to hide " + source + " in.");
				log.info("JDP_RETROGEN_EVENTS: 		[CONTINGENCY] " + source + " in system with age [" + system.getAge() + "]");

				picker.add(ideal);
			}

			PlanetAPI planet = picker.pick();

			if (planet != null) {
				List<CampaignTerrainAPI> terrain = planet.getStarSystem().getTerrainCopy().stream().filter(t -> t.getType().equals(Terrain.MAGNETIC_FIELD) && t.getOrbitFocus().equals(planet)).toList();
				if (!terrain.isEmpty()) {
					terrain.get(0).setExpired(true);
				}

				Global.getSector().getMemoryWithoutUpdate().set("$jdp_plasmaDynamoKey", planet);

				planet.getMemoryWithoutUpdate().set("$jdp_plasmaDynamo", true);

				planet.getMemoryWithoutUpdate().set("$jdp_plasmaDynamo", true);
				planet.getMemoryWithoutUpdate().set("$jdp_plasmaDynamoBlockFirstSurvey", true);

				planet.setName("Charon");
				planet.getMarket().setName("Charon");

				planet.changeType(jdp_Planets.JDP_VOLATILE_GIANT, random);
				planet.getMarket().getConditions().clear();
				PlanetConditionGenerator.generateConditionsForPlanet(null, planet, planet.getStarSystem().getAge());

				PlanetSpecAPI spec = planet.getSpec();

				spec.setShieldTexture2(Global.getSettings().getSpriteName("industry", "plasma_net_texture"));
				spec.setShieldThickness2(0.15f);
				spec.setShieldColor2(new Color(255,255,255,255));

				planet.applySpecChanges();

				planet.addScript(new jdp_ColonyFlicker(planet, 0.002F, false, false, true));

				removeConditions(planet.getMarket(), DECIVILIZED);
				removeConditions(planet.getMarket(), RUINS);
				removeConditions(planet.getMarket(), VOLATILES);
				removeConditions(planet.getMarket(), DECIVILIZED);

				planet.getMarket().removeCondition(Conditions.EXTREME_WEATHER);

				planet.getMarket().addCondition(Conditions.DECIVILIZED);

				planet.getMarket().addCondition(Conditions.EXTREME_WEATHER);
				planet.getMarket().addCondition(Conditions.VOLATILES_PLENTIFUL);
				planet.getMarket().addCondition(Conditions.RUINS_EXTENSIVE);
				planet.getMarket().addCondition(jdp_Conditions.JDP_DOMAINMESHUGATRON);
				planet.getMarket().addCondition(jdp_Conditions.JDP_HUBRISMARKER);

				//Supply Station Ship
				SectorEntityToken jdp_meshugatron = planet.getStarSystem().addCustomEntity("jdp_meshugatron",
						null, // name - if null, defaultName from custom_entities.json will be used
						"jdp_meshugatron", // type of object, defined in custom_entities.json
						"neutral"); // faction
				jdp_meshugatron.setCircularOrbitPointingDown(planet, Math.round(Math.random() * 360f), (planet.getRadius() + 200f), Math.round(Math.random() * 360f));
				makeDiscoverable(jdp_meshugatron, 1500f, 2000f);

				jdp_meshugatron.setCircularOrbitPointingDown(planet, Math.round(Math.random() * 360f), (planet.getRadius() + 200f), planet.getRadius() / 10F);
				Misc.setAbandonedStationMarket("jdp_meshugatron", jdp_meshugatron);


				// add some unused stuff to the dockyard
				CargoAPI cargo = jdp_meshugatron.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
				cargo.initMothballedShips(Factions.HEGEMONY);

				/*CampaignFleetAPI temp = Global.getFactory().createEmptyFleet(Factions.HEGEMONY, null, true);
				temp.getFleetData().addFleetMember("enforcer_XIV_Elite");
				temp.getFleetData().addFleetMember("enforcer_XIV_Elite");
				temp.getFleetData().addFleetMember("eagle_xiv_Elite");
				temp.getFleetData().addFleetMember("dominator_XIV_Elite");
				DefaultFleetInflaterParams p = new DefaultFleetInflaterParams();
				p.quality = -1;
				temp.setInflater(new DefaultFleetInflater(p));
				temp.inflateIfNeeded();
				temp.setInflater(null);*/

				//Gate
				SectorEntityToken gate = planet.getStarSystem().addCustomEntity("jdp_shantygate",
						"Gatehome Freeport", // name - if null, defaultName from custom_entities.json will be used
						"jdp_shantygate", // type of object, defined in custom_entities.json
						null); // faction
				if (!planet.getStarSystem().getEntitiesWithTag(Tags.GATE).isEmpty()) {
					List<SectorEntityToken> othergates = planet.getStarSystem().getEntitiesWithTag(Tags.GATE).stream().toList();
					if (othergates.size() != 1) log.info("JDP_RETROGEN_EVENTS: 		Encountered multiple gates while adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
					SectorEntityToken otherGate = othergates.get(0);

					if (otherGate.getOrbit() != null) {
						gate.setOrbit(otherGate.getOrbit().makeCopy());
					}
					gate.setLocation(otherGate.getLocation().x, otherGate.getLocation().y);
					planet.getStarSystem().removeEntity(otherGate);
				} else {
					List<OrbitGap> gaps = BaseThemeGenerator.findGaps(planet.getStarSystem().getCenter(), 3000, 20000, 800);
					float orbitRadius = 7000;
					if (!gaps.isEmpty()) {
						orbitRadius = (gaps.get(0).start + gaps.get(0).end) * 0.5f;
					}
					float orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
					gate.setCircularOrbit(planet.getStarSystem().getStar(), Math.round(Math.random() * 360), orbitRadius, orbitDays);
				}

				MarketAPI market = Global.getFactory().createMarket("jdp_gatehome_market", gate.getName(), 5);
				market.setSize(5);
				market.setHidden(true);
				market.setFactionId(jdp_Factions.JDP_SIXTH);
				gate.setFaction(jdp_Factions.JDP_SIXTH);
				
				market.getMemoryWithoutUpdate().set(MemFlags.HIDDEN_BASE_MEM_FLAG, true);

				market.addCondition(Conditions.DECIVILIZED_SUBPOP);
				market.addCondition(Conditions.POPULATION_5);

				market.addIndustry(Industries.POPULATION);
				market.addIndustry(Industries.MILITARYBASE);
				market.addIndustry(Industries.STARFORTRESS);
				market.addIndustry(Industries.WAYSTATION);
				market.addIndustry(Industries.MEGAPORT);
				market.addIndustry(Industries.REFINING);
				market.addIndustry(Industries.HEAVYINDUSTRY);
				market.getIndustry(Industries.HEAVYINDUSTRY).setSpecialItem(new SpecialItemData(Items.CORRUPTED_NANOFORGE, null));
				
				market.addSubmarket(Submarkets.SUBMARKET_OPEN);
				market.addSubmarket(Submarkets.SUBMARKET_BLACK);
				market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());

				market.setPrimaryEntity(gate);
				gate.setMarket(market);

				market.setEconGroup(market.getId());
				market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);

				Global.getSector().getEconomy().addMarket(market, true);


				EconomyAPI.EconomyUpdateListener isolated = new jdp_IsolatedEconomyListener(market, "Brought in by smugglers and privateers");

				Global.getSector().getListenerManager().addListener(isolated);
				Global.getSector().getEconomy().addUpdateListener(isolated);

				market.reapplyIndustries();

				long seed = StarSystemGenerator.random.nextLong();
				planet.addTag(NOT_RANDOM_MISSION_TARGET);
				planet.getStarSystem().addTag(Tags.THEME_SPECIAL);

				log.info("JDP_RETROGEN_EVENTS: 		Adding " + source + " to [" + planet.getName() + "] in [" + planet.getContainingLocation().getNameWithLowercaseType() + "]");
			} else {
				log.warn("JDP_RETROGEN_EVENTS: 		[WARNING] Failed to add " + source + ".");
			}
		} else {
			log.info("JDP_RETROGEN_EVENTS: 		" + source + " already exists. No action taken");
		}
	}



	public Boolean validateSystem(StarSystemAPI system) {
		//General disqualifiers for messing with a system.
		//Ageless systems are by definition created manually by someone, so we ignore them in case they weren't tagged correctly.
		return (
				system.hasTag(Tags.THEME_HIDDEN) || system.hasTag(Tags.THEME_SPECIAL) || system.hasTag(Tags.THEME_CORE) ||
				system.isEnteredByPlayer() || (system.getAge() == null) || system.hasTag("wh_fixed") //<- Bandaid, might be unnecessary in future
		);
	}
}