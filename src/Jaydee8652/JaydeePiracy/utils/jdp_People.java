package Jaydee8652.JaydeePiracy.utils;

import Jaydee8652.JaydeePiracy.JaydeePiracyPlugin;
import Jaydee8652.JaydeePiracy.campaign.world.jdp_Hiroc;
import Jaydee8652.JaydeePiracy.plugins.jdp_lunaSettings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.missions.RecoverAPlanetkiller;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseMissionHub;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator;
import org.apache.log4j.Logger;
import Jaydee8652.JaydeePiracy.utils.jdp_Ranks;

import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.*;
import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getSpecialistData;
import static com.fs.starfarer.api.impl.campaign.ids.People.createFactionLeaders;

public class jdp_People {
	public static String JDP_CALHOUM = "jdp_calhoum";
	public static String JDP_RULISON = "jdp_rulison";
	public static String JDP_DEMON = "jdp_demon";
	public static String JDP_RAMPANT = "jdp_rampant";
	public static String JDP_PAOLO = "jdp_paolo";
	public static String JDP_OMEGA = "jdp_omega";
	public static String JDP_PENDRAGON = "jdp_pendragon";
	public static String JDP_PENDRAGONSICK = "jdp_pendragon2";
	public static String JDP_PENDRAGONASCEND = "jdp_pendragon3";
	public static String JDP_DEADCOMM = "jdp_deadcomm";
	public static String JDP_MAERULA = "jdp_maerula";
	public static String JDP_WASABI = "jdp_wasabi";
	public static String JDP_KABAYAKI = "jdp_kabayaki";
	public static String JDP_DZINTRA = "jdp_dzintra";
	public static String JDP_AGYALA = "jdp_agyala";
	public static String JDP_MARTA = "jdp_marta";
	public static String JDP_KLAES = "jdp_klaes";

	public static Logger log = Global.getLogger(JaydeePiracyPlugin.class);

	public static PersonAPI getPerson(String id) {
		return Global.getSector().getImportantPeople().getPerson(id);
	}

