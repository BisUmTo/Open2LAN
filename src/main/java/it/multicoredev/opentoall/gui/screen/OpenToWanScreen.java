package it.multicoredev.opentoall.gui.screen;


import it.multicoredev.opentoall.OpenToALL;
import it.multicoredev.opentoall.gui.widget.CopyTextFieldWidget;
import it.multicoredev.opentoall.gui.widget.PasswordFieldWidget;
import it.multicoredev.opentoall.ngrok.NgrokTunnel;
import it.multicoredev.opentoall.ngrok.NgrokThread;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static it.multicoredev.opentoall.OpenToALL.*;

public class OpenToWanScreen extends Screen {
    private static final Text START_TEXT = new TranslatableText("wanServer.start");
    private static final Text START_FAILED_TEXT = new TranslatableText("wanServer.startFailed").styled(style -> style.withColor(Formatting.RED));
    private static final Text START_FAILED_CHECK_TOKEN_TEXT = new TranslatableText("wanServer.startFailedCheckToken").styled(style -> style.withColor(Formatting.RED));
    private static final Text START_TIMEOUT_TEXT = new TranslatableText("wanServer.startTimeout").styled(style -> style.withColor(Formatting.RED));
    private static final Text STOP_TEXT = new TranslatableText("wanServer.stop");
    private static final Text STOPPED_TEXT = new TranslatableText("wanServer.stopped");
    private static final Text ADDRESS_TEXT = new TranslatableText("wanServer.address");
    private static final Text UNKNOWN_ADDRESS_TEXT = new TranslatableText("wanServer.unknownAddress");
    private static final Text AUTHTOKEN_TEXT = new TranslatableText("wanServer.authtoken");
    private static final Text GET_AUTHTOKEN_TEXT = new TranslatableText("wanServer.getAuthtoken");
    private static final Text ALREADY_AUTHORIZED_TEXT = new TranslatableText("wanServer.alreadyAuthorized");
    private static final Text NEED_AUTHORIZATION_TEXT = new TranslatableText("wanServer.needAuthorization");
    public static final Text DONLOADING_TEXT = new TranslatableText("wanServer.downloadingNgrok").styled(style -> style.withColor(Formatting.GRAY));
    public static final Text DONLOADED_TEXT = new TranslatableText("wanServer.downloadedNgrok").styled(style -> style.withColor(Formatting.GRAY));
    private static final String NGROK_WEBSITE = "https://dashboard.ngrok.com/";

    private final Screen parent;
    private final MinecraftServer server;
    private CopyTextFieldWidget addressField;
    private PasswordFieldWidget authtokenField;

    public OpenToWanScreen(Screen parent) {
        super(new TranslatableText("wanServer.title"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        this.server = client.getServer();
    }

    public static void sendWanMessage(String translatableKey) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new TranslatableText(translatableKey,
                Texts.bracketed(new LiteralText(NGROK_TUNNEL.ip())).styled(
                        (style) -> style
                                .withColor(Formatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, NGROK_TUNNEL.ip()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))
                                .withInsertion(NGROK_TUNNEL.ip())
                )
        ));
    }

    protected void init() {
        // ADDRESS
        addressField = new CopyTextFieldWidget(client.textRenderer, width / 2 - 155 + 1, height / 4 + 21, 308, 20, ADDRESS_TEXT);
        if (NGROK_TUNNEL != null && NGROK_TUNNEL.ip() != null) addressField.setText(NGROK_TUNNEL.ip());
        else addressField.setText(UNKNOWN_ADDRESS_TEXT.getString());
        addDrawableChild(addressField);

        // AUTHTOKEN
        authtokenField = new PasswordFieldWidget(client.textRenderer, width / 2 - 155 + 1, height / 4 + 62, 228, 20, NEED_AUTHORIZATION_TEXT);
        if (!NgrokThread.needAuthentication()) {
            authtokenField.setEditable(false);
            authtokenField.emptyText = ALREADY_AUTHORIZED_TEXT;
        }
        authtokenField.setMaxLength(66);
        addDrawableChild(authtokenField);

        // OPEN NGROK
        ButtonWidget openNgrok = new ButtonWidget(this.width / 2 + 85, height / 4 + 62, 70, 20, GET_AUTHTOKEN_TEXT, this::openNgrokWebsite);
        this.addDrawableChild(openNgrok);

        // START
        ButtonWidget startButton = new ButtonWidget(width / 2 - 155, height / 4 + 104, 150, 20, START_TEXT, this::startWanWorld);
        if (NGROK_TUNNEL != null) startButton.visible = false;
        this.addDrawableChild(startButton);

        // STOP
        ButtonWidget stopButton = new ButtonWidget(width / 2 - 155, height / 4 + 104, 150, 20, STOP_TEXT, this::stopWanWorld);
        if (NGROK_TUNNEL == null) stopButton.visible = false;
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
        drawCenteredText(matrices, textRenderer, AUTHTOKEN_TEXT, width / 2 - 153 + (textRenderer.getWidth(AUTHTOKEN_TEXT) / 2), height / 4 + 49, 16777215);
        authtokenField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    // START
    private void startWanWorld(ButtonWidget button) {
        new Thread(this::startWanWorld).start();
        client.setScreen(null);
    }

    private void startWanWorld() {
        try {
            if (NgrokThread.needAuthentication()) OpenToALL.shutDown();
            if ((!NgrokThread.isRunning(1) && NGROK_THREAD == null) || NgrokThread.needAuthentication()) {
                NGROK_THREAD = new NgrokThread();
                if (NgrokThread.needAuthentication()) NGROK_THREAD.setAuthtoken(authtokenField.getText());
                NGROK_THREAD.start();
            }
            if(!NgrokThread.isRunning(NgrokThread.fileExist()?50:600)) throw new TimeoutException();
            NGROK_TUNNEL = new NgrokTunnel(server.getServerPort());
            sendWanMessage("wanServer.started");
        } catch (TimeoutException e){
            if (OpenToALL.DEBUG) e.printStackTrace();
            client.inGameHud.getChatHud().addMessage(START_TIMEOUT_TEXT);
        } catch (Exception e) {
            if (OpenToALL.DEBUG) e.printStackTrace();
            if(NgrokThread.needAuthentication() && authtokenField.getText().isEmpty())
                client.inGameHud.getChatHud().addMessage(START_FAILED_CHECK_TOKEN_TEXT);
            else
                client.inGameHud.getChatHud().addMessage(START_FAILED_TEXT);
        }
    }

    // STOP
    private void stopWanWorld(ButtonWidget button) {
        stopWanWorld();
        client.setScreen(parent);
    }

    private void stopWanWorld() {
        try {
            NGROK_TUNNEL.close();
            client.inGameHud.getChatHud().addMessage(STOPPED_TEXT);
        } catch (IOException e) {
            if (OpenToALL.DEBUG) e.printStackTrace();
        }
        NGROK_TUNNEL = null;
    }

    // OPEN
    private void openNgrokWebsite(ButtonWidget button) {
        client.setScreen(new ConfirmChatLinkScreen((confirmed) -> {
            if (confirmed) {
                Util.getOperatingSystem().open(NGROK_WEBSITE);
            }

            this.client.setScreen(this);
        }, NGROK_WEBSITE, true));
    }

}
