package mod.linguardium.open2lan.mixin;

import mod.linguardium.open2lan.Open2Lan;
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
            Open2Lan.NGROK_TUNNEL.close();
        } catch (IOException ignored) {
        }
        Open2Lan.NGROK_THREAD.start();
    }
}
