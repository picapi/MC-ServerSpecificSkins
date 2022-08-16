package io.picapi.mc.fabric.client.serverspecificskins;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {

    static Gson gson = new Gson();
    public static class Config {
        Map<String,ServerSkinSettingType> skin_type;

        Config(){
            skin_type = new HashMap<>();
        }

        public ServerSkinSettingType getSkinTypeForAddress(String address){
            return skin_type.getOrDefault(address,ServerSkinSettingType.CLASSIC);
        }

        public void setSkinTypeForAddress(String address, ServerSkinSettingType type){
            skin_type.put(address, type);
        }
    }

    static Path defaultConfigPath = FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/config.json");



    public static Config getConfig() {
        try {
            Config config = gson.fromJson(new FileReader(defaultConfigPath.toFile()), Config.class);
            return Objects.requireNonNullElseGet(config, Config::new);
        } catch (FileNotFoundException e) {
            return new Config();
        }
    }

    public static void saveConfig(Config new_config) throws IOException {
        FileWriter writer = new FileWriter(defaultConfigPath.toFile());
        gson.toJson(new_config, writer);
        writer.close();
    }

}
