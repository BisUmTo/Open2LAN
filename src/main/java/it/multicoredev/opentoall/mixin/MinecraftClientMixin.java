package it.multicoredev.opentoall.mixin;

import it.multicoredev.opentoall.OpenToALL;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "stop", at = @At(value = "HEAD"))
    public void lanServerProperties(CallbackInfo ci) {
        try {
            if (OpenToALL.NGROK_TUNNEL != null) OpenToALL.NGROK_TUNNEL.close();
        } catch (IOException e) {
            if(OpenToALL.DEBUG) e.printStackTrace();
        }
        if (OpenToALL.NGROK_THREAD != null && OpenToALL.NGROK_THREAD.isAlive()) OpenToALL.NGROK_THREAD.interrupt();
    }
}
