package io.github.picapi.mc.fabric.client.serverspecificskins.client;

import io.github.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerAddressUtilities;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSkinSettingType;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ServerSkinManager {

    File selectedSkin;
    Boolean clearSkinForServer;

    ButtonWidget.Builder clearSkinBuilder;
    ButtonWidget.Builder setSkinBuilder;
    ArrayList<ButtonWidget> activeClearButtons = new ArrayList<>();
    CyclingButtonWidget.Builder<ServerSkinSettingType> skinTypeBuilder;

    public ServerSkinManager(ServerInfo server){
        ConfigManager.Config config = ConfigManager.getConfig();
        setSkinBuilder = ButtonWidget.builder(Text.translatable("serverspecificskins.addServer.skinConfig.setSkin"),new SelectSkinPressAction());
        clearSkinBuilder = ButtonWidget.builder(Text.translatable("serverspecificskins.addServer.skinConfig.clearSkin"),new ClearSkinPressAction());
        skinTypeBuilder = CyclingButtonWidget.builder(ServerSkinSettingType::getText)
                .values(ServerSkinSettingType.values())
                .initially(config.getSkinTypeForAddress(ServerAddressUtilities.stringify(server)));
        clearSkinForServer = false;
    }

    public ButtonWidget buildSetSkinButton(int x,int y, int width, int height){
        return setSkinBuilder.dimensions(x,y,width,height).build();
    }

    public ButtonWidget buildClearSkinButton(int x,int y, int width, int height){
        ButtonWidget clearButton = clearSkinBuilder.dimensions(x,y,width,height).build();
        activeClearButtons.add(clearButton);
        return clearButton;
    }

    public CyclingButtonWidget<ServerSkinSettingType> buildSkinTypeButton(int x,int y, int width, int height){
       return skinTypeBuilder.build(x,y,width,height,Text.translatable("serverspecificskins.addServer.skinType"));
    }

    public boolean shouldClearSkin(){
        return clearSkinForServer;
    }

    public File getSelectedSkin(){
        return selectedSkin;
    }

    public class SelectSkinPressAction implements ButtonWidget.PressAction {
        ButtonWidget clearSkinButton;

        public void setClearButton(ButtonWidget button){
            clearSkinButton = button;
        }

        public void onPress(ButtonWidget b){
            try {
                selectedSkin = ServerSpecificSkinsClient.selectSkin();
                if(selectedSkin != null) {
                    clearSkinForServer = false;
                }
                for (ButtonWidget button : activeClearButtons) {
                    button.active = true;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        }

    public class ClearSkinPressAction implements ButtonWidget.PressAction {

        public void onPress(ButtonWidget b){
            clearSkinForServer = true;
            for (ButtonWidget button : activeClearButtons) {
                button.active = false;
            }
        }
    }
}
