package Jaydee8652.JaydeePiracy.scripts.weapons;

import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;

public class jdp_TyrantsEyeMineExplosion implements ProximityExplosionEffect {
	
	public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams("jdp_tyrantseye_minelayer", 10f);
		//p.hitGlowSizeMult = 0.5f;
		p.thickness = 50f;
		RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
	}
}



