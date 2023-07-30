package io.github.picapi.mc.fabric.client.serverspecificskins.client.mixin;

import io.github.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSkinSettingType;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSpecificSkins;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.ServerSpecificSkinsClient;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.SkinChanger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

@Mixin(targets = "net/minecraft/client/gui/screen/ConnectScreen$1")
public abstract class ConnectScreenThreadMixin {

    private ServerInfo info;
    private ConnectScreen screen;

    @Inject(method = "<init>", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void get_server_info(ConnectScreen connectScreen, String string, ServerAddress serverAddress, MinecraftClient minecraftClient, ServerInfo serverInfo, CallbackInfo ci) {
        info = serverInfo;
        screen = connectScreen;
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Ljava/util/Optional;get()Ljava/lang/Object;"))
    private void apply_skin(CallbackInfo ci){
        ConfigManager.Config config = ConfigManager.getConfig();
        ServerSkinSettingType expectedType = config.getSkinTypeForServer(info);
        ServerSpecificSkins.LOGGER.info("We've tried to connect - deploy the skin!");
        var skin_file = ServerSpecificSkinsClient.getFileForServer(info);
        ServerSpecificSkins.LOGGER.info("Got file " + skin_file.getPath());
        if (skin_file.isFile()) {
            ServerSpecificSkins.LOGGER.info("Skin file found, checking if already applied...!");
            ((ConnectScreenInvoker) screen).invokeSetStatus(Text.translatable("serverspecificskins.connecting.checkingSkin"));
            byte[] new_hash = new byte[0];
            try {
                new_hash = DigestUtils.getSha256Digest().digest(Files.readAllBytes(skin_file.toPath()));
                SkinChanger.SkinInfo skinInfo = SkinChanger.getCurrentSkin();
                if (!(Arrays.equals(new_hash, SkinChanger.getCurrentSkinSHA256Hash())) || !expectedType.equals(skinInfo.getSkinType())) {
                    ServerSpecificSkins.LOGGER.info("Hash differs - applying skin");
                    ((ConnectScreenInvoker) screen).invokeSetStatus(Text.translatable("serverspecificskins.connecting.changingSkin"));
                    if (SkinChanger.setSkin(skin_file, expectedType)) {
                        // Look to recalculate hash...
                        var new_skin_web = SkinChanger.getCurrentSkin();
                        ServerSpecificSkinsClient.saveSkinForServer(info, new_skin_web.getSkinData());

                    }
                } else {
                    ServerSpecificSkins.LOGGER.info("Hash matches, skipping.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            ServerSpecificSkins.LOGGER.info("No skin file found, continuing...");
        }
    }


}
