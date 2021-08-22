package it.multicoredev.opentoall.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.PublishCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PublishCommand.class)
public class PublishCommandMixin {
    @Shadow
    private static SimpleCommandExceptionType FAILED_EXCEPTION;
    @Shadow
    private static DynamicCommandExceptionType ALREADY_PUBLISHED_EXCEPTION;

    /**
     * @author BisUmTo
     * @reason Changing '/publish' command
     */
    @Overwrite
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("publish").requires((serverCommandSource) -> serverCommandSource.hasPermissionLevel(4))
                        .executes((commandContext) -> execute(commandContext.getSource(), NetworkUtils.findLocalPort(), false, false))
                        .then(
                                CommandManager.argument("port", IntegerArgumentType.integer(0, 65535))
                                        .executes((commandContext) -> execute(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), true, false))
                        ).then(
                        CommandManager.literal("allowCheats")
                                .executes((commandContext) -> execute(commandContext.getSource(), NetworkUtils.findLocalPort(), true, false))
                                .then(
                                        CommandManager.argument("port", IntegerArgumentType.integer(0, 65535))
                                                .executes((commandContext) -> execute(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), true, false))
                                )
                ).then(
                        CommandManager.literal("offlineMode")
                                .executes((commandContext) -> execute(commandContext.getSource(), NetworkUtils.findLocalPort(), true, true))
                                .then(
                                        CommandManager.argument("port", IntegerArgumentType.integer(0, 65535))
                                                .executes((commandContext) -> execute(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), true, true))
                                ).then(
                                CommandManager.literal("allowCheats")
                                        .executes((commandContext) -> execute(commandContext.getSource(), NetworkUtils.findLocalPort(), true, true))
                                        .then(
                                                CommandManager.argument("port", IntegerArgumentType.integer(0, 65535))
                                                        .executes((commandContext) -> execute(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port"), true, true))
                                        )
                        )
                )
        );
    }

    private static int execute(ServerCommandSource source, int port, boolean allowCheats, boolean forceOfflineMode) throws CommandSyntaxException {
        if (source.getServer().isRemote()) {
            throw ALREADY_PUBLISHED_EXCEPTION.create(source.getServer().getServerPort());
        } else if (!source.getServer().openToLan(source.getServer().getDefaultGameMode(), allowCheats, port)) {
            if (forceOfflineMode) source.getServer().setOnlineMode(false);
            throw FAILED_EXCEPTION.create();
        } else {
            source.sendFeedback(new TranslatableText("commands.publish.success", port), true);
            return port;
        }
    }
}