	public static void jdp_createMiscCharacters() {
		ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
		PersonAPI person;
		MarketAPI market;

		//Hayes Calhoum - Base Commander of the Interfector Launch Site
		person = Global.getFactory().createPerson();
		person.setId(JDP_CALHOUM);
		person.setFaction(Factions.PLAYER);
		person.setGender(Gender.MALE);
		person.setRankId(Ranks.GROUND_COLONEL);
		person.setPostId(Ranks.POST_BASE_COMMANDER);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Hayes");
		person.getName().setLast("Calhoum");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_CALHOUM));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Rulison Plowshare - E.U.T.C Terraformer
		person = Global.getFactory().createPerson();
		person.setId(JDP_RULISON);
		person.setFaction(jdp_Factions.JDP_ERIDANIUTOPIA);
		person.setGender(Gender.MALE);
		person.setRankId(jdp_Ranks.JDP_ENGINEER);
		person.setPostId(jdp_Ranks.POST_SPECIALIST_TERRAFORMER);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Rulison");
		person.getName().setLast("Plowshare");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_RULISON));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
			var data = getSpecialistData();
			data.createSpecialist(person, JDP_PROFICIENCY_TERRAFORMER);

			var qaras = Global.getSector().getEconomy().getMarket("qaras");
			person.setMarket(qaras);
			qaras.getCommDirectory().addPerson(person, 4);
			qaras.getCommDirectory().getEntryForPerson(person).setHidden(true);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Marta Lafey - Kalafina Larme Admiral
		person = Global.getFactory().createPerson();
		person.setId(JDP_MARTA);
		person.setFaction(jdp_Factions.JDP_ERIDANIUTOPIA);
		person.setGender(Gender.FEMALE);
		person.setRankId(Ranks.SPACE_ADMIRAL);
		person.setPostId(Ranks.POST_ARISTOCRAT);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Marta");
		person.getName().setLast("Lafey");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_MARTA));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//*Finger* Prismata - Not *many bubbles*, nor *light relfections*.
		person = Global.getFactory().createPerson();
		person.setId(JDP_OMEGA);
		person.setFaction(Factions.OMEGA);
		person.setGender(Gender.ANY);
		person.setRankId(jdp_Ranks.JDP_DANCER);
		person.setPostId(jdp_Ranks.POST_JDP_GAMEMASTER);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("*Finger*");
		person.getName().setLast("Prismata");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_OMEGA));
		if (!ip.containsPerson(person)) {
			person.getStats().setLevel(7);
			person.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
			person.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			person.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
			person.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
			person.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
			person.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
			person.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
			person.setPersonality(Personalities.RECKLESS);

			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
			var data = getSpecialistData();
			data.createSpecialist(person, JDP_PROFICIENCY_DANCER);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Ochre Pendragon - The object of your quest
		person = Global.getFactory().createPerson();
		person.setId(JDP_PENDRAGON);
		person.setFaction(Factions.REMNANTS);
		person.setGender(Gender.ANY);
		person.setRankId(jdp_Ranks.JDP_CAPTIVE);
		person.setPostId(jdp_Ranks.POST_JDP_BLANK);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Ochre");
		person.getName().setLast("Pendragon");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_PENDRAGON));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Ochre Pendragon (Sick) - It is a good pain.
		person = Global.getFactory().createPerson();
		person.setId(JDP_PENDRAGONSICK);
		person.setFaction(Factions.REMNANTS);
		person.setGender(Gender.ANY);
		person.setRankId(jdp_Ranks.JDP_CAPTIVE);
		person.setPostId(jdp_Ranks.POST_JDP_BLANK);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Ochre");
		person.getName().setLast("Pendragon");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_PENDRAGONSICK));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Ochre Pendragon (Ascendent) - He's gone to a better place. Maybe?
		person = Global.getFactory().createPerson();
		person.setId(JDP_PENDRAGONASCEND);
		person.setFaction(Factions.REMNANTS);
		person.setGender(Gender.ANY);
		person.setRankId(jdp_Ranks.JDP_CAPTIVE);
		person.setPostId(jdp_Ranks.POST_JDP_BLANK);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Ochre");
		person.getName().setLast("Pendragon");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_PENDRAGONASCEND));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Deadcomm - Not a person, just comms static. Utility for when people hang up on you and I want it to have more impact.
		person = Global.getFactory().createPerson();
		person.setId(JDP_DEADCOMM);
		person.setFaction(jdp_Factions.JDP_DEADCOMM);
		person.setGender(Gender.ANY);
		person.setRankId(jdp_Ranks.JDP_DEAD);
		person.setPostId(jdp_Ranks.POST_JDP_ERROR);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("ERROR: DATA INVALID");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_DEADCOMM));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Paolo Casiraghi - Galatia Domain History Lecturer
		person = Global.getFactory().createPerson();
		person.setId(JDP_PAOLO);
		person.setFaction(Factions.INDEPENDENT);
		person.setGender(Gender.MALE);
		person.setRankId(Ranks.CITIZEN);
		person.setPostId(Ranks.POST_ACADEMICIAN);
		person.getName().setFirst("Paolo");
		person.getName().setLast("Casiraghi");
		person.setImportance(PersonImportance.HIGH);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_PAOLO));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Agyala Tsimnova - Domain Logistics Officer
		person = Global.getFactory().createPerson();
		person.setId(JDP_AGYALA);
		person.setFaction(jdp_Factions.JDP_TECHNOLOGY);
		person.setGender(Gender.FEMALE);
		person.setRankId(Ranks.AGENT);
		person.setPostId(jdp_Ranks.POST_SPECIALIST_INSTRUMENT);
		person.getName().setFirst("Agyala");
		person.getName().setLast("Tsimnova");
		person.setImportance(PersonImportance.HIGH);
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_AGYALA));

		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
			var data = getSpecialistData();
			data.createSpecialist(person, JDP_PROFICIENCY_INSTRUMENT);

			var jangala = Global.getSector().getEconomy().getMarket("jangala");
			person.setMarket(jangala);
			jangala.getCommDirectory().addPerson(person, 4);
			if (jdp_lunaSettings.jdp_experimentalInstrument().equals(false)) jangala.getCommDirectory().getEntryForPerson(person).setHidden(true);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Maerula - Flowerfish pseudo-Faction Lead
		person = Global.getFactory().createPerson();
		person.setId(JDP_MAERULA);
		person.setFaction(jdp_Factions.JDP_FLOWERFISH);
		person.setGender(Gender.ANY);
		person.setRankId(Ranks.SENIOR_EXECUTIVE);
		person.setPostId(Ranks.POST_HACKER);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Maerula");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_MAERULA));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);

			market = Global.getSector().getEconomy().getMarket("ailmar");
			if (market != null) {
				market.addPerson(person);
				market.getCommDirectory().addPerson(person, 3);
				market.getCommDirectory().getEntryForPerson(person).setHidden(true);
			}
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Wasabi - Flowerfish "offical"-Faction Lead
		person = Global.getFactory().createPerson();
		person.setId(JDP_WASABI);
		person.setFaction(jdp_Factions.JDP_FLOWERFISH);
		person.setGender(Gender.FEMALE);
		person.setRankId(Ranks.SPECIAL_AGENT);
		person.setPostId(Ranks.POST_SECURITY_CHIEF);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("\"Wasabi\"");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_WASABI));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Kabayaki - Flowerfish Faction representative in the Fikenheld court
		person = Global.getFactory().createPerson();
		person.setId(JDP_KABAYAKI);
		person.setFaction(jdp_Factions.JDP_FLOWERFISH);
		person.setGender(Gender.FEMALE);
		person.setRankId(Ranks.SPECIAL_AGENT);
		person.setPostId(Ranks.POST_SECURITY_CHIEF);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("\"Kabayaki\"");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_KABAYAKI));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Klaes Abserscal - Pirate King, successor to Leonis and inheritor of the VIth Battlegroup
		person = Global.getFactory().createPerson();
		person.setId(JDP_KLAES);
		person.setFaction(jdp_Factions.JDP_SIXTH);
		person.setGender(Gender.MALE);
		person.setRankId(Ranks.SPACE_CAPTAIN);
		person.setPostId(Ranks.POST_ADMINISTRATOR);
		person.setImportance(PersonImportance.HIGH);
		person.getName().setFirst("Klaes");
		person.getName().setLast("Aberscal");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_KLAES));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);

			market = Global.getSector().getEconomy().getMarket("jdp_gatehome_market");//jdp_gatehome_market
			if (market != null) {
				market.setAdmin(person);
				market.getCommDirectory().addPerson(person, 0);
				market.addPerson(person);
			}
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");

		//Dzintra Bardou - Head of the Fabrique Orbitale remnants.
		person = Global.getFactory().createPerson();
		person.setId(JDP_DZINTRA);
		person.setFaction(jdp_Factions.JDP_FABRIQUEORBITALE);
		person.setGender(Gender.FEMALE);
		person.setRankId(Ranks.SENIOR_EXECUTIVE);
		person.setPostId(Ranks.POST_ADMINISTRATOR);
		person.setImportance(PersonImportance.MEDIUM);
		person.getName().setFirst("Dzintra");
		person.getName().setLast("Bardou");
		person.setPortraitSprite(Global.getSettings().getSpriteName("characters", JDP_DZINTRA));
		if (!ip.containsPerson(person)) {
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") did not exist. Has been generated retroactively");
			ip.addPerson(person);
		} else
			log.info("JDP_RETROGEN_PEOPLE: " + person.getName().getFullName() + " (" + person.getId() + ") already exists. No action taken");
	}
}
