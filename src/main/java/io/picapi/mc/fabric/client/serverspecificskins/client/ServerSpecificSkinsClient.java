package io.picapi.mc.fabric.client.serverspecificskins.client;

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
            var new_file = ServerSpecificSkinsClient.getFileForAddress(server.address);
            var dir = new_file.getParentFile();
            if (!dir.isDirectory()){if (!dir.mkdirs()){throw new IOException("Could not create directory for skin files.");}}
            Files.copy(skin.toPath(),new_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void saveSkinForServer(ServerAddress server, byte[] skin_data) throws IOException {
        var new_file = ServerSpecificSkinsClient.getFileForAddress(server);
        var dir = new_file.getParentFile();
        if (!dir.isDirectory()){if (!dir.mkdirs()){throw new IOException("Could not create directory for skin files.");}}
        Files.write(new_file.toPath(), skin_data);
    }

    public static void deleteSkinForServer(ServerInfo server) {
        var new_file = ServerSpecificSkinsClient.getFileForAddress(server.address);
        if (new_file.isFile()) {
            new_file.delete();
        }
    }

    public static File getFileForAddress(ServerAddress address){
        return getFileForAddress(address.getAddress()+":"+ address.getPort());
    }
    public static File getFileForAddress(String address){
        String fixed_address;
        if (address.contains(":")){
            fixed_address = address.replace(':', '_');
        } else {
            fixed_address = address + "_25565";
        }
        return FabricLoader.getInstance().getConfigDir().resolve("server_specific_skins/"+fixed_address+".png").toFile();
    }
}
