package Jaydee8652.JaydeePiracy.scripts.weapons;

import java.awt.*;
import java.util.*;
import java.util.List;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.dweller.RiftLightningEffect;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import static Jaydee8652.JaydeePiracy.scripts.jdp_StolenUtils.isShip;
import static com.fs.starfarer.api.util.Misc.ZERO;

public class jdp_BroadheadEffect implements EveryFrameWeaponEffectPlugin, OnHitEffectPlugin, OnFireEffectPlugin, ProximityExplosionEffect {
	public static Map<DamageType, String> damageTypeMap = new HashMap<>();

    public static final String HE_WEAPON_NAME = "jdp_broadhead_he";
    public static final String SPLIT_WEAPON_NAME = "jdp_broadhead_split";
    public static final String KINETIC_WEAPON_NAME = "jdp_broadhead_kinetic";
    private float count;
    static {
        damageTypeMap.put(DamageType.HIGH_EXPLOSIVE, HE_WEAPON_NAME);
        damageTypeMap.put(DamageType.KINETIC, KINETIC_WEAPON_NAME);
	}


	public final float SPLIT_INACCURACY = 10f;
	public final float SPLIT_SPEED_VARIATION = 0.1f;
	public final int SPLIT_COUNT = 6;//6
	public final Color MUZZLE_COLOR = new Color(255, 180, 113);
	public final Color SPLIT_COLOR = new Color(232, 198, 143, 205);
    public final Color PIERCE_COLOR = new Color(125, 125, 100);


	private boolean checkedType = false;
	private DamageType damageType = DamageType.HIGH_EXPLOSIVE;

	private final List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();

	private boolean hitShield = false;

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run if we are paused
        if (engine.isPaused() || weapon.getShip() == null || !engine.isEntityInPlay(weapon.getShip())) {
            return;
        }
        ShipAPI ship = weapon.getShip();

        //Swap type
        if (!weapon.isFiring()) {
            if (!checkedType) {
                checkedType = true;

                if (hitShield) {
                    damageType = DamageType.KINETIC;
                } else {
                    damageType = DamageType.HIGH_EXPLOSIVE;
                }
            }
        } else {
            checkedType = false;
        }


		//Tahlan Split Code

        //Lists for cleaning up dead projectiles from memory
        List<DamagingProjectileAPI> cleanList = new ArrayList<>();
        List<DamagingProjectileAPI> splitProjectiles = new ArrayList<>();

        //Splits shots that should be splitting
        for (DamagingProjectileAPI proj : alreadyRegisteredProjectiles) {
            //Only split burst shots
            String splitProjName = HE_WEAPON_NAME + "_shot";
            if (proj.getProjectileSpecId().contains(splitProjName)) {
                //Calculates split range : hard-coded to match up with range properly
                Vector2f loc = proj.getLocation();
                float projAngle = proj.getFacing();
                float projDamage = proj.getDamageAmount();
                float splitDuration = (weapon.getRange() / weapon.getProjectileSpeed()) * 0.5f;

                //Split once our duration has passed; spawn a bunch of shots
                if (proj.getElapsed() > splitDuration) {
                    //Hide the explosion with some muzzle flash
                    engine.addSmoothParticle(loc, ZERO, 200f, 0.5f, 0.1f, MUZZLE_COLOR);
                    engine.addHitParticle(loc, ZERO, 100f, 0.5f, 0.25f, SPLIT_COLOR);

                    //Actually spawn shots
                    for (int i = 0; i < SPLIT_COUNT; i++) {
                        //Spawns the shot, with some inaccuracy
                        float angleOffset = MathUtils.getRandomNumberInRange(-SPLIT_INACCURACY / 2, SPLIT_INACCURACY / 2) + MathUtils.getRandomNumberInRange(-SPLIT_INACCURACY / 2, SPLIT_INACCURACY / 2);
                        DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, SPLIT_WEAPON_NAME, loc, projAngle + angleOffset, new Vector2f(0, 0));
                        splitProjectiles.add(newProj);

                        //Varies the speed slightly, for a more artillery-esque look
                        float rand = MathUtils.getRandomNumberInRange(1 - SPLIT_SPEED_VARIATION, 1 + SPLIT_SPEED_VARIATION);
                        newProj.getVelocity().x *= rand;
                        newProj.getVelocity().y *= rand;

                        //Splits up the damage
                        newProj.setDamageAmount(projDamage / (float) SPLIT_COUNT * 1.25f);
                        ProximityFuseAIAPI AI = (ProximityFuseAIAPI) (newProj.getAI());
                        AI.updateDamage();

                        //Removes the original projectile
                        engine.removeEntity(proj);
                    }
                    cleanList.add(proj);
                    continue;
                }
            }

