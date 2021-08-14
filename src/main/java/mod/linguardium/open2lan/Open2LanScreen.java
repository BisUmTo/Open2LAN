package mod.linguardium.open2lan;


import mod.linguardium.open2lan.mixin.IntegratedServerAccessor;
import mod.linguardium.open2lan.mixin.PlayerManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

import java.io.IOException;

import static mod.linguardium.open2lan.Open2Lan.MOD_ID;

public class Open2LanScreen extends Screen {
    private static final Text ALLOW_COMMANDS_TEXT = new TranslatableText("selectWorld.allowCommands");
    private static final Text GAME_MODE_TEXT = new TranslatableText("selectWorld.gameMode");
    private static final Text MAX_PLAYERS_TEXT = new TranslatableText("lanServer.maxPlayers");
    private static final Text ONLINE_MODE_TEXT = new TranslatableText("lanServer.onlineMode");
    private static final Text ENABLE_PVP_TEXT = new TranslatableText("lanServer.pvpEnabled");
    private static final Text OTHER_PLAYERS_TEXT = new TranslatableText("lanServer.otherPlayers");
    private static final Text SELECT_PORT_TEXT = new TranslatableText("lanServer.selectPort");
    private static final Text START_TEXT = new TranslatableText("lanServer.start");
    private static final Text CONFIG_SAVED_TEXT = new TranslatableText("lanServer.configSaved");
    private static final Text CONFIG_TITLE_TEXT = new TranslatableText("lanServer.configTitle");
    private static final Text OPEN_TO_WAN_TEXT = new TranslatableText("lanServer.shareToWan");
    private static final Text CHANGED_PORT_TEXT = new TranslatableText("wanServer.changedPort");
    private static final Identifier WIDGETS_TEXTURE = new Identifier(MOD_ID, "textures/gui/widgets.png");

    private final Screen parent;
    private final MinecraftServer server;
    private GameMode gameMode;
    private boolean allowCommands;
    private boolean onlineMode;
    private boolean enablePvp;
    private int lanPort;
    private int maxPlayers;

    // BUTTONS
    private CyclingButtonWidget gamemodeButton;
    private CyclingButtonWidget allowCommandsButton;
    private CyclingButtonWidget onlineModeButton;
    private CyclingButtonWidget enablePvpButton;
    private TextFieldWidget portField;
    private TextFieldWidget maxPlayersField;

    private ButtonWidget startButton;
    private ButtonWidget doneButton;
    private ButtonWidget cancelButton;
    private ButtonWidget wanButton;

    private Text displayTitle;

    private int portTextColor;
    private int maxPlayerTextColor;

    public Open2LanScreen(Screen parent, MinecraftClient client) {
        super(new TranslatableText("lanServer.title"));
        this.parent = parent;
        this.client = client;
        this.server = client.getServer();
        this.displayTitle = getTitle();

        if (client.isIntegratedServerRunning() && server.isRemote()) { // UPDATE
            gameMode = server.getForcedGameMode();
            allowCommands = server.getPlayerManager().areCheatsAllowed();
            onlineMode = server.isOnlineMode();
            enablePvp = server.isPvpEnabled();
            lanPort = server.getServerPort();
            maxPlayers = server.getMaxPlayerCount();
        } else { // START
            gameMode = server.getSaveProperties().getGameMode();
            allowCommands = server.getSaveProperties().areCommandsAllowed();
            onlineMode = true;
            enablePvp = server.isPvpEnabled();
            lanPort = 25565;
            maxPlayers = 8;
        }
    }

