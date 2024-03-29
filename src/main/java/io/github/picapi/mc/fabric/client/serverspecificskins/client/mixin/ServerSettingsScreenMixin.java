package io.github.picapi.mc.fabric.client.serverspecificskins.client.mixin;

import io.github.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerAddressUtilities;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.ServerSkinManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSkinSettingType;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.ServerSpecificSkinsClient;
import org.apache.logging.log4j.core.jmx.Server;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraft.client.gui.screen.AddServerScreen;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Objects;

@Mixin(AddServerScreen.class)
public class ServerSettingsScreenMixin extends Screen {
    @Shadow @Final private ServerInfo server;
    @Shadow private TextFieldWidget addressField;
    @Shadow private TextFieldWidget serverNameField;
    private static final net.minecraft.text.Text CUSTOM_SKIN_SELECT_TEXT = Text.translatable("serverspecificskins.addServer.skinConfig");

    protected ServerSettingsScreenMixin(Text title) {
        super(title);
    }

    ServerSkinManager manager;
    ButtonWidget setSkinButton;
    ButtonWidget clearSkinButton;
    CyclingButtonWidget<ServerSkinSettingType> skinTypeButton;

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo info) {
        manager = new ServerSkinManager(this.server);
        setSkinButton = manager.buildSetSkinButton(this.width / 2 - 160, this.height / 4 + 72 + 20, 120, 20);
        clearSkinButton = manager.buildClearSkinButton(this.width/2 + 40,this.height / 4 + 72 + 20,120, 20);
        skinTypeButton = manager.buildSkinTypeButton(this.width / 2 - 35, this.height / 4 + 72 + 20,70, 20);
        clearSkinButton.active = ServerSpecificSkinsClient.getFileForServer(this.server).isFile();
        this.addDrawableChild(setSkinButton);
        this.addDrawableChild(clearSkinButton);
        this.addDrawableChild(skinTypeButton);
    }
    @Inject(at = @At("HEAD"), method = "addAndClose")
        private void check_if_info_changed(CallbackInfo info) {
        if ((!Objects.equals(this.server.address, this.addressField.getText()) || !Objects.equals(this.server.name, this.serverNameField.getText()))) {
            manager.set_previous_info(this.server);
            manager.requestSetSkinForServer();
        }
    }
    @Inject(at = @At("TAIL"), method = "addAndClose")
    private void saveSkin(CallbackInfo info) throws IOException {
        if (manager.shouldSetSkin()) {
            ServerSpecificSkinsClient.saveSkinForServer(this.server, manager.getSelectedSkin());
        }
        else if (manager.shouldClearSkin()){
            ServerSpecificSkinsClient.deleteSkinForServer(this.server);
        }
        if(manager.get_previous_info() != null){
            ServerSpecificSkinsClient.deleteSkinForServer(manager.get_previous_info());
            manager.clear_previous_info();
        }
        ConfigManager.Config config = ConfigManager.getConfig();
        config.setSkinTypeForServer(this.server, skinTypeButton.getValue());
        ConfigManager.saveConfig(config);
    }


}
