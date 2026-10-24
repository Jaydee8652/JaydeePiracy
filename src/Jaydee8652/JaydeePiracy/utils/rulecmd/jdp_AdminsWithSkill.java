package Jaydee8652.JaydeePiracy.utils.rulecmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;

import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getAdmins;


public class jdp_AdminsWithSkill extends BaseCommandPlugin {
	private TextPanelAPI textPanel;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		var admins = getAdmins().stream().filter(a -> a.getStats().hasSkill(params.get(0).string)).toList();
		this.textPanel = dialog.getTextPanel();
		for(PersonAPI admin: admins) {
			 textPanel.addImage(admin.getPortraitSprite());
			 textPanel.addPara(admin.getName().getFullName(), Misc.getTextColor());
		}
		return true;
	}
}








