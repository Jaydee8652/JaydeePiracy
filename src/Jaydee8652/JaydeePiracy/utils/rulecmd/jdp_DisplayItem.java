package Jaydee8652.JaydeePiracy.utils.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseInstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.InstallableItemEffect;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.rpg.Person;
import com.fs.starfarer.api.ui.BaseTooltipCreator;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;


public class jdp_DisplayItem extends BaseCommandPlugin {
	private TextPanelAPI textPanel;

	public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {
		String itemID = null;
		itemID = params.get(0).string;

		SpecialItemSpecAPI itemSpec = Global.getSettings().getSpecialItemSpec(itemID);

		this.textPanel = dialog.getTextPanel();
		InstallableItemEffect effect = ItemEffectsRepo.ITEM_EFFECTS.get(itemID);

		//Name
		textPanel.addPara("Obtain: " + itemSpec.getName(), Misc.getHighlightColor(), Misc.getHighlightColor());

		TooltipMakerAPI tooltip = textPanel.beginTooltip();
		
		//Manufacturer
		tooltip.addPara(itemSpec.getManufacturer(), Global.getSettings().getDesignTypeColor(itemSpec.getManufacturer()), 0f);

		//Image
		TooltipMakerAPI item1 = tooltip.beginImageWithText(itemSpec.getIconName(), 48);

		item1.addPara(itemSpec.getDescFirstPara(), 10f);

		effect.addItemDescription(null, item1, new SpecialItemData(itemID, null), InstallableIndustryItemPlugin.InstallableItemDescriptionMode.CARGO_TOOLTIP);
		tooltip.addImageWithText(0f);
		textPanel.addTooltip();
		return true;
	}
}








