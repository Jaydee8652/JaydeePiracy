package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.impl.hullmods.ShardSpawner;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Jaydee8652.JaydeeProficiencies.specialists.jdp_SpecialistUtils.getPersonAdmin;
import static Jaydee8652.JaydeeProficiencies.utils.campaign.jdp_TerraformingProcessUtils.addTerraformingDisplay;
import static com.fs.starfarer.api.impl.hullmods.ShardSpawner.variantData;
public class jdp_materielsubsumption {
	public static Map<HullSize, Float> HULLSIZE_MULT = new HashMap<>();
	static {
		HULLSIZE_MULT.put(HullSize.FIGHTER, 0f);
		HULLSIZE_MULT.put(HullSize.FRIGATE, 0.25f);
		HULLSIZE_MULT.put(HullSize.DESTROYER, 0.5f);
		HULLSIZE_MULT.put(HullSize.CRUISER, 0.75f);
		HULLSIZE_MULT.put(HullSize.CAPITAL_SHIP, 1f);
	}

	public static class ShardFadeInPlugin extends BaseEveryFrameCombatPlugin {
		float elapsed = 0f;
		ShipAPI [] ships = null;
		CollisionClass collisionClass;

		String variantId;
		ShipAPI source;
		ShipAPI owner;
		float fadeInTime;
		float angle;

		public ShardFadeInPlugin(String variantId, ShipAPI source, ShipAPI owner, float fadeInTime, float angle) {
			this.variantId = variantId;
			this.source = source;
			this.owner = owner;
			this.fadeInTime = fadeInTime;
			this.angle = angle;

		}


		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			if (Global.getCombatEngine().isPaused()) return;

			elapsed += amount;
			CombatEngineAPI engine = Global.getCombatEngine();

			if (ships == null) {
				float facing = source.getFacing() + 15f * ((float) Math.random() - 0.5f);

				Vector2f loc = Misc.getUnitVectorAtDegreeAngle(angle);
				loc.scale(source.getCollisionRadius() * 0.1f);
				Vector2f.add(loc, source.getLocation(), loc);

				CombatFleetManagerAPI fleetManager = engine.getFleetManager(owner.getOriginalOwner());
				boolean wasSuppressed = fleetManager.isSuppressDeploymentMessages();
				fleetManager.setSuppressDeploymentMessages(true);

				FighterWingSpecAPI spec = Global.getSettings().getFighterWingSpec(variantId);
				ships = new ShipAPI[spec.getNumFighters()];

				PersonAPI captain = Global.getSettings().createPerson();
				captain.setPersonality(Personalities.RECKLESS);
				captain.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
				captain.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
				captain.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);

				ShipAPI leader = engine.getFleetManager(owner.getOriginalOwner()).spawnShipOrWing(variantId, loc, facing, 0f, captain);
				for (int i = 0; i < ships.length; i++) {
					ships[i] = leader.getWing().getWingMembers().get(i);
					ships[i].getLocation().set(loc);
				}
				collisionClass = ships[0].getCollisionClass();

				for (int i = 0; i < ships.length; i++) {
					ships[i].cloneVariant();
					ships[i].getVariant().addTag(Tags.SHIP_LIMITED_TOOLTIP);

					FactionAPI faction = Global.getSector().getFaction(Factions.OMEGA);
					if (faction != null) {
						String name = faction.pickRandomShipName();
						ships[i].setName(name);
					}
				}
				fleetManager.setSuppressDeploymentMessages(wasSuppressed);
				collisionClass = ships[0].getCollisionClass();
			}

			float progress = elapsed / fadeInTime;
			if (progress > 1f) progress = 1f;

