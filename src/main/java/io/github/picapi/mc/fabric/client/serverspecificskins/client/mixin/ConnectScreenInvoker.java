package io.github.picapi.mc.fabric.client.serverspecificskins.client.mixin;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ConnectScreen.class)
public interface ConnectScreenInvoker {
    @Invoker("setStatus")
    void invokeSetStatus(Text newStatus);
}
