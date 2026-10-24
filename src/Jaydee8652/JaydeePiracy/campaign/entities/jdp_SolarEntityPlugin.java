package Jaydee8652.JaydeePiracy.campaign.entities;

import java.awt.Color;
import java.util.ArrayList;

import Jaydee8652.JaydeePiracy.utils.jdp_Conditions;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.econ.RecentUnrest;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;

import static Jaydee8652.JaydeePiracy.scripts.skills.jdp_solar.*;
import static Jaydee8652.JaydeePiracy.utils.jdp_Skills.JDP_SOLAR;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_IndustryUtils.updateMarket;
import static com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.random;
import static com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.addBombardVisual;
import static com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.getBombardDisruptDuration;

public class jdp_SolarEntityPlugin extends BaseCustomEntityPlugin {
	public float radius;
	public float angle;

	public float offset = Math.round(Math.random() * 50f);

	public float time = DEORBIT_TIME;
	public Boolean runonce = true;
	public float phase =1f;

	public MarketAPI market;

	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
	}

	public void runOnce() {
		radius = entity.getCircularOrbitRadius() - (entity.getOrbitFocus().getRadius() + entity.getRadius());
		angle = entity.getCircularOrbitAngle() - entity.getOrbitFocus().getCircularOrbitAngle();
		entity.getMemory().set(MARKET, entity.getOrbitFocus().getName());
		runonce = false;
	}

	public void averted() {
		market.removeTag("jdp_solar_warned");

		MessageIntel intel = new MessageIntel();
		intel.setIcon(Global.getSettings().getSkillSpec(JDP_SOLAR).getSpriteName());
		intel.endAfterDelay(1);
		intel.setSound(intel.getCommMessageSound());
		intel.addLine(Global.getSettings().getSkillSpec(JDP_SOLAR).getName() + " - " + market.getName(), Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
		intel.addLine(BaseIntelPlugin.BULLET + "Solar Array Collapse Averted", Misc.getTextColor(), new String[]{"Solar Array Collapse Averted"}, Misc.getPositiveHighlightColor());
		Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
	}

	public void destroyed() {
		MessageIntel intel = new MessageIntel();
		intel.setIcon(Global.getSettings().getSkillSpec(JDP_SOLAR).getSpriteName());
		intel.endAfterDelay(1);
		intel.setSound(intel.getCommMessageSound());
		intel.addLine(Global.getSettings().getSkillSpec(JDP_SOLAR).getName() + " - " + market.getName(), Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
		intel.addLine(BaseIntelPlugin.BULLET + "Total Solar Array Collapse", Misc.getTextColor(), new String[]{"Total Solar Array Collapse"}, Misc.getNegativeHighlightColor());
		Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
	}

	public void warn() {
		market.addTag("jdp_solar_warned");

		MessageIntel intel = new MessageIntel();
		intel.setIcon(Global.getSettings().getSkillSpec(JDP_SOLAR).getSpriteName());
		intel.endAfterDelay(1);
		intel.setSound(intel.getCommMessageSound());
		intel.addLine(Global.getSettings().getSkillSpec(JDP_SOLAR).getName() + " - " + market.getName(), Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
		intel.addLine(BaseIntelPlugin.BULLET + "Solar Array Collapse Imminent", Misc.getTextColor(), new String[]{"Solar Array Collapse Imminent"}, Misc.getNegativeHighlightColor());
		Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
	}

	public void advance(float amount) {
		if (runonce) runOnce();
		market = entity.getOrbitFocus().getMarket();
		if (market == null) return;

		if (!market.getAdmin().getStats().hasSkill(JDP_SOLAR)) {
			entity.getMemory().set(UNSTABLE, true);
			phase = -1f;
		} else {
			entity.getMemory().unset(UNSTABLE);
			phase = 1f;
		}

		time += phase;
		time = MathUtils.clamp(time, 0f, DEORBIT_TIME);
		entity.setCircularOrbitPointingDown(entity.getOrbitFocus(), (entity.getOrbitFocus().getCircularOrbitAngle() + angle) + offset * ((time / DEORBIT_TIME) - 1f), (radius * (time / DEORBIT_TIME)) + entity.getOrbitFocus().getRadius() + entity.getRadius(), entity.getCircularOrbitPeriod());

		if (time < DEORBIT_TIME * 0.8f && !market.hasTag("jdp_solar_warned")) warn();
		else if (time >= DEORBIT_TIME && market.hasTag("jdp_solar_warned")) averted();

		if (time <= 0) {
			Misc.fadeAndExpire(entity);
			if(!market.hasTag("jdp_solar_impact")) {
				market.addTag("jdp_solar_impact");

				market.removeCondition(jdp_Conditions.JDP_MAKESHIFTSOLARARRAY);
				entity.getOrbitFocus().getMemory().unset(SOLARS);

				RecentUnrest.get(market).add(3, "Makeshift Solar Array Impact");
				for (Industry ind : market.getIndustries()) {
					if (!ind.getSpec().hasTag(Industries.TAG_NO_SATURATION_BOMBARDMENT)) {
						if (ind.getDisruptedDays() >= ((float) getBombardDisruptDuration() / 6) * 0.8f) continue;
						ind.setDisrupted(((float) getBombardDisruptDuration() / 6) * StarSystemGenerator.getNormalRandom(random, 1f, 1.25f));
					}
				}

				destroyed();
				addBombardVisual(market.getPrimaryEntity());
				updateMarket(market);
			}
		}

		if (entity.getMemory().contains(CONTINGENCY)) {
			phase = 0f;
			if (entity.getOrbitFocus().getMemory().contains(SOLARS)) {
				ArrayList<SectorEntityToken> solars = (ArrayList<SectorEntityToken>) entity.getOrbitFocus().getMemory().get(SOLARS);
				for (SectorEntityToken solar : solars) {
					addBombardVisual(solar);
					Misc.fadeAndExpire(solar);
				}

				MessageIntel intel = new MessageIntel();
				intel.setIcon(Global.getSettings().getSkillSpec(JDP_SOLAR).getSpriteName());
				intel.endAfterDelay(1);
				intel.setSound(intel.getCommMessageSound());
				intel.addLine(Global.getSettings().getSkillSpec(JDP_SOLAR).getName() + " - " + market.getName(), Misc.getTextColor(), new String[]{}, Misc.getHighlightColor());
				intel.addLine(BaseIntelPlugin.BULLET + "Solar Array Collapse Averted", Misc.getPositiveHighlightColor());
				Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);

				market.removeCondition(jdp_Conditions.JDP_MAKESHIFTSOLARARRAY);
				updateMarket(market);
				entity.getOrbitFocus().getMemory().unset(SOLARS);
			}
		}
	}
}









