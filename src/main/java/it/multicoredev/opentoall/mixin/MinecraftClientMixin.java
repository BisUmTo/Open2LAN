package it.multicoredev.opentoall.mixin;

import it.multicoredev.opentoall.OpenToALL;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "stop", at = @At(value = "HEAD"))
    public void stop(CallbackInfo ci) {
        OpenToALL.shutDown();
    }
}
