package io.github.picapi.mc.fabric.client.serverspecificskins.client.mixin;

import io.github.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSkinSettingType;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSpecificSkins;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.ServerSpecificSkinsClient;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.SkinChanger;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;


@Mixin(ConnectScreen.class)
public abstract class MultiplayerJoinMixin  {

    private static Thread skinApplyingThread;

    @Inject(method = "connect(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Z)V", at = @At(value = "INVOKE", target="Lnet/minecraft/client/gui/screen/ConnectScreen;connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;)V"),  locals = LocalCapture.CAPTURE_FAILHARD)
    private static void SkinApplier(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean quickPlay, CallbackInfo ci, ConnectScreen connectScreen) throws InterruptedException {
        skinApplyingThread = new Thread("Skin Applying Thread"){
            public void run() {
                ConfigManager.Config config = ConfigManager.getConfig();
                ServerSkinSettingType expectedType = config.getSkinTypeForServer(info);
                ServerSpecificSkins.LOGGER.info("We've tried to connect - deploy the skin!");
                var skin_file = ServerSpecificSkinsClient.getFileForServer(info);
                ServerSpecificSkins.LOGGER.info("Got file " + skin_file.getPath());
                if (skin_file.isFile()) {
                    ServerSpecificSkins.LOGGER.info("Skin file found, checking if already applied...!");
                    ((ConnectScreenInvoker) connectScreen).invokeSetStatus(Text.translatable("serverspecificskins.connecting.checkingSkin"));
                    byte[] new_hash = new byte[0];
                    try {
                        new_hash = DigestUtils.getSha256Digest().digest(Files.readAllBytes(skin_file.toPath()));
                        SkinChanger.SkinInfo skinInfo = SkinChanger.getCurrentSkin();
                        if (!(Arrays.equals(new_hash, SkinChanger.getCurrentSkinSHA256Hash())) || !expectedType.equals(skinInfo.getSkinType())) {
                            ServerSpecificSkins.LOGGER.info("Hash differs - applying skin");
                            ((ConnectScreenInvoker) connectScreen).invokeSetStatus(Text.translatable("serverspecificskins.connecting.changingSkin"));
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
        };
        skinApplyingThread.start();
        skinApplyingThread.join();
    }

}
