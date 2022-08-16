package io.picapi.mc.fabric.client.serverspecificskins.client.mixin;

import io.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import io.picapi.mc.fabric.client.serverspecificskins.ServerAddressUtilities;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import io.picapi.mc.fabric.client.serverspecificskins.ServerSkinSettingType;
import io.picapi.mc.fabric.client.serverspecificskins.client.ServerSpecificSkinsClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.gui.screen.AddServerScreen;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Mixin(AddServerScreen.class)
public class ServerSettingsScreenMixin extends Screen {
    @Shadow @Final private ServerInfo server;
    private static final net.minecraft.text.Text CUSTOM_SKIN_SELECT_TEXT = Text.translatable("serverspecificskins.addServer.skinConfig");

    protected ServerSettingsScreenMixin(Text title) {
        super(title);
    }

    ButtonWidget clearSkinButton;
    ButtonWidget setSkinButton;

    CyclingButtonWidget<ServerSkinSettingType> skinTypeButton;
    File prospectiveSkin;
    boolean skinForRemoval = false;

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo info) {
        ConfigManager.Config config = ConfigManager.getConfig();
        setSkinButton = new ButtonWidget(this.width / 2 - 160, this.height / 4 + 72 + 20, 120, 20 , Text.translatable("serverspecificskins.addServer.skinConfig.setSkin"),(button) -> {
            try {
                prospectiveSkin = ServerSpecificSkinsClient.selectSkin();
                if(prospectiveSkin != null) {
                    clearSkinButton.active = true;
                    skinForRemoval = false;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        clearSkinButton = new ButtonWidget(this.width/2 + 40,this.height / 4 + 72 + 20,120,20, Text.translatable("serverspecificskins.addServer.skinConfig.clearSkin"),(button) -> {
            skinForRemoval = true;
            clearSkinButton.active = false;
        });
        clearSkinButton.active = ServerSpecificSkinsClient.getFileForAddress(this.server.address).isFile();
        skinTypeButton = CyclingButtonWidget.builder(ServerSkinSettingType::getText)
                .values(ServerSkinSettingType.values())
                .initially(config.getSkinTypeForAddress(ServerAddressUtilities.stringify(this.server)))
                .build(
                        this.width / 2 - 35, this.height / 4 + 72 + 20,70,20, Text.translatable("serverspecificskins.addServer.skinType")
        );
        this.addDrawableChild(setSkinButton);
        this.addDrawableChild(clearSkinButton);
        this.addDrawableChild(skinTypeButton);
    }

    @Inject(at = @At("TAIL"), method = "addAndClose")
    private void saveSkin(CallbackInfo info) throws IOException {
        if (skinForRemoval){
            ServerSpecificSkinsClient.deleteSkinForServer(this.server);
        }
        else if (prospectiveSkin != null) {
            ServerSpecificSkinsClient.saveSkinForServer(this.server, prospectiveSkin);
        }
        ConfigManager.Config config = ConfigManager.getConfig();
        config.setSkinTypeForAddress(ServerAddressUtilities.stringify(this.server),skinTypeButton.getValue());
        ConfigManager.saveConfig(config);
    }


}
