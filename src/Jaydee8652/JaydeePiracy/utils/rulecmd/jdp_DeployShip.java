package Jaydee8652.JaydeePiracy.utils.rulecmd;

import Jaydee8652.JaydeePiracy.campaign.world.jdp_Hiroc;
import Jaydee8652.JaydeePiracy.campaign.world.jdp_Kugel;
import Jaydee8652.JaydeePiracy.scripts.intel.NotableSystemIntel;
import Jaydee8652.JaydeePiracy.scripts.jdp_DemonLeashAssignmentAI;
import Jaydee8652.JaydeePiracy.utils.jdp_People;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.events.sd.LeashAssignmentAI;
import com.fs.starfarer.api.impl.campaign.intel.misc.SurveyDataForPlanetIntel;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.missions.hub.TriggerFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.DerelictThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.SurveyDataSpecial;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_GenerationUtils.findInterestingSystem;

public class jdp_DeployShip extends BaseCommandPlugin {
    protected boolean isValidTarget(FleetMemberAPI member) {
        if (member.isFighterWing() || member.isAlly()) return false;
        if (Misc.isAutomated(member)) return false;
        if (!member.getCaptain().isDefault()) return false;
        if (member.getHullSpec().getFuel() == 0) return false;
        if (member.isFlagship()) return false;
        return true;
    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        var playerFleet = Global.getSector().getPlayerFleet();
        var members = new ArrayList<FleetMemberAPI>();
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (!isValidTarget(member)) continue;
            members.add(member);
        }

        if (!members.isEmpty()) {
            dialog.showFleetMemberPickerDialog("Select a ship to deploy", "Deploy", "Cancel", 3, 7, 58f, false, false, members,
            new FleetMemberPickerListener() {
                public void pickedFleetMembers(List<FleetMemberAPI> members) {
                    if (members != null && members.size() == 1) {
                        var selected = members.get(0);
                        var cr = selected.getRepairTracker().getCR();

                        CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(Factions.PLAYER, FleetTypes.PATROL_SMALL, null);
                        fleet.setName("TEST");
                        fleet.setTransponderOn(true);
                        fleet.setNoFactionInName(true);

                        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
                        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
                        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
                        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);

                        fleet.getMemoryWithoutUpdate().set("$clearCommands_no_remove", true);
                        fleet.getMemoryWithoutUpdate().set("$jdp_deployedFleet", true);

                        fleet.setId("jdp_deployedFleet");

                        var commander = Global.getSector().getPlayerFaction().createRandomPerson();
                        commander.setRankId(Ranks.PILOT);
                        commander.setPostId(Ranks.POST_OFFICER);

                        fleet.setCommander(commander);
                        fleet.getFleetData().addFleetMember(selected);
                        fleet.getFleetData().getMembersListCopy().get(0).getRepairTracker().setCR(cr);

                        playerFleet.getFleetData().removeFleetMember(selected);

                        playerFleet.getStarSystem().addEntity(fleet);
                        fleet.setLocation(playerFleet.getLocation().x, playerFleet.getLocation().y);

                        fleet.clearAssignments();
                        fleet.clearAbilities();
                        fleet.addAbility(Abilities.TRANSVERSE_JUMP);
                        var spec = fleet.getAbility(Abilities.TRANSVERSE_JUMP).getSpec();
                        var activationDays = spec.getActivationDays();

                        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, jdp_Kugel.getRelay(), 2.0F, "calibrating drive field");
                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, jdp_Kugel.getPlanet(), 1.0F, "moving into position", new Script() {
                            @Override
                            public void run() {
                                fleet.getAbility(Abilities.TRANSVERSE_JUMP).activate();
                            }
                        });
                        fleet.addAssignment(FleetAssignment.HOLD, fleet, activationDays + (activationDays * 0.5f), "initiating transverse jump");

                        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION, jdp_Kugel.getWell(), 50F, "moving into position", new Script() {
                            @Override
                            public void run() {
                                fleet.getAbility(Abilities.TRANSVERSE_JUMP).activate();
                            }
                        });
                        fleet.addAssignment(FleetAssignment.HOLD, fleet, activationDays + (activationDays * 0.5f), "initiating transverse jump", new Script() {
                            @Override
                            public void run() {
                                fleet.getContainingLocation().removeEntity(fleet);
                            }
                        });
                    }
                }

                public void cancelledFleetMemberPicking() {

                }
            });
        }
        return true;
    }

}

