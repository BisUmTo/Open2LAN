package mod.linguardium.open2lan;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.IOException;

import static mod.linguardium.open2lan.Open2Lan.NGROK_TUNNEL;

public class Open2WanScreen extends Screen {
    private static final Text START_TEXT = new TranslatableText("wanServer.start");
    private static final Text START_FAILED_TEXT = new TranslatableText("wanServer.startFailed").styled(style -> style.withColor(Formatting.RED));
    private static final Text STOP_TEXT = new TranslatableText("wanServer.stop");
    private static final Text STOPPED_TEXT = new TranslatableText("wanServer.stopped");
    private static final Text ADDRESS_TEXT = new TranslatableText("wanServer.address");
    private static final Text UNKNOWN_ADDRESS_TEXT = new TranslatableText("wanServer.unknownAddress");
    private static final Text AUTHTOKEN_TEXT = new TranslatableText("wanServer.authtoken");

    private final Screen parent;
    private final MinecraftServer server;
    // BUTTONS
    private ButtonWidget startButton;
    private ButtonWidget stopButton;
    private ButtonWidget cancelButton;
    private TextFieldWidget addressField;
    private TextFieldWidget authtokenField;
    private String authtoken;


    public Open2WanScreen(Screen parent, MinecraftClient client) {
        super(new TranslatableText("wanServer.title"));
        this.parent = parent;
        this.client = client;
        this.server = client.getServer();
    }

    protected void init() {
        // ADDRESS
        addressField = new TextFieldWidget(client.textRenderer, width / 2 - 155 + 1, height / 4 + 21, 308, 20, ADDRESS_TEXT) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (isMouseOver(mouseX, mouseY)) {
                    client.keyboard.setClipboard(addressField.getText());
                    playDownSound(client.getSoundManager());
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        if (NGROK_TUNNEL != null && NGROK_TUNNEL.ip() != null)
            addressField.setText(NGROK_TUNNEL.ip());
        else
            addressField.setText(UNKNOWN_ADDRESS_TEXT.getString());
        addressField.setEditable(false);
        addDrawableChild(addressField);

        // AUTHTOKEN
        authtokenField = new TextFieldWidget(client.textRenderer, width / 2 - 155 + 1, height / 4 + 62, 308, 20, Text.of(authtoken));
        // todo auth
        addDrawableChild(authtokenField);

        // START
        startButton = new ButtonWidget(width / 2 - 155, height / 4 + 104, 150, 20, START_TEXT, (button) -> {
            client.setScreen(null);
            try {
                NGROK_TUNNEL = new NgrokTunnel(server.getServerPort());
                client.inGameHud.getChatHud().addMessage(new TranslatableText("wanServer.started",
                        Texts.bracketed(new LiteralText(NGROK_TUNNEL.ip())).styled(
                                (style) -> style
                                        .withColor(Formatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, NGROK_TUNNEL.ip()))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))
                                        .withInsertion(NGROK_TUNNEL.ip())
                        )
                ));
            } catch (Exception e) {
                client.inGameHud.getChatHud().addMessage(START_FAILED_TEXT);
                Open2Lan.LOGGER.error(e);
            }

        });
        this.addDrawableChild(startButton);

        // STOP
        stopButton = new ButtonWidget(width / 2 - 155, height / 4 + 104, 150, 20, STOP_TEXT, (button) -> {
            client.setScreen(null);
            try {
                NGROK_TUNNEL.close();
                client.inGameHud.getChatHud().addMessage(STOPPED_TEXT);
            } catch (IOException ignored) {
            }
            NGROK_TUNNEL = null;
        });
        stopButton.visible = false;
        this.addDrawableChild(stopButton);

        // CANCEL
        cancelButton = new ButtonWidget(this.width / 2 + 5, height / 4 + 104, 150, 20, ScreenTexts.CANCEL, (button) -> this.client.setScreen(this.parent));
        this.addDrawableChild(cancelButton);

        // UPDATE PAGE
        if (NGROK_TUNNEL != null) {
            stopButton.visible = true;
            startButton.visible = false;
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, Math.min(40, height / 4 - 30), 16777215);
        drawCenteredText(matrices, this.textRenderer, ADDRESS_TEXT, width / 2 - 153 + (textRenderer.getWidth(ADDRESS_TEXT) / 2), height / 4 + 8, 16777215);
        addressField.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, AUTHTOKEN_TEXT, width / 2 - 153 + (textRenderer.getWidth(AUTHTOKEN_TEXT) / 2), height / 4 + 49, 16777215);
        authtokenField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

}
