package Jaydee8652.JaydeePiracy.scripts.skills.flowerfish.scripts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.campaign.ids.BattleObjectives;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.ForceConcentration;
import com.fs.starfarer.api.impl.campaign.skills.SupportDoctrine;
import com.fs.starfarer.api.input.InputEventAPI;

public class jdp_flowerfishPerpetualPursuitScript extends BaseEveryFrameCombatPlugin {
	private CombatEngineAPI engine;
	public void init(CombatEngineAPI engine) {
		this.engine = engine;
	}
	
	private ShipAPI prevPlayerShip = null;
	private int skipFrames = 0;

	public void advance(float amount, List<InputEventAPI> events) {
		if (engine == null) return;
		//if (engine.isPaused()) return;
		if (Global.getSettings().getModManager().isModEnabled("second_in_command")) {
			updateForSide(engine.getFleetManager(0));
			updateForSide(engine.getFleetManager(1));
		}
	}
	
	protected ShipAPI undoDPMod = null;

	private void updateForSide(CombatFleetManagerAPI manager) {
		PersonAPI commander = manager.getFleetCommander();
		if (commander == null) return;

		if (manager.getFleetCommander().hasTag("jdp_flowerfishPerpetualPursuit")) {
			List<BattleObjectiveAPI> objectives = Global.getCombatEngine().getObjectives();

			int held = (int) objectives.stream().filter(it -> it.getOwner() == manager.getOwner()).count();

			if (held >= 2f || objectives.isEmpty()) {
				commander.getStats().getDynamic().getMod(Stats.CAN_DEPLOY_LEFT_RIGHT_MOD).modifyFlat("jdp_flowerfishPerpetualPursuit", 1f);
			}
			if (held < 2f && !objectives.isEmpty()) {
				commander.getStats().getDynamic().getMod(Stats.CAN_DEPLOY_LEFT_RIGHT_MOD).unmodify("jdp_flowerfishPerpetualPursuit");
			}

		}
    }
}
