package io.github.picapi.mc.fabric.client.serverspecificskins.migrations.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.github.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerAddressUtilities;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSpecificSkins;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.Map;

public class ConfigMigrationManager {

    public static void applyMigrations() {
        // Get current config file
        ServerSpecificSkins.LOGGER.debug("Loading current config");
        JsonObject raw_config;
        try {
            raw_config = JsonParser.parseReader(new FileReader(ConfigManager.defaultConfigPath.toFile())).getAsJsonObject();
        } catch (FileNotFoundException e) {
            // Ignore - means there's no config to migrate.
            ServerSpecificSkins.LOGGER.debug("No existing config found - no need to migrate.");
            return;
        }
        Integer config_version;
        if (raw_config.get("config_version") == null) {
            config_version = 1;
        } else {
            config_version = raw_config.get("config_version").getAsInt();
        }
        if(config_version.equals(ConfigManager.currentConfigVersion)){
            // No migrations needed
            ServerSpecificSkins.LOGGER.debug("Config is current version, no need to migrate.");
            return;
        }
        // TODO: Move migrations into individual units, rather than being in this file
        if (config_version == 1) {
            ServerSpecificSkins.LOGGER.info("Applying migrations to Config Version 2...");
            // For each existing entry, convert server addresses to hashes.
            ServerSpecificSkins.LOGGER.info("Migrating configs and saved skins to new format...");
            JsonObject skin_types = raw_config.getAsJsonObject("skin_type");
            JsonObject migrated_skin_types = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : skin_types.entrySet()) {
                String old_value = entry.getKey();
                String new_value;
                new_value = new StringBuilder().append(old_value, 0, old_value.lastIndexOf("_")).append(":").append(old_value.substring(old_value.lastIndexOf("_") + 1)).toString();
                new_value = ServerAddressUtilities.hashServerAddress(new_value);
                migrated_skin_types.addProperty(new_value, entry.getValue().toString());
                // Try to rename files while doing so.
                File target_file = FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/"+entry.getKey()+".png").toFile();
                if (target_file.exists()){
                    target_file.renameTo(FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/"+new_value+".png").toFile());
                }
                ServerSpecificSkins.LOGGER.info("Converted " + old_value + " to " + new_value +".");
            }
            raw_config.add("skin_type", migrated_skin_types);
            ServerSpecificSkins.LOGGER.info("Migrated to Config Version 2 successfully.");
            config_version = 2;
        }
        raw_config.addProperty("config_version", config_version);
        // Save migrated configuration
        try {
            ServerSpecificSkins.LOGGER.debug("Saving config...");
            FileWriter writer = new FileWriter(ConfigManager.defaultConfigPath.toFile());
            Gson gson = new Gson();
            gson.toJson(raw_config, writer);
            writer.close();
            ServerSpecificSkins.LOGGER.debug("Config saved.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
