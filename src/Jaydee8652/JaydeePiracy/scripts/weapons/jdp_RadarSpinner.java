package Jaydee8652.JaydeePiracy.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;

public class jdp_RadarSpinner implements EveryFrameWeaponEffectPlugin {
    private float angle = 0;
    private float turnRate = 0;
    private boolean runOnce = true;
    protected float phase = 0.0F;
    protected float direction = 1;

	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
		if (!weapon.getShip().isAlive()) return;

        if (runOnce) {
            turnRate = weapon.getSpec().getTurnRate();
            runOnce = false;
        }
        float systemMult = 1;

        angle = MathUtils.clampAngle(angle + (turnRate * systemMult * amount));
		weapon.setCurrAngle(weapon.getShip().getFacing() + weapon.getSlot().getAngle() + angle);
	}
}
