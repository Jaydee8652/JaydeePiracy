package Jaydee8652.JaydeePiracy.hullmods;

import Jaydee8652.JaydeePiracy.utils.refl.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import Jaydee8652.JaydeePiracy.utils.jdp_Items.RELAY_DRONE_NAME;
import Jaydee8652.JaydeeProficiencies.utils.refl.ReflectionUtilis

//Made with help from @combustiblemon
//Thanks!

class jdp_mercenaryRelayHelper: BaseHullMod() {
    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        if (stats?.variant == null) {
            return
        }

        val variant = stats.variant

        // If there is no hullmod, unapply
        // If there is no fleet, then there is no commander, so they cannot have the skill, unapply
        // If there is a fleet, but there is no commander, they cannot have the skill, unapply
        // If they don't have the skill, unapply
        if (!variant.hasHullMod("jdp_mercenaryRelay") || stats.fleetMember == null || stats.fleetMember.fleetCommander == null || !stats.fleetMember.fleetCommander.hasTag("jdp_flowerfishWithTheNetwork")) {
            if (variant.hullSpec.tags.contains("jdp_mercenaryRelayHelper")) {
                val hullId = variant.hullSpec.hullId

                val originalHullSpec = Global.getSettings().getHullSpec(hullId)
                if (originalHullSpec != null) {
                    val oldWings = variant.nonBuiltInWings.toList()
                    variant.wings.clear()

                    variant.setHullSpecAPI(originalHullSpec)
                    variant.refreshBuiltInWings()

                    for (wing in oldWings) {
                        if (wing != RELAY_DRONE_NAME) variant.wings.add(wing)
                    }
                }
            }
            return
        }

        if (variant.hullSpec.tags.contains("jdp_mercenaryRelayHelper")) return

        var newSpec: ShipHullSpecAPI?
        val hullSpec = variant.hullSpec

        newSpec = ReflectionUtils.invoke("clone", hullSpec) as ShipHullSpecAPI

        newSpec.addTag("jdp_mercenaryRelayHelper")

        val intFields = ReflectionUtils.getFieldsOfType(newSpec, newSpec.fighterBays.javaClass)

        val newFighterBayNumber = newSpec.fighterBays + 1

        for (field in intFields) {
            val fieldValue = ReflectionUtils.get(field, newSpec)
            if (fieldValue is Int && fieldValue == hullSpec.fighterBays) {
                if (newSpec.fighterBays == newFighterBayNumber) {
                    break
                }

                ReflectionUtils.set(field, newSpec, newFighterBayNumber)
                if (newSpec.fighterBays == newFighterBayNumber) {
                    break
                }

                ReflectionUtils.set(field, newSpec, newFighterBayNumber - 1)
            }
        }

        newSpec.builtInWings.add(RELAY_DRONE_NAME)

        val oldWings = variant.nonBuiltInWings.toList()
        variant.wings.clear()

        stats.numFighterBays.modifyFlat("jdp_mercenaryRelayHelper", 1f)
        variant.setHullSpecAPI(newSpec)
        variant.refreshBuiltInWings()

        for (wing in oldWings) {
            if (wing != RELAY_DRONE_NAME) variant.wings.add(wing)
        }
    }
}