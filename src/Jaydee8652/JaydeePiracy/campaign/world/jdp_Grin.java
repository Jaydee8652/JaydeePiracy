package Jaydee8652.JaydeePiracy.campaign.world;

import java.awt.Color;

import Jaydee8652.JaydeePiracy.scripts.jdp_DemonLeashAssignmentAI;
import Jaydee8652.JaydeePiracy.utils.jdp_Planets;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BattleCreationContext;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.DefenderDataOverride;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.world.TTBlackSite;
import com.fs.starfarer.api.impl.campaign.world.ZigLeashAssignmentAI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import org.lwjgl.util.vector.Vector2f;
import Jaydee8652.JaydeePiracy.utils.jdp_Factions;

public class jdp_Grin {
	public static String NOT_RANDOM_MISSION_TARGET = "$not_random_mission_target";



	//Makes the Demon interaction fire
	public static class jdp_DemonFIDConfig implements FleetInteractionDialogPluginImpl.FIDConfigGen {
		public FleetInteractionDialogPluginImpl.FIDConfig createConfig() {
			FleetInteractionDialogPluginImpl.FIDConfig config = new FleetInteractionDialogPluginImpl.FIDConfig();

			config.showTransponderStatus = false;
			config.showEngageText = false;
			config.alwaysPursue = true;
			config.dismissOnLeave = false;
			config.withSalvage = false;
			config.printXPToDialog = true;

			config.noSalvageLeaveOptionText = "Continue";

			config.delegate = new FleetInteractionDialogPluginImpl.BaseFIDDelegate() {
				public void postPlayerSalvageGeneration(InteractionDialogAPI dialog, FleetEncounterContext context, CargoAPI salvage) {
					new RemnantSeededFleetManager.RemnantFleetInteractionConfigGen().createConfig().delegate.
							postPlayerSalvageGeneration(dialog, context, salvage);
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

					ShipRecoverySpecial.PerShipData ship = new ShipRecoverySpecial.PerShipData("jdp_laplacian_Demon", ShipRecoverySpecial.ShipCondition.WRECKED, 0f);
					ship.shipName = "Naggarok";
					DerelictShipEntityPlugin.DerelictShipData params = new DerelictShipEntityPlugin.DerelictShipData(ship, false);
					CustomCampaignEntityAPI entity = (CustomCampaignEntityAPI) BaseThemeGenerator.addSalvageEntity(
							fleet.getContainingLocation(),
							Entities.WRECK, jdp_Factions.JDP_LAPLACIANS, params);
					Misc.makeImportant(entity, "jdp_laplacian");
					entity.getMemoryWithoutUpdate().set("$jdp_laplaciantag", true);

					entity.getLocation().x = fleet.getLocation().x + (50f - (float) Math.random() * 100f);
					entity.getLocation().y = fleet.getLocation().y + (50f - (float) Math.random() * 100f);

					ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
					data.notNowOptionExits = true;
					data.noDescriptionText = true;
					DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) entity.getCustomPlugin();
					ShipRecoverySpecial.PerShipData copy = (ShipRecoverySpecial.PerShipData) dsep.getData().ship.clone();
					copy.variant = Global.getSettings().getVariant(copy.variantId).clone();
					copy.variantId = null;
					copy.variant.addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
					copy.variant.addTag(Tags.SHIP_UNIQUE_SIGNATURE);
					data.addShip(copy);

					Misc.setSalvageSpecial(entity, data);

					dialog.setInteractionTarget(entity);
					RuleBasedInteractionDialogPluginImpl plugin = new RuleBasedInteractionDialogPluginImpl("jdp_AfterDemonDefeat");
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
		StarSystemAPI system = sector.createStarSystem("Grin");
		system.setOptionalUniqueId("jdp_Grin");
		system.setType(StarSystemType.SINGLE);
		system.addTag(Tags.THEME_UNSAFE);
		system.addTag(Tags.THEME_SPECIAL);
		LocationAPI hyper = Global.getSector().getHyperspace();
		system.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "music_campaign_alpha_site");

		system.setBackgroundTextureFilename("graphics/backgrounds/background4.jpg");

		//In the Abyss
		system.getLocation().set(12400, 54400);

		HyperspaceTerrainPlugin hyperTerrain = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
		NebulaEditor editor = new NebulaEditor(hyperTerrain);
		editor.clearArc(system.getLocation().x, system.getLocation().y, 0, 200, 0, 360f);

		//Grin Star
		PlanetAPI grin = system.initStar("jdp_grin", // unique id for this star
				jdp_Planets.JDP_RED_PULSAR, // id in planets.json
				250f,        // radius (in pixels at default zoom)
				1500,
				20f, // solar wind burn level
				1f, // flare probability
				6.0f); // CR loss multiplier, good values are in the range of 1-5); // corona radius, from star edge

		SectorEntityToken grinHorizon = system.addTerrain(Terrain.PULSAR_BEAM,
				new StarCoronaTerrainPlugin.CoronaParams(27500,
						1250,
						grin,
						2f,
						0f,
						2f)
		);
		grinHorizon.setCircularOrbit(grin, 0, 0, 15);

		system.setLightColor(new Color(255, 50, 50)); // light color in entire system, affects all entities

		//Maddie Fractal World
		PlanetAPI maddie = system.addPlanet("jdp_maddie", grin, "Maddie", "jdp_fractal", 50, 80, 2000, 45);
		maddie.setCustomDescriptionId("jdp_maddie");
		maddie.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
		maddie.getMarket().addCondition(Conditions.LOW_GRAVITY);
		maddie.getMarket().addCondition(Conditions.COLD);
		maddie.getMarket().addCondition(Conditions.DARK);
		maddie.getMarket().addCondition(Conditions.RARE_ORE_ABUNDANT);
		maddie.getMarket().getMemoryWithoutUpdate().set(NOT_RANDOM_MISSION_TARGET, true);
		maddie.getMemoryWithoutUpdate().set("$jdp_maddietag", true);

		//Jump points
		system.autogenerateHyperspaceJumpPoints(true, true);

		//Add Demon Fleet
		addFleet(maddie);
	}

