package io.github.picapi.mc.fabric.client.serverspecificskins;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;

public class Installer {

    public static void main(String[] args) throws IOException, URISyntaxException {
        int optionSelected = JOptionPane.showConfirmDialog(null,
                "This JAR file is a Fabric Mod, designed to be placed within a mods folder.\nWould you like to install ServerSpecificSkins now?",
                "ServerSpecificSkins", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (optionSelected == JOptionPane.YES_OPTION){
            String targetPath;
            // Load a list of profiles from the user's minecraft launcher
            String os_name = System.getProperty("os.name");
            if(os_name.startsWith("Windows")){
                targetPath = System.getProperty("user.home")+"/AppData/Roaming/.minecraft/";
            } else if (os_name.startsWith("Mac")) {
                targetPath = System.getProperty("user.home")+"/Library/Application Support/minecraft/";
            } else if (os_name.startsWith("Linux") || os_name.startsWith("LINUX")){
                targetPath = System.getProperty("user.home")+"/.minecraft/";
            } else {
                JOptionPane.showMessageDialog(null, "Could not detect operating system. Please install manually.",
                        "ServerSpecificSkins", JOptionPane.ERROR_MESSAGE);
                return;
            }
                JsonObject object = JsonParser.parseString(Files.readString(Path.of(targetPath,"launcher_profiles.json"))).getAsJsonObject();
                JsonObject profileList = object.getAsJsonObject("profiles");
                HashMap<String, JsonObject> options = new HashMap<>();

            for (String profileName : profileList.keySet()) {
                JsonObject profileValue = profileList.getAsJsonObject(profileName);
                String lastVersionId = profileValue.get("lastVersionId").getAsString();
                if (lastVersionId.startsWith("fabric") && lastVersionId.endsWith("1.19.3")) {
                    options.put(profileValue.get("name").getAsString() + " (" + profileName + ")", profileValue);
                }
            }
                if(options.size() == 0){
                    JOptionPane.showMessageDialog(null, "No suitable profiles found. Please make sure you have installed a version of Fabric for Minecraft 1.19.3.",
                            "ServerSpecificSkins", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String selection = (String)JOptionPane.showInputDialog(null,
                        "Choose the profile you wish to install ServerSpecificSkins for.",
                        "ServerSpecificSkins",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options.keySet().toArray(),
                        options.keySet().toArray()[0]
                );
                if (selection == null){
                    return;
                }
                JsonObject selectedProfile = options.get(selection);
                JsonElement gameDirEle = selectedProfile.get("gameDir");
                String gameDir;
                if(gameDirEle == null){
                    gameDir = targetPath;
                } else {
                    gameDir = gameDirEle.getAsString();
                }
                Path modsFolder = Path.of(gameDir,"/mods/");
                try {
                    Files.createDirectories(modsFolder);
                    List<Path> existingFiles = Files.list(modsFolder).filter(f -> f.getFileName().toString().startsWith("ServerSpecificSkins")).toList();
                    if (existingFiles.size() > 0){
                        int choice = JOptionPane.showConfirmDialog(
                                null,
                                "A version of this mod is already installed - would you like to overwrite it?",
                                "ServerSpecificSkins",
                                JOptionPane.YES_NO_OPTION
                        );
                        if(choice == JOptionPane.YES_OPTION){
                            existingFiles.forEach((p) -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else {
                            return;
                        }
                    }
                    Path jarPath = Path.of(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    Files.copy(jarPath, modsFolder.resolve(jarPath.getFileName().toString()));
                } catch (IOException e){
                    JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(),
                            "ServerSpecificSkins", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            JOptionPane.showMessageDialog(null, "Installed successfully!",
                    "ServerSpecificSkins", JOptionPane.INFORMATION_MESSAGE);
            }
    }
}
