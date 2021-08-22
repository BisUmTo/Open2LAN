package it.multicoredev.opentoall.gui.widget;

import it.multicoredev.opentoall.mixin.TextFieldWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class CopyTextFieldWidget extends TextFieldWidget {
    public CopyTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        setEditable(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && !((TextFieldWidgetAccessor)this).isEditable()) {
            MinecraftClient.getInstance().keyboard.setClipboard(getText());
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
