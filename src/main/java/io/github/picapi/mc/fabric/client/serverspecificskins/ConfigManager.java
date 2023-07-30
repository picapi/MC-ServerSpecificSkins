package io.github.picapi.mc.fabric.client.serverspecificskins;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {

    static Gson gson = new Gson();
    public static class Config {
        Map<String,ServerSkinSettingType> skin_type;
        Integer config_version;

        Config(){
            skin_type = new HashMap<>();
            config_version = currentConfigVersion;
        }

        public ServerSkinSettingType getSkinTypeForHash(String hash){
            return skin_type.getOrDefault(hash,ServerSkinSettingType.CLASSIC);
        }

        public ServerSkinSettingType getSkinTypeForServer(ServerInfo address){
            return getSkinTypeForHash(ServerAddressUtilities.hashServerAddress(address));
        }

        public void setSkinTypeForHash(String hash, ServerSkinSettingType type){
            skin_type.put(hash, type);
        }

        public void setSkinTypeForServer(ServerInfo address, ServerSkinSettingType type){
            setSkinTypeForHash(ServerAddressUtilities.hashServerAddress(address), type);
        }
    }

    public static final Path defaultConfigPath = FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/config.json");
    public static final Integer currentConfigVersion = 2;


    public static Config getConfig() {
        try {
            Config config = gson.fromJson(new FileReader(defaultConfigPath.toFile()), Config.class);
            return Objects.requireNonNullElseGet(config, Config::new);
        } catch (FileNotFoundException e) {
            return new Config();
        }
    }

    public static void saveConfig(Config new_config) throws IOException {
        Files.createDirectories(defaultConfigPath.getParent());
        FileWriter writer = new FileWriter(defaultConfigPath.toFile());
        gson.toJson(new_config, writer);
        writer.close();
    }

}