			for (int i = 0; i < ships.length; i++) {
				ShipAPI ship = ships[i];
				ship.setAlphaMult(progress);

				if (progress < 0.5f) {
					ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
					ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT);
					ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT);
					ship.blockCommandForOneFrame(ShipCommand.STRAFE_LEFT);
					ship.blockCommandForOneFrame(ShipCommand.STRAFE_RIGHT);
				}

				ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
				ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
				ship.blockCommandForOneFrame(ShipCommand.FIRE);
				ship.blockCommandForOneFrame(ShipCommand.PULL_BACK_FIGHTERS);
				ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
				ship.setHoldFireOneFrame(true);
				ship.setHoldFire(true);

				ship.setCollisionClass(CollisionClass.NONE);
				ship.getMutableStats().getHullDamageTakenMult().modifyMult("ShardSpawnerInvuln", 0f);
				if (progress < 0.5f) {
					ship.getVelocity().set(source.getVelocity());
				} else if (progress > 0.75f){
					ship.setCollisionClass(collisionClass);
					ship.getMutableStats().getHullDamageTakenMult().unmodifyMult("ShardSpawnerInvuln");
				}

				float jitterLevel = progress;
				if (jitterLevel < 0.5f) {
					jitterLevel *= 2f;
				} else {
					jitterLevel = (1f - jitterLevel) * 2f;
				}

				float jitterRange = 1f - progress;
				float maxRangeBonus = 50f;
				float jitterRangeBonus = jitterRange * maxRangeBonus;
				Color c = ShardSpawner.JITTER_COLOR;

				ship.setJitter(this, c, jitterLevel, 25, 0f, jitterRangeBonus);
			}

			if (elapsed > fadeInTime) {
				for (int i = 0; i < ships.length; i++) {
					ShipAPI ship = ships[i];
					ship.setAlphaMult(1f);
					ship.setHoldFire(false);
					ship.setCollisionClass(collisionClass);
					ship.getMutableStats().getHullDamageTakenMult().unmodifyMult("ShardSpawnerInvuln");
				}
				engine.removePlugin(this);
			}
		}
	}

	public static int SPLIT_WEIGHT = 1;
	public static float AI_BONUS = 2f;

	public static float SPAWN_TIME = 4f;
	public static String DATA_KEY = "jdp_subsumption_shard_spawner_data_key";

	public static class jdp_materielsubsumptionDMG implements HullDamageAboutToBeTakenListener {
		private final int side;
		private final String id;

		public jdp_materielsubsumptionDMG(int side, String id) {
			this.side = side;
			this.id = id.replace("_ship_0", "");
		}

		@Override
		public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
			if (param instanceof ShipAPI killer) {
				var engine = Global.getCombatEngine();
				if (killer.isFighter() && killer.getWing() !=null && killer.getWing().getSourceShip() != null) killer = killer.getWing().getSourceShip();
				if (killer.getCaptain() == null || !killer.getCaptain().getStats().hasSkill(id)) return false;

				if (ship.hasTag("jdp_subsumed")) return false;
				if (ship.getVariant().hasTag("omega") || ship.getVariant().hasTag("mess") || ship.getVariant().hasTag("mess_remnant")) return false;

                if (ship.isFighter()) return false;
				if (ship.getOwner() == side) return false;

				if (killer.getParentStation() != null) killer = killer.getParentStation();
				
				if (!ship.isHulk() && engine.isEntityInPlay(ship) && ship.getHitpoints() <= 0) {
					ship.addTag("jdp_subsumed");

					var chance = HULLSIZE_MULT.get(ship.getHullSize());
					if (Misc.isAutomated(ship.getMutableStats())) chance *= AI_BONUS;

					var weight = 0;
					if (chance > 1) {
						weight += SPLIT_WEIGHT;
						chance -= 1;
					}
					if (Math.random() < chance) weight += SPLIT_WEIGHT;

					if (SPLIT_WEIGHT > 0) {
						ShardSpawner ShardSpawner = new ShardSpawner();

						engine.setCombatNotOverForAtLeast(SPAWN_TIME + 1f);

						if (!ship.isHulk() || !engine.isEntityInPlay(ship)) return false;
						WeightedRandomPicker<ShardSpawner.ShardType> typePicker = ShardSpawner.getTypePickerBasedOnLocalConditions(ship);

						int spawnAnglesIter = 0;
						float angleOffset = (float) Math.random() * 360f;

						float addedWeight = 0f;
						while (addedWeight < weight) {
							com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardType type = typePicker.pick();

							com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardTypeVariants variants = variantData.get(HullSize.FIGHTER);
							WeightedRandomPicker<String> variantPicker = variants.get(type);
							String variant = variantPicker.pick();


							if (variant != null) {
								WeightedRandomPicker<Float> spawnAngles = ShardSpawner.getSpawnAngles(spawnAnglesIter++);
								var shard = new ShardFadeInPlugin(variant, ship, killer, SPAWN_TIME, spawnAngles.pickAndRemove() + angleOffset);

								Global.getCombatEngine().addPlugin(shard);
								addedWeight += 1f;
							} else {
								addedWeight += 0.1f; // if we didn't manage to add anything, eventually break out of the loop
							}
						}
					}
				}
			}
			return false;
		}
	}

	public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
		public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
			ShipAPI ship = (ShipAPI) stats.getEntity();
			CombatEngineAPI engine = Global.getCombatEngine();

			if (engine == null || ship == null) return;

			if (engine.getListenerManager().getListeners(jdp_materielsubsumptionDMG.class).stream().noneMatch(l -> ((jdp_materielsubsumptionDMG) l).side == ship.getOwner())) {
				engine.getListenerManager().addListener(new jdp_materielsubsumptionDMG(ship.getOwner(), id));
			}
		}

		public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
		}

		@Override
		public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
			info.addPara("Disabling an enemy ship has a %s/%s/%s/%s chance to spawn a wing of Aspect-class fighters depending on hull size", 0f, Misc.getHighlightColor(), Misc.getHighlightColor(), Math.round(100 * HULLSIZE_MULT.get(HullSize.FRIGATE)) + "%", Math.round(100 * HULLSIZE_MULT.get(HullSize.DESTROYER)) + "%", Math.round(100 * HULLSIZE_MULT.get(HullSize.CRUISER)) + "%", Math.round(100 * HULLSIZE_MULT.get(HullSize.CAPITAL_SHIP)) + "%");
			info.addPara(BaseIntelPlugin.BULLET+"If the enemy ship is an automated ship, the chance is increased to %s/%s/%s/%s", 0f, Misc.getTextColor(), Misc.getHighlightColor(), Math.round(AI_BONUS * 100 * HULLSIZE_MULT.get(HullSize.FRIGATE)) + "%", Math.round(AI_BONUS * 100 * HULLSIZE_MULT.get(HullSize.DESTROYER)) + "%", Math.round(AI_BONUS * 100 * HULLSIZE_MULT.get(HullSize.CRUISER)) + "%", Math.round(AI_BONUS * 100 * HULLSIZE_MULT.get(HullSize.CAPITAL_SHIP)) + "%");
			info.addPara(BaseIntelPlugin.BULLET+"Chances higher than %s can spawn an additional wing at the chance of the excess", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "100%");

			info.addSpacer(10f);
			float HEIGHT = 55f;
			float PAD = 10f;

			TooltipMakerAPI relay = info.beginImageWithText(Global.getSettings().getHullSpec("aspect").getSpriteName(), HEIGHT);
			relay.addPara("Aspect-class Fighter", 0f, Misc.getHighlightColor(), "Aspect-class Fighter");
			relay.addPara("  -Pilot has Elite Gunnery Implants, Elite Point Defense and Elite Impact Mitigation", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Elite Gunnery Implants", "Elite Point Defense", "Elite Impact Mitigation");
			relay.addPara("  -Variant is selected based on the composition of the enemy fleet", 0f, Misc.getTextColor());
			info.addImageWithText(PAD);
			info.addSpacer(10f);

		}

		public String getEffectPerLevelDescription() {
			return null;
		}

		public ScopeDescription getScopeDescription() {
			return ScopeDescription.PILOTED_SHIP;
		}
	}
}