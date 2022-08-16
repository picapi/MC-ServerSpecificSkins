package io.picapi.mc.fabric.client.serverspecificskins.client.mixin;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import io.picapi.mc.fabric.client.serverspecificskins.client.ServerSpecificSkinsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;

@Mixin(ServerList.class)
public class ServerListRemoveMixin {

    @Inject(at = @At(value="TAIL"), method = "remove", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void init(ServerInfo sInfo, CallbackInfo info) {
        ServerSpecificSkinsClient.deleteSkinForServer(sInfo);
    }


}
