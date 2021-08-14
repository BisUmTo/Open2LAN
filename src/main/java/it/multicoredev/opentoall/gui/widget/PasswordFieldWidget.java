package it.multicoredev.opentoall.gui.widget;

import it.multicoredev.opentoall.mixin.TextFieldWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

public class PasswordFieldWidget extends TextFieldWidget {
    public boolean password = true;
    public Text emptyText;

    public PasswordFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        emptyText = text;
    }

    public PasswordFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height) {
        super(textRenderer, x, y, width, height, Text.of(""));
        emptyText = Text.of("");
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        String authtoken = getText();
        if (!isActive() && getText().isEmpty()) setText(emptyText.getString());
        else if (password) setText(StringUtils.repeat('*', authtoken.length()));
        super.renderButton(matrices,mouseX,mouseY,delta);
        setText(authtoken);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && button == 1) {
            setText(MinecraftClient.getInstance().keyboard.getClipboard());
            playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