	//Demon Fleet
	public static void addFleet(SectorEntityToken maddie) {
		CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(jdp_Factions.JDP_LAPLACIANS, FleetTypes.PATROL_LARGE, null);
		fleet.setName("Unidentified Fleet");
		fleet.setNoFactionInName(true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_LOW_REP_IMPACT, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
		fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
		fleet.getMemoryWithoutUpdate().set("$clearCommands_no_remove", true);
		fleet.getMemoryWithoutUpdate().set("$jdp_demon", true);

		fleet.getMemoryWithoutUpdate().set(MusicPlayerPluginImpl.KEEP_PLAYING_LOCATION_MUSIC_DURING_ENCOUNTER_MEM_KEY, true);

		PersonAPI person = createDemonCaptain();
		PersonAPI commander = createDemonCaptain();

		fleet.setCommander(commander);

		//Naggarok
		FleetMemberAPI naggarok = fleet.getFleetData().addFleetMember("jdp_laplacian_TrueDemon");
		naggarok.setCaptain(commander);
		naggarok.setShipName("Naggarok");
		naggarok.updateStats();
		naggarok.getRepairTracker().setCR(naggarok.getRepairTracker().getMaxCR());
		naggarok.setVariant(naggarok.getVariant().clone(), false, false);
		naggarok.getVariant().setSource(VariantSource.REFIT);
		naggarok.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

		//Eschaton
		FleetMemberAPI eschaton = fleet.getFleetData().addFleetMember("jdp_laplacian_TrueDemon");
		eschaton.setCaptain(person);
		eschaton.setShipName("Eschaton");
		eschaton.updateStats();
		eschaton.getRepairTracker().setCR(eschaton.getRepairTracker().getMaxCR());
		eschaton.setVariant(eschaton.getVariant().clone(), false, false);
		eschaton.getVariant().setSource(VariantSource.REFIT);
		eschaton.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

		//Gninesis
		FleetMemberAPI gninesis = fleet.getFleetData().addFleetMember("jdp_laplacian_TrueDemon");
		gninesis.setCaptain(person);
		gninesis.setShipName("Gninesis");
		gninesis.updateStats();
		gninesis.getRepairTracker().setCR(gninesis.getRepairTracker().getMaxCR());
		gninesis.setVariant(gninesis.getVariant().clone(), false, false);
		gninesis.getVariant().setSource(VariantSource.REFIT);
		gninesis.getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

		fleet.getFleetData().ensureHasFlagship();
		fleet.clearAbilities();
		fleet.setTransponderOn(true);

		Vector2f loc = new Vector2f(maddie.getLocation().x + 300 * ((float) Math.random() - 0.5f),
				maddie.getLocation().y + 300 * ((float) Math.random() - 0.5f));
		fleet.setLocation(loc.x, loc.y);
		maddie.getContainingLocation().addEntity(fleet);

		fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN,
				new jdp_DemonFIDConfig());

		fleet.addScript(new jdp_DemonLeashAssignmentAI(fleet, maddie));
	}

	//Demon Captain
	public static PersonAPI createDemonCaptain() {
		PersonAPI person = Global.getFactory().createPerson();
		person.setName(new FullName("Laplace's", "Demon", FullName.Gender.ANY));
		person.setFaction(jdp_Factions.JDP_LAPLACIANS);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", "jdp_demon"));
		person.setPersonality(Personalities.RECKLESS);
		person.setRankId(Ranks.UNKNOWN);
		person.setPostId(null);

		person.getStats().setSkipRefresh(true);

		person.getStats().setLevel(10);
		person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
		person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
		person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
		person.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
		person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
		person.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
		person.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 2);
		person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		person.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
		person.getStats().setSkillLevel(Skills.OMEGA_ECM, 1);
		person.getStats().setSkillLevel(Skills.NAVIGATION, 1);

		person.getStats().setSkipRefresh(false);

		return person;
	}
}













