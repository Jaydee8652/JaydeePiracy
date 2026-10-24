package Jaydee8652.JaydeePiracy.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class jdp_CuttlefishHide implements EveryFrameWeaponEffectPlugin {
    private float shiftRate = 0;
    private boolean runOnce = true;
    protected float phase = 0.0F;
    protected float direction = 1;

	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
		if (!weapon.getShip().isAlive()) return;

        if (runOnce) {
            shiftRate = weapon.getSpec().getTurnRate();
            runOnce = false;
        }

        phase += amount * direction * shiftRate * MathUtils.clamp(weapon.getShip().getHardFluxLevel(), 0.1f, 1f);
        while (phase > 1) {
            direction = -1;
            phase = 1;
        }
        while (phase < 0) {
            direction = 1;
            phase = 0;
        }

        //int red = Math.round(phase * 30) + 200;

        //Effectively makes the sprite more red, as it already has color.
        int green = 240 - (int) (Math.round(Math.pow(phase, 2) * 40));
        int blue = 245 - (int) Math.round(Math.pow(phase, 2) * 40);

        //Shifts the color of the base sprite, not the weapon.
        //The weapon is everything that is not shifted.
        Color color = new Color(255, green, blue);
        weapon.getShip().getSpriteAPI().setColor(color);
	}
}
