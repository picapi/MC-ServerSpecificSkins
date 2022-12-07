package io.github.picapi.mc.fabric.client.serverspecificskins.client.mixin;

import io.github.picapi.mc.fabric.client.serverspecificskins.ConfigManager;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerAddressUtilities;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.text.Text;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSkinSettingType;
import io.github.picapi.mc.fabric.client.serverspecificskins.ServerSpecificSkins;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.ServerSpecificSkinsClient;
import io.github.picapi.mc.fabric.client.serverspecificskins.client.SkinChanger;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

@Mixin(targets = "net/minecraft/client/gui/screen/ConnectScreen$1")
public abstract class MultiplayerJoinMixin  {

    @Final
    @Shadow ConnectScreen field_2416;

    @Final
    @Shadow ServerAddress field_33737;

    @Mixin(ConnectScreen.class)
    private interface ConnectScreenInvoker {
        @Invoker("setStatus")
        void invokeSetStatus(Text newStatus);
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/net/InetSocketAddress;Z)Lnet/minecraft/network/ClientConnection;"), method = "run()V")
    private void SkinListener(CallbackInfo ci) throws IOException {
        // Get the relevant file - for now just use a placeholder
        ConfigManager.Config config = ConfigManager.getConfig();
        ServerSkinSettingType expectedType = config.getSkinTypeForAddress(ServerAddressUtilities.stringify(field_33737));
        ServerSpecificSkins.LOGGER.info("We've tried to connect - deploy the skin!");
        var skin_file = ServerSpecificSkinsClient.getFileForAddress(field_33737);
        ServerSpecificSkins.LOGGER.info("Got file "+ skin_file.getPath());
        if(skin_file.isFile()) {
            ServerSpecificSkins.LOGGER.info("Skin file found, checking if already applied...!");
            ((ConnectScreenInvoker)field_2416).invokeSetStatus(Text.translatable("serverspecificskins.connecting.checkingSkin"));
            byte[] new_hash = DigestUtils.getSha256Digest().digest(Files.readAllBytes(skin_file.toPath()));
            SkinChanger.SkinInfo skinInfo = SkinChanger.getCurrentSkin();
            if(!(Arrays.equals(new_hash,SkinChanger.getCurrentSkinSHA256Hash())) || !expectedType.equals(skinInfo.getSkinType())  ) {
                ServerSpecificSkins.LOGGER.info("Hash differs - applying skin");
                ((ConnectScreenInvoker)field_2416).invokeSetStatus(Text.translatable("serverspecificskins.connecting.changingSkin"));
                if(SkinChanger.setSkin(skin_file, expectedType)) {
                    // Look to recalculate hash...
                    var new_skin_web = SkinChanger.getCurrentSkin();
                    ServerSpecificSkinsClient.saveSkinForServer(field_33737, new_skin_web.getSkinData());
                }
            } else {
                ServerSpecificSkins.LOGGER.info("Hash matches, skipping.");
            }

        } else {
            ServerSpecificSkins.LOGGER.info("No skin file found, continuing...");
        }
    }

}
