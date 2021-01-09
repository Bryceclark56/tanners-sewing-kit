package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.bc56.tanners_sewing_kit.tpa.PlayerTeleportRequest;
import me.bc56.tanners_sewing_kit.tpa.TeleportMixinAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class TpaCommands {
    public static final String TARGET = "target";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> tpa = dispatcher
                .register(literal("tpa").then(argument(TARGET, player()).executes(context -> tpaRequest(context))));
        dispatcher.register(literal("tprequest").redirect(tpa));

        LiteralCommandNode<ServerCommandSource> tpahere = dispatcher.register(
                literal("tpahere").then(argument(TARGET, player()).executes(context -> tpaHereRequest(context))));
        dispatcher.register(literal("tphere").redirect(tpahere));

        dispatcher.register(literal("tpaccept").executes(context -> acceptRequest(context)));
    }

    public static int tpaRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerTeleportRequest.newTpa(context.getSource().getPlayer(), getPlayer(context, TARGET));

        return 0;
    }

    public static int tpaHereRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerTeleportRequest.newTpaHere(context.getSource().getPlayer(), getPlayer(context, TARGET));

        return 0;
    }

    public static int acceptRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity caller = context.getSource().getPlayer();

        PlayerTeleportRequest request = ((TeleportMixinAccess)caller).getIncomingRequest();

        if (request != null) {
            request.execute();
        }

        return 0;
    }
}
