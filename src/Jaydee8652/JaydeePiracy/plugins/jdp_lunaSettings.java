package Jaydee8652.JaydeePiracy.plugins;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;

import javax.swing.*;
import java.awt.*;

public class jdp_lunaSettings {
    //Allow Shrouded Phase Ships
    public static Boolean jdp_shroudedPhase() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_shroudedPhase");
        }
        return value;
    }

    //Colony Items Global
    public static Boolean jdp_campaignColonyItemsGlobal() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignColonyItemsGlobal");
        }
        return value;
    }

    //Cryoarithmetic Engine
    public static Boolean jdp_campaignCryo() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignCryo");
        }
        return value;
    }
    //Biofactory Embryo
    public static Boolean jdp_campaignBiofactory() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignBiofactory");
        }
        return value;
    }
    //Soil Nanites
    public static Boolean jdp_campaignSoil() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignSoil");
        }
        return value;
    }
    //Mantle Bore
    public static Boolean jdp_campaignBore() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignBore");
        }
        return value;
    }
    //Orbital Fusion Lamp
    public static Boolean jdp_campaignLamp() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignLamp");
        }
        return value;
    }
    //Combat Drone Replicator
    public static Boolean jdp_campaignReplicator() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignReplicator");
        }
        return value;
    }
    //Dealmaker Holosuite
    public static Boolean jdp_campaignHolosuite() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignHolosuite");
        }
        return value;
    }
    //Hypershunt Tap
    public static Boolean jdp_campaignTap() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignTap");
        }
        return value;
    }
    //Plasma Dynamo
    public static Boolean jdp_campaignDynamo() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignDynamo");
        }
        return value;
    }
    //Fullerene Spool
    public static Boolean jdp_campaignFullerene() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignFullerene");
        }
        return value;
    }
    //Madoka Buffalo
    public static Boolean jdp_campaignMadoka() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignMadoka");
        }
        return value;
    }
    //Subsurface Ecosystem
    public static Boolean jdp_campaignSubsurface() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_campaignSubsurface");
        }
        return value;
    }
    //Flowerfish XO
    public static Boolean jdp_experimentalFlowerfish() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_experimentalFlowerfish");
        }
        return value;
    }
    //Terraformer Specialist
    public static Boolean jdp_experimentalTerraformer() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_experimentalTerraformer");
        }
        return value;
    }
    //Instrument Specialist
    public static Boolean jdp_experimentalInstrument() {
        Boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            value = LunaSettings.getBoolean("jaydeepiracy", "jdp_experimentalInstrument");
        }
        return value;
    }
}
