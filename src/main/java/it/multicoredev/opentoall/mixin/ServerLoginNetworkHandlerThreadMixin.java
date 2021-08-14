package it.multicoredev.opentoall.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/server/network/ServerLoginNetworkHandler$1")
public class ServerLoginNetworkHandlerThreadMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isSingleplayer()Z", remap = true), method = "run", remap = false)
    private boolean singlePlayerNotOpenInLan(MinecraftServer minecraftServer) {
        return (!minecraftServer.isRemote() && minecraftServer.isSingleplayer()) || !minecraftServer.isOnlineMode();
    }
}
