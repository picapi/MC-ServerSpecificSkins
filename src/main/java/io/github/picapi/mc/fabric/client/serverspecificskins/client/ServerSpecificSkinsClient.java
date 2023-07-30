package io.github.picapi.mc.fabric.client.serverspecificskins.client;

import io.github.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerAddressUtilities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import static org.lwjgl.system.MemoryStack.stackPush;

@Environment(EnvType.CLIENT)
public class ServerSpecificSkinsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

    }

    public static File selectSkin() throws IOException {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer aFilterPatterns = stack.mallocPointer(1);

            aFilterPatterns.put(stack.UTF8("*.png"));

            aFilterPatterns.flip();

            String file_path = TinyFileDialogs.tinyfd_openFileDialog("Choose a skin...",null,aFilterPatterns,null, false);

            if (file_path != null) {
                File file = new File(file_path);
                if (file.isFile()){
                    return file;
                }
            }
            return null;
        }
    }

    public static void saveSkinForServer(ServerInfo server, File skin) throws IOException {
        if (skin.isFile()){
            var new_file = ServerSpecificSkinsClient.getFileForServer(server);
            var dir = new_file.getParentFile();
            if (!dir.isDirectory()){if (!dir.mkdirs()){throw new IOException("Could not create directory for skin files.");}}
            Files.copy(skin.toPath(),new_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void saveSkinForServer(ServerInfo server, byte[] skin_data) throws IOException {
        var new_file = ServerSpecificSkinsClient.getFileForServer(server);
        var dir = new_file.getParentFile();
        if (!dir.isDirectory()){if (!dir.mkdirs()){throw new IOException("Could not create directory for skin files.");}}
        Files.write(new_file.toPath(), skin_data);
    }

    public static void deleteSkinForServer(ServerInfo server) throws IOException {
        var new_file = ServerSpecificSkinsClient.getFileForServer(server);
        if (new_file.isFile()) {
            if (!new_file.delete()){throw new IOException();}
        }
        ConfigManager.Config config = ConfigManager.getConfig();
        config.removeServerFromConfig(server);
        ConfigManager.saveConfig(config);
    }

    public static File getFileForServer(ServerInfo server){
        String fixed_address = ServerAddressUtilities.hashServerAddress(server);
        return FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/"+fixed_address+".png").toFile();
    }
}
