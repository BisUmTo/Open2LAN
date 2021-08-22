package it.multicoredev.opentoall.gui.screen;


import it.multicoredev.opentoall.Resources;
import it.multicoredev.opentoall.gui.widget.CopyTextFieldWidget;
import it.multicoredev.opentoall.playit.PlayIt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import static it.multicoredev.opentoall.OpenToALL.PLAYIT;

public class OpenToWanScreen extends Screen {
    private static final Text START_TEXT = new TranslatableText("wanServer.start");
    private static final Text STOP_TEXT = new TranslatableText("wanServer.stop");
    private static final Text ADDRESS_TEXT = new TranslatableText("wanServer.address");
    private static final Text UNKNOWN_ADDRESS_TEXT = new TranslatableText("wanServer.unknownAddress");
    private static final String PLAYIT_WEBSITE = "https://playit.gg/";
    private static final Text PLAYIT_WEBSITE_TEXT = new TranslatableText("wanServer.unknownAddress");
    private static final Identifier WIDGETS_TEXTURE = new Identifier(Resources.MOD_ID, "textures/gui/widgets.png");

    private final Screen parent;
    private final MinecraftServer server;
    private CopyTextFieldWidget addressField;

    public OpenToWanScreen(Screen parent) {
        super(new TranslatableText("wanServer.title"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        this.server = client.getServer();
    }

    public static void sendWanMessage(String translatableKey) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new TranslatableText(translatableKey,
                Texts.bracketed(new LiteralText(PLAYIT.getAddress())).styled(
                        (style) -> style
                                .withColor(Formatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, PLAYIT.getAddress()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))
                                .withInsertion(PLAYIT.getAddress())
                )
        ));
    }

    protected void init() {
        // ADDRESS
        addressField = new CopyTextFieldWidget(client.textRenderer, width / 2 - 155 + 1, height / 4 + 21, 276, 20, ADDRESS_TEXT);
        if (PLAYIT != null && PLAYIT.getAddress() != null) addressField.setText(PLAYIT.getAddress());
        else addressField.setText(UNKNOWN_ADDRESS_TEXT.getString());
        if(PLAYIT != null && PLAYIT.getAddress() != null) addressField.setMaxLength(PLAYIT.getAddress().length());
        addDrawableChild(addressField);

        // PLAYIT
        ButtonWidget playItButton = new TexturedButtonWidget(width / 2 + 132, height / 4 + 20, 22, 22, 20, 0, 22, WIDGETS_TEXTURE, 42, 44, (button) -> {
            client.setScreen(new ConfirmChatLinkScreen((confirmed) -> {
                if (confirmed) {
                    Util.getOperatingSystem().open(PLAYIT_WEBSITE);
                }
                this.client.setScreen(this);
            }, PLAYIT_WEBSITE, true));
        }, PLAYIT_WEBSITE_TEXT);
        this.addDrawableChild(playItButton);

        // START
        ButtonWidget startButton = new ButtonWidget(width / 2 - 155, height / 4 + 104, 150, 20, START_TEXT, this::startWanWorld);
        if (PLAYIT != null) startButton.visible = false;
        this.addDrawableChild(startButton);

        // STOP
        ButtonWidget stopButton = new ButtonWidget(width / 2 - 155, height / 4 + 104, 150, 20, STOP_TEXT, this::stopWanWorld);
        if (PLAYIT == null) stopButton.visible = false;
        this.addDrawableChild(stopButton);

        // CANCEL
        ButtonWidget cancelButton = new ButtonWidget(this.width / 2 + 5, height / 4 + 104, 150, 20, ScreenTexts.CANCEL, (button) -> client.setScreen(this.parent));
        this.addDrawableChild(cancelButton);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, title, width / 2, Math.min(40, height / 4 - 30), 16777215);
        drawCenteredText(matrices, textRenderer, ADDRESS_TEXT, width / 2 - 153 + (textRenderer.getWidth(ADDRESS_TEXT) / 2), height / 4 + 8, 16777215);
        addressField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    // START
    private void startWanWorld(ButtonWidget button) {
        new Thread(OpenToWanScreen::startWanWorld).start();
        client.setScreen(null);
    }

    public static void startWanWorld() {
        if (PLAYIT != null) PLAYIT.stop();
        PLAYIT = new PlayIt(MinecraftClient.getInstance().getServer().getServerPort());
        PLAYIT.start();
    }

    // STOP
    private void stopWanWorld(ButtonWidget button) {
        if (PLAYIT != null) PLAYIT.stop();
        PLAYIT = null;
        client.setScreen(parent);
    }
}