            //If this projectile is not loaded in memory, cleaning time!
            if (!engine.isEntityInPlay(proj)) {
                cleanList.add(proj);
            }
        }

        //Register all the split projectiles. Done here to avoid comodding
        alreadyRegisteredProjectiles.addAll(splitProjectiles);

        for (DamagingProjectileAPI proj : alreadyRegisteredProjectiles) {
            if (proj.getCustomData().containsKey("jdp_hitShield")) {
                hitShield = (boolean) proj.getCustomData().get("jdp_hitShield");
            }
        }

        //Runs the cleaning
        for (DamagingProjectileAPI proj : cleanList) {
            DamagingProjectileAPI explosion = (DamagingProjectileAPI) proj.getCustomData().get("jdp_createdExplosion");

            //The explosion has a lifetime of 0.12,
            // some delay here to register the hit and then clean the projectile that exploded from memory
            if (explosion == null || (explosion.getElapsed() > 0.1)) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        ShipAPI ship = weapon.getShip();
        Vector2f loc = projectile.getLocation();
        float projAngle = projectile.getFacing();

        //Spawns the new shot
        DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(ship, weapon, damageTypeMap.get(damageType), loc, projAngle, ship.getVelocity());
        alreadyRegisteredProjectiles.add(newProj);

        //Removes the original projectile
        engine.removeEntity(projectile);
    }



    @Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        DamagingProjectileAPI dataProj = projectile;
        if (damageResult.getType() == DamageType.OTHER) return;

        //If the projectile we're dealing with is an explosion, feed the data back through the submunition that exploded
        if (projectile.getCustomData().containsKey("jdp_explosionSource")) {
            dataProj = (DamagingProjectileAPI) projectile.getCustomData().get("jdp_explosionSource");
        }

        if (shieldHit) {
            dataProj.setCustomData("jdp_hitShield", true);
        } else {
            dataProj.setCustomData("jdp_hitShield", false);
        }

        //Piercing arcs
        String pierceProjName = KINETIC_WEAPON_NAME + "_shot";
        if (projectile.getProjectileSpecId() != null && projectile.getProjectileSpecId().contains(pierceProjName) && isShip(target)) {
            ShipAPI ship = (ShipAPI) target;
            float pierceChance = ((ShipAPI) target).getHardFluxLevel() - 0.1f;
            pierceChance *= (float) (0.5 * ship.getMutableStats().getDynamic().getValue(Stats.SHIELD_PIERCED_MULT));

            boolean piercedShield = shieldHit && (float) Math.random() < pierceChance;

            if (!shieldHit || piercedShield) {
                float emp = projectile.getEmpAmount();
                float dam = 0;
                engine.spawnEmpArcPierceShields(
                        projectile.getSource(), point, target, target,
                        DamageType.ENERGY,
                        dam, // damage
                        emp, // emp
                        100000f, // max range
                        "tachyon_lance_emp_impact",
                        10f, // thickness
                        PIERCE_COLOR,
                        new Color(200, 200, 230, 255)
                );
            }
        }
    }

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        //Links the explosion with the projectile that created it
        explosion.setCustomData("jdp_explosionSource", originalProjectile);
        originalProjectile.setCustomData("jdp_createdExplosion", explosion);
    }
}





