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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        if (config_version.equals(ConfigManager.currentConfigVersion)) {
            // No migrations needed
            ServerSpecificSkins.LOGGER.debug("Config is current version, no need to migrate.");
            return;
        }
        // TODO: Move migrations into individual units, rather than being in this file
        if (config_version == 1) {
            ServerSpecificSkins.LOGGER.info("Applying migrations to Config Version 2...");
            ServerSpecificSkins.LOGGER.info("Migrating configs and saved skins to new format...");
            // Load the skin_type section of the config
            JsonObject skin_types = raw_config.getAsJsonObject("skin_type");
            // Load servers.dat
            NbtCompound server_list;
            try {
                server_list = NbtIo.read(FabricLoader.getInstance().getGameDir().resolve("servers.dat").toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NbtList servers = server_list.getList("servers", NbtCompound.COMPOUND_TYPE);
            // Create a new object for the migrated skin types
            JsonObject migrated_skin_types = new JsonObject();
            // Create a list for old paths, so we can delete later.
            ArrayList<Path> oldPaths = new ArrayList<>();
            for (int i = 0; i <= servers.size(); i++) {
                NbtCompound server = servers.getCompound(i);
                // Get the IP address, old style filename, and generate new style filename.
                String ip_addr = server.getString("ip");
                String hashed_name_and_ip = ServerAddressUtilities.hashServerAddress(server.getString("name") + "|" + ip_addr);
                String old_format_address;
                if (ip_addr.contains(":")) {
                    old_format_address = ip_addr.replace(':', '_');
                } else {
                    old_format_address = ip_addr + "_25565";
                }
                if (skin_types.get(old_format_address) == null){
                    // Skip this part of the loop, since there's no skin associated with the address.
                    continue;
                }
                // Add the correct record to the migrated json object
                migrated_skin_types.add(hashed_name_and_ip, skin_types.get(old_format_address));
                // Copy over old skin into new style so it continues to work
                Path old_set_skin = FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/" + old_format_address + ".png");
                if (old_set_skin.toFile().exists()) {
                    try {
                        Files.copy(old_set_skin, FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/" + hashed_name_and_ip + ".png"));
                    } catch (IOException e) {
                        ServerSpecificSkins.LOGGER.error("Failed to copy " + old_set_skin + " to path " + FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/" + hashed_name_and_ip + ".png"), e);
                    }
                    oldPaths.add(old_set_skin);
                }
                ServerSpecificSkins.LOGGER.info("Converted " + server.getString("name") + "|" + ip_addr + " to " + hashed_name_and_ip + ".");
            }
            // Cleanup by deleting old skins
            ServerSpecificSkins.LOGGER.info("Deleting old skins...");
            for (Path path : oldPaths) {
                if (path.normalize().startsWith(FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/"))) {
                    path.toFile().delete();
                    ServerSpecificSkins.LOGGER.info("Deleted " + path);
                } else {
                    ServerSpecificSkins.LOGGER.error("Did not delete" + path + ", it appears to be outside the config directory somehow");
                }
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
