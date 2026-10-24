package Jaydee8652.JaydeePiracy.campaign.entities;

import java.awt.Color;

import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomEntitySpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.ids.Pings;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.RemnantSystemType;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class jdp_ProbeEntityPlugin extends BaseCustomEntityPlugin {

	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		entity.setDetectionRangeDetailsOverrideMult(0.75f);
	}

	private float sincePing = 10f;

	public void advance(float amount) {
		if (entity.hasTag("jdp_blockedAdutainment")) {
			for (SectorEntityToken probe : entity.getContainingLocation().getEntitiesWithTag("jdp_adutainmentProbe")) {
				probe.addTag("jdp_blockedAdutainment");
			}
		}

		if (entity.isInCurrentLocation()) {
			sincePing += amount;
			if (sincePing >= 6f) {
				sincePing = 0f;
				CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
				if (playerFleet != null && entity.getVisibilityLevelTo(playerFleet) == VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS && !entity.hasTag("jdp_blockedAdutainment")) {
					Global.getSector().addPing(entity, Pings.COMMS, new Color(70,200,255,255));
				}
			}
		}
	}
}









