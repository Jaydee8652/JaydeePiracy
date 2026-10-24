package Jaydee8652.JaydeePiracy.scripts.skills;

import Jaydee8652.JaydeeProficiencies.patch.AutomatedHullmodPatch;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.ShipSkillEffect;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.sql.Timestamp;

import static Jaydee8652.JaydeePiracy.scripts.ai.jdp_relayAI.jdp_relayIcon;
import static Jaydee8652.JaydeeProficiencies.patch.AutomatedHullmodPatch.JDP_AUTOMATED_NO_PENALTY_TAGS;
import static com.fs.starfarer.api.impl.campaign.skills.CyberneticAugmentation.getNumEliteSkills;

public class jdp_cerebralsymbiance {
    public static String JDP_CEREBRAL_SYMBIANCE_TAG = "jdp_cerebralsymbiance_tag";
    public static class CerebralSymbianceTooltip implements AutomatedHullmodPatch.TooltipBuffer {
        @Override
        public void createTooltip(TooltipMakerAPI tooltip, ShipAPI ship) {
            var captain = ship.getCaptain();
            var h = Misc.getHighlightColor();
            TooltipMakerAPI icon = tooltip.beginImageWithText(captain.getPortraitSprite(), 100f);
            icon.addPara("%s has the %s skill, they are capable of perfectly interfacing with our fleet", 0f, h, captain.getName().getFullName(), "Cerebral Symbiance");
            icon.addSpacer(10f);
            icon.addPara("Any ship they are assigned to will no longer require specialised expertise to maintain", 0f, h);
            tooltip.addImageWithText(10f);
            tooltip.addSpacer(10f);
        }
    }
    static {
        JDP_AUTOMATED_NO_PENALTY_TAGS.put(JDP_CEREBRAL_SYMBIANCE_TAG, new CerebralSymbianceTooltip());
    }

    public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {
        public void apply(MutableShipStatsAPI stats, HullSize hullSize, String id, float level) {
            if (stats.getVariant() != null) stats.getVariant().addTag(JDP_CEREBRAL_SYMBIANCE_TAG);
        }

        public void unapply(MutableShipStatsAPI stats, HullSize hullSize, String id) {
            //Technically doesn't quite work all the time for nonsense reasons. Also unapplied in RefitApplicator
            stats.getVariant().removeTag(JDP_CEREBRAL_SYMBIANCE_TAG);
        }

        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            info.addPara("Any assigned ship does not require special expertise to maintain", hc, 0f);
        }

        public String getEffectDescription(float level) {
            return null;
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }
    }
}
