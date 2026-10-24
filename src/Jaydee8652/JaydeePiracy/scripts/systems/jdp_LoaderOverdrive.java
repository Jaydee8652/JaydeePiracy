package Jaydee8652.JaydeePiracy.scripts.systems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class jdp_LoaderOverdrive extends BaseShipSystemScript {

    public static float EFFECT = 0f;
    public static final float FLUX_REDUCTION = 50f;


    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        final ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        if (state == State.ACTIVE) {
            EFFECT = EFFECT + 0.01f;
        }

        float mult = 1f + EFFECT * effectLevel;
        stats.getBallisticRoFMult().modifyMult(id, mult);
        stats.getBallisticAmmoRegenMult().modifyMult(id, mult);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 0.5f);


        stats.getWeaponMalfunctionChance().modifyFlat(id,EFFECT * 0.075f);
        stats.getCriticalMalfunctionChance().modifyMult(id, 0);
        stats.getMaxRecoilMult().modifyMult(id, mult);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 2 * mult);

    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getWeaponMalfunctionChance().unmodify(id);
        stats.getCriticalMalfunctionChance().unmodify(id);
        stats.getMaxRecoilMult().unmodify(id);
        stats.getCombatWeaponRepairTimeMult().unmodify(id);

        EFFECT = 0;
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float mult = (EFFECT * effectLevel) * 100;
        float malfunction = (float) (mult * 0.075);

        if (index == 0) {
            return new StatusData("ballistic ammo regeneration and rate of fire +" + (int) mult + "%", false);
        }
        if (index == 1) {
            return new StatusData("weapon non-critical malfunction chance +" + (int) malfunction + "%", true);
        }
        return null;
    }
}
