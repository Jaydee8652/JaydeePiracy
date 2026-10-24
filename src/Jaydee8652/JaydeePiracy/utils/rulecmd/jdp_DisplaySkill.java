package Jaydee8652.JaydeePiracy.utils.rulecmd;

import java.awt.*;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.rpg.Person;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import org.lwjgl.opencl.CLEvent;


public class jdp_DisplaySkill extends BaseCommandPlugin {
	private TextPanelAPI textPanel;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String key = params.get(0).string;
		float level = Float.parseFloat(params.get(1).string);

		SkillSpecAPI spec = Global.getSettings().getSkillSpec(key);

		Person fake = (Person) Global.getFactory().createPerson();
		fake.setFaction(Factions.SLEEPER);
		fake.getStats().setSkillLevel(key, level);

		Boolean admin = false;
		if (spec.isAdminSkill()) admin = true;

		TextPanelAPI text = dialog.getTextPanel();

		text.addSkillPanel(fake, admin);
		text.setFontSmallInsignia();
		return true;
	}
}









