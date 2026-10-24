package Jaydee8652.JaydeePiracy.campaign.econ.industries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import Jaydee8652.JaydeePiracy.utils.jdp_Items;
import Jaydee8652.JaydeePiracy.utils.jdp_People;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin;
import com.fs.starfarer.api.campaign.econ.InstallableIndustryItemPlugin.InstallableItemDescriptionMode;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import Jaydee8652.JaydeePiracy.campaign.entities.jdp_MissileEntityPlugin;
import com.fs.starfarer.api.impl.campaign.FusionLampEntityPlugin;
import com.fs.starfarer.api.impl.campaign.RuleBasedInteractionDialogPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.impl.*;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.DelayedActionScript;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;


public class jdp_ItemEffectsRepo {
	public static void addItemEffectsToVanillaRepo() {
		ItemEffectsRepo.ITEM_EFFECTS.putAll(ITEM_EFFECTS);
	}

	public static Map<String, InstallableItemEffect> ITEM_EFFECTS = new HashMap<String, InstallableItemEffect>() {{
		put(Items.PLANETKILLER, new BaseInstallableItemEffect(Items.PLANETKILLER) {
			public void apply(Industry industry) {}
			public void unapply(Industry industry) {}
			//All it does is enable the base industry, all other effects handled in the industry.
			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
					InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Supported by the infrastructure of a dedicated launch site, a Planetkiller Device is a show of force difficult to ignore.",
						pad, Misc.getHighlightColor());
			}
		});
		put(jdp_Items.JDP_LOBSTER_EGGS, new BaseInstallableItemEffect(jdp_Items.JDP_LOBSTER_EGGS) {
			public void apply(Industry industry) {
				if (!industry.getMarket().hasCondition(jdp_Conditions.JDP_VOLTURNIANLOBSTERPENS) && !industry.getMarket().hasCondition(Conditions.VOLTURNIAN_LOBSTER_PENS)) {
					industry.getMarket().addCondition(jdp_Conditions.JDP_VOLTURNIANLOBSTERPENS);
				}
			}

			public void unapply(final Industry industry) {
				if (industry.getMarket().hasCondition(jdp_Conditions.JDP_VOLTURNIANLOBSTERPENS)) {
					industry.getMarket().removeCondition(jdp_Conditions.JDP_VOLTURNIANLOBSTERPENS);
				}
			}

			protected void addItemDescriptionImpl(Industry industry, TooltipMakerAPI text, SpecialItemData data,
												  InstallableItemDescriptionMode mode, String pre, float pad) {
				text.addPara(pre + "Adds the %s condition.",
						pad, Misc.getHighlightColor(),
						"Volturnian Lobster Pens");
			}
		});
	}};
}




