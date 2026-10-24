package Jaydee8652.JaydeePiracy.campaign.entities;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.util.FlickerUtilV2;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;

public class jdp_MissileEntityPlugin extends BaseCustomEntityPlugin {
	public static Color LIGHT_COLOR = new Color(255,175,255,255);
	public static Color GLOW_COLOR = new Color(255,0,255,150);

	public static String GLOW_COLOR_KEY = "$core_lampGlowColor";
	public static String LIGHT_COLOR_KEY = "$core_lampLightColor";
	public static float GLOW_FREQUENCY = 0.2F;
	private transient SpriteAPI sprite;
	private transient SpriteAPI glow;
	protected float phase = 0.0F;
	protected FlickerUtilV2 flicker = new FlickerUtilV2();

	//Reuses the Fusion Lamp code, has some vestigial stuff from that in here.
	public jdp_MissileEntityPlugin() {
	}

	public void init(SectorEntityToken entity, Object pluginParams) {
		super.init(entity, pluginParams);
		entity.setDetectionRangeDetailsOverrideMult(0.75F);
		this.readResolve();
	}

	Object readResolve() {
		this.glow = Global.getSettings().getSprite("campaignEntities", "jdp_missile_glow");
		this.sprite = Global.getSettings().getSprite("campaignEntities", "jdp_missile_glow");
		return this;
	}

	public void advance(float amount) {
		for(this.phase += amount * GLOW_FREQUENCY; this.phase > 1.0F; --this.phase) {
		}

		this.flicker.advance(amount * 1F);
		SectorEntityToken focus = this.entity.getOrbitFocus();
		if (focus instanceof PlanetAPI) {
			PlanetAPI planet = (PlanetAPI)focus;
			float lightAlpha = this.getLightAlpha();
			lightAlpha *= this.entity.getSensorFaderBrightness();
			lightAlpha *= this.entity.getSensorContactFaderBrightness();
			planet.setSecondLight(new Vector3f(this.entity.getLocation().x, this.entity.getLocation().y, this.entity.getCircularOrbitRadius() * 0.75F), Misc.scaleColor(this.getLightColor(), lightAlpha));
		}
	}

	public float getFlickerBasedMult() {
		float f = 1.0F - 0.1F * this.flicker.getBrightness();
		return f;
	}

	public float getGlowAlpha() {
		float glowAlpha = 0.0F;
		if (this.phase < 0.5F) {
			glowAlpha = this.phase * 2.0F;
		}

		if (this.phase >= 0.5F) {
			glowAlpha = 1.0F - (this.phase - 0.5F) * 2.0F;
		}

		glowAlpha = 0.75F + glowAlpha * 0.25F;
		glowAlpha *= this.getFlickerBasedMult();
		if (glowAlpha < 0.0F) {
			glowAlpha = 0.0F;
		}

		if (glowAlpha > 1.0F) {
			glowAlpha = 1.0F;
		}

		return glowAlpha;
	}

	public float getLightAlpha() {
		float lightAlpha = 0.0F;
		if (this.phase < 0.5F) {
			lightAlpha = this.phase * 2.0F;
		}

		if (this.phase >= 0.5F) {
			lightAlpha = 1.0F - (this.phase - 0.5F) * 2.0F;
		}

		lightAlpha = 0.5F + lightAlpha * 0.5F;
		lightAlpha *= this.getFlickerBasedMult();
		if (lightAlpha < 0.0F) {
			lightAlpha = 0.0F;
		}

		if (lightAlpha > 1.0F) {
			lightAlpha = 1.0F;
		}

		return lightAlpha;
	}

	public Color getGlowColor() {
		Color glowColor = GLOW_COLOR;
		if (this.entity.getMemoryWithoutUpdate().contains(GLOW_COLOR_KEY)) {
			glowColor = (Color)this.entity.getMemoryWithoutUpdate().get(GLOW_COLOR_KEY);
		}

		return glowColor;
	}

	public Color getLightColor() {
		Color lightColor = LIGHT_COLOR;
		if (this.entity.getMemoryWithoutUpdate().contains(LIGHT_COLOR_KEY)) {
			lightColor = (Color)this.entity.getMemoryWithoutUpdate().get(LIGHT_COLOR_KEY);
		}

		return lightColor;
	}

	public void setGlowColor(Color color) {
		this.entity.getMemoryWithoutUpdate().set(GLOW_COLOR_KEY, color);
	}

	public void setLightColor(Color color) {
		this.entity.getMemoryWithoutUpdate().set(LIGHT_COLOR_KEY, color);
	}

	public float getRenderRange() {
		return this.entity.getRadius() + 1200.0F;
	}

	public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
		float alphaMult = viewport.getAlphaMult();
		alphaMult *= this.entity.getSensorFaderBrightness();
		alphaMult *= this.entity.getSensorContactFaderBrightness();
		if (!(alphaMult <= 0.0F)) {
			CustomEntitySpecAPI spec = this.entity.getCustomEntitySpec();
			if (spec != null) {
				float w = 180.0F;
				float h = 180.0F;
				Vector2f loc = this.entity.getLocation();
				if (this.sprite != null) {
					this.sprite.setAngle(this.entity.getFacing() - 90.0F);
					this.sprite.setAlphaMult(alphaMult);
					this.sprite.setSize(w, h);
					this.sprite.renderAtCenter(loc.x, loc.y);
				}

				float glowAlpha = this.getGlowAlpha();
				this.glow.setColor(this.getGlowColor());
				w = 180.0F;
				h = 180.0F;
				this.glow.setSize(w, h);
				this.glow.setAlphaMult(alphaMult * glowAlpha * 0.5F);
				this.glow.setAdditiveBlend();
				this.glow.renderAtCenter(loc.x, loc.y);

				for(int i = 0; i < 5; ++i) {
					w *= 1.0F;
					h *= 1.0F;
					this.glow.setSize(w, h);
					this.glow.setAlphaMult(alphaMult * glowAlpha * 0.67F);
					this.glow.renderAtCenter(loc.x, loc.y);
				}

			}
		}
	}
}