    protected void init() {
        // GAMEMODE
        gamemodeButton = CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values(GameMode.values()).initially(gameMode)
                .build(width / 2 - 155, height / 4 + 8, 150, 20, GAME_MODE_TEXT, (button, gameMode) -> this.gameMode = gameMode);
        addDrawableChild(gamemodeButton);

        // ALLOW COMMANDS
        allowCommandsButton = CyclingButtonWidget.onOffBuilder(allowCommands)
                .build(this.width / 2 + 5, height / 4 + 8, 150, 20, ALLOW_COMMANDS_TEXT, (button, allowCommands) -> this.allowCommands = allowCommands);
        addDrawableChild(allowCommandsButton);

        // PORT
        portField = new TextFieldWidget(client.textRenderer, width / 2 - 155 + 1, height / 4 + 45, 148, 20, SELECT_PORT_TEXT) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (isMouseOver(mouseX, mouseY) && client.isIntegratedServerRunning() && server.isRemote()) {
                    client.keyboard.setClipboard(portField.getText());
                    playDownSound(client.getSoundManager());
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };
        portField.setText(Integer.toString(lanPort));
        portField.setMaxLength(6);
        portField.setChangedListener((sPort) -> {
            Integer i = null;
            try {
                i = Integer.parseInt(sPort);
            } catch (NumberFormatException ignored) {
            }
            if (i != null && i < 65536 && i > 0) {
                lanPort = i;
                portTextColor = 0xFFFFFF;
            } else {
                lanPort = 25565;
                portTextColor = 0xFF0000;
            }
            portField.setEditableColor(portTextColor);
        });
        addDrawableChild(portField);

        // MAX PLAYERS
        maxPlayersField = new TextFieldWidget(client.textRenderer, width / 2 + 5 + 1, height / 4 + 45, 148, 20, MAX_PLAYERS_TEXT);
        maxPlayersField.setText(Integer.toString(maxPlayers));
        maxPlayersField.setMaxLength(3);
        maxPlayersField.setChangedListener((sPlayer) -> {
            Integer i = null;
            try {
                i = Integer.parseInt(sPlayer);
            } catch (NumberFormatException ignored) {
            }
            if (i != null && i <= 128 && i > 1) {
                maxPlayers = i;
                maxPlayerTextColor = 0xFFFFFF;
            } else {
                maxPlayers = 8;
                maxPlayerTextColor = 0xFF0000;
            }
            maxPlayersField.setEditableColor(maxPlayerTextColor);
        });
        addDrawableChild(maxPlayersField);

        // ONLINE MODE
        onlineModeButton = CyclingButtonWidget.onOffBuilder(onlineMode)
                .build(width / 2 - 155, height / 4 + 69, 150, 20, ONLINE_MODE_TEXT, (button, onlineMode) -> this.onlineMode = onlineMode);
        addDrawableChild(onlineModeButton);

        // ENABLE PVP
        enablePvpButton = CyclingButtonWidget.onOffBuilder(enablePvp)
                .build(width / 2 + 5, height / 4 + 69, 150, 20, ENABLE_PVP_TEXT, (button, enablePvp) -> this.enablePvp = enablePvp);
        addDrawableChild(enablePvpButton);

        // START
        startButton = new ButtonWidget(width / 2 - 155, height / 4 + 104, 150, 20, START_TEXT, (button) -> {
            client.setScreen(null);
            int i = lanPort > 0 && lanPort < 65536 ? lanPort : NetworkUtils.findLocalPort();
            if (server.openToLan(this.gameMode, this.allowCommands, i)) {
                client.inGameHud.getChatHud().addMessage(new TranslatableText("commands.publish.started",
                        Texts.bracketed(new LiteralText(String.valueOf(i))).styled(
                                (style) -> style
                                        .withColor(Formatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(i)))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))
                                        .withInsertion(String.valueOf(i))
                        )
                ));
                if (Open2Lan.NGROK_TUNNEL != null && Open2Lan.NGROK_TUNNEL.port() != server.getServerPort()) {
                    try {
                        Open2Lan.NGROK_TUNNEL.close();
                        client.inGameHud.getChatHud().addMessage(CHANGED_PORT_TEXT);
                    } catch (IOException ignored) {
                    }
                    Open2Lan.NGROK_TUNNEL = null;
                }
                // todo messaggio wan
            } else
                client.inGameHud.getChatHud().addMessage(new TranslatableText("commands.publish.failed"));
            client.updateWindowTitle();
            server.setOnlineMode(onlineMode);
            server.setPvpEnabled(enablePvp);
            ((PlayerManagerAccessor) server.getPlayerManager()).setMaxPlayers(maxPlayers);
        });
        this.addDrawableChild(startButton);

        // WAN
        wanButton = new TexturedButtonWidget(width / 2 - 155, height / 4 + 104, 20, 20, 0, 0, 20, WIDGETS_TEXTURE, 20, 40, (button) -> {
            this.client.setScreen(new Open2WanScreen(this, client));
        }, OPEN_TO_WAN_TEXT);
        wanButton.visible = false;
        this.addDrawableChild(wanButton);

        // DONE
        doneButton = new ButtonWidget(width / 2 - 131, height / 4 + 104, 126, 20, ScreenTexts.DONE, (button) -> {
            client.setScreen(null);
            ((IntegratedServerAccessor) server).setForcedGameMode(gameMode);
            server.getPlayerManager().setCheatsAllowed(allowCommands);
            client.player.setClientPermissionLevel(server.getPermissionLevel(client.player.getGameProfile()));
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                server.getCommandManager().sendCommandTree(serverPlayerEntity);
            }
            client.inGameHud.getChatHud().addMessage(CONFIG_SAVED_TEXT);
            server.setOnlineMode(onlineMode);
            server.setPvpEnabled(enablePvp);
            ((PlayerManagerAccessor) server.getPlayerManager()).setMaxPlayers(maxPlayers);
        });
        doneButton.visible = false;
        this.addDrawableChild(doneButton);

        // CANCEL
        cancelButton = new ButtonWidget(this.width / 2 + 5, height / 4 + 104, 150, 20, ScreenTexts.CANCEL, (button) -> this.client.setScreen(this.parent));
        this.addDrawableChild(cancelButton);

        // UPDATE PAGE
        if (client.isIntegratedServerRunning() && server.isRemote()) {
            doneButton.visible = true;
            wanButton.visible = true;
            startButton.visible = false;
            portField.setEditable(false);
            displayTitle = CONFIG_TITLE_TEXT;
        }
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        drawCenteredText(matrices, textRenderer, displayTitle, width / 2, Math.min(40, height / 4 - 30), 16777215);
        drawCenteredText(matrices, textRenderer, OTHER_PLAYERS_TEXT, width / 2, Math.max(55, height / 4 - 5), 16777215);
        drawCenteredText(matrices, this.textRenderer, SELECT_PORT_TEXT, width / 2 - 153 + (textRenderer.getWidth(SELECT_PORT_TEXT) / 2), height / 4 + 32, 16777215);
        portField.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, MAX_PLAYERS_TEXT, width / 2 + 7 + (textRenderer.getWidth(MAX_PLAYERS_TEXT) / 2), height / 4 + 32, 16777215);
        maxPlayersField.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public int getLanPort() {
        return lanPort;
    }
}
