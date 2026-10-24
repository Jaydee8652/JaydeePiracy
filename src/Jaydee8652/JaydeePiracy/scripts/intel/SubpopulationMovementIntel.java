package Jaydee8652.JaydeePiracy.scripts.intel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.awt.Color;

import Jaydee8652.JaydeePiracy.campaign.econ.jdp_recivilizedMinority;
import Jaydee8652.JaydeePiracy.scripts.intel.admin.SubpopulationTooltipCreator;
import Jaydee8652.JaydeeProficiencies.specialists.elements.jdp_DisplayElement;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static Jaydee8652.JaydeePiracy.campaign.econ.jdp_recivilizedMinority.getTravelDaysWithDelay;

public class SubpopulationMovementIntel extends BaseIntelPlugin {
	protected MarketAPI market;
	protected MarketAPI destination;
	protected int duration;
	protected MarketConditionAPI condition;
	protected jdp_recivilizedMinority.SubpopulationEffectData data;

	@Override
	protected String getName() {return (condition.getName() + " - Forced Relocation");}
	@Override
	public FactionAPI getFactionForUIColors() {return destination.getFaction();}
	@Override
	public String getIcon() {return Global.getSettings().getMarketConditionSpec(condition.getId()).getIcon();}
	@Override
	public SectorEntityToken getMapLocation(SectorMapAPI map) {return market.getPrimaryEntity();}
	@Override
	public String getCommMessageSound() {return "ui_discovered_entity";}

	public SubpopulationMovementIntel(MarketAPI market, MarketAPI destination, MarketConditionAPI condition, jdp_recivilizedMinority.SubpopulationEffectData data) {
		this.market = market;
		this.destination = destination;
		this.condition = condition;
		this.data = data;

		this.duration = getTravelDaysWithDelay(destination.getPrimaryEntity(), market.getPrimaryEntity());
	}

	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}

	protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
		Color h = Misc.getHighlightColor();
		Color g = Misc.getGrayColor();
		float pad = 3f;
		float opad = 10f;

		float initPad = pad;
		if (mode == ListInfoMode.IN_DESC) initPad = opad;

		Color tc = getBulletColorForMode(mode);

		bullet(info);
		unindent(info);
	}

	@Override
	public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
		String pre = "";
		String post = "";

		Color c = getTitleColor(mode);
		info.addPara(pre + getName() + post, c, 0f);
		addBulletPoints(info, mode);
	}

	@Override
	public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
		info.addPara("The " + condition.getName() + " previously on " + market.getName() + " is currently on route to " + destination.getName(), 3f, Misc.getHighlightColor(), market.getName() + "", destination.getName());
		info.addSpacer(10f);

		var timeRemaining = Math.round(duration - Global.getSector().getClock().getElapsedDaysSince(getPlayerVisibleTimestamp()));
		var plurality = "s";
		if(timeRemaining <= 1) plurality = "";

		info.addPara("Will arrive in " + timeRemaining + " day" + plurality, 3f, Misc.getHighlightColor(), timeRemaining + "");
		info.addPara("*Hover icon to view affects", Misc.getGrayColor(), 3f);
		info.addSpacer(3f);
		MarketConditionSpecAPI conditionSpec = Global.getSettings().getMarketConditionSpec(condition.getId());
		if (conditionSpec != null) {
			var imageWidth = 40f;

			var sprite = Global.getSettings().getSprite(conditionSpec.getIcon());
			var imageHeight = (imageWidth / sprite.getWidth()) * sprite.getHeight();
			var color = Misc.getBasePlayerColor();

			var displayElement = new jdp_DisplayElement(sprite, "", color, info, imageWidth, imageHeight);
			displayElement.setHasBorder(true);
			info.addTooltipTo(new SubpopulationTooltipCreator(data.getType(), data.getOriginMarket(), true), displayElement.getElementPanel(), TooltipMakerAPI.TooltipLocation.BELOW);
		}
	}

	@Override
	public Set<String> getIntelTags(SectorMapAPI map) {
		Set<String> tags = super.getIntelTags(map);
		tags.add(Tags.INTEL_FLEET_LOG);
		return tags;
	}

	public String getSortString() {
		if (getTagsForSort().contains(Tags.INTEL_FLEET_LOG) || getTagsForSort().contains(Tags.INTEL_EXPLORATION)) {
			return getSortStringNewestFirst();
		}
		return "AAA";
	}

	public String getSmallDescriptionTitle() {
		return getName();
	}

	@Override
	public List<IntelInfoPlugin.ArrowData> getArrowData(SectorMapAPI map) {
		SectorEntityToken src = market.getPrimaryEntity();
		SectorEntityToken dest = destination.getPrimaryEntity();

		if (src != null && dest != null && src.getContainingLocation() != dest.getContainingLocation()) {
			List<IntelInfoPlugin.ArrowData> result = new ArrayList<IntelInfoPlugin.ArrowData>();

			IntelInfoPlugin.ArrowData arrow = new IntelInfoPlugin.ArrowData(src, dest);

			arrow.color = Global.getSettings().getSprite(getIcon()).getAverageBrightColor();
			arrow.width = 20f;
			result.add(arrow);

			return result;
		} else {
			return null;
		}
	}
}









