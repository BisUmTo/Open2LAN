package it.multicoredev.opentoall.mixin;

import it.multicoredev.opentoall.gui.screen.OpenToLanScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    private static final Text OPEN_TO_LAN_TEXT = new TranslatableText("menu.shareToLan");
    private static final Text LAN_CONFIG_TEXT = new TranslatableText("menu.lanConfig");

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isIntegratedServerRunning()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void lanServerProperties(CallbackInfo ci, String s, ButtonWidget oldButton) {
        oldButton.visible = false;

        ButtonWidget lanServerButton = new ButtonWidget(width / 2 + 4, height / 4 + 96 + -16, 98, 20, OPEN_TO_LAN_TEXT,
                (button) -> Objects.requireNonNull(client).setScreen(new OpenToLanScreen(this))
        );
        addDrawableChild(lanServerButton);

        if (client != null && client.isIntegratedServerRunning() && client.getServer().isRemote()) {
            lanServerButton.setMessage(LAN_CONFIG_TEXT);
        }
        lanServerButton.active = client.isIntegratedServerRunning();
    }
}
