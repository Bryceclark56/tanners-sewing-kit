package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.bc56.tanners_sewing_kit.common.PlayerTeleportRequest;
import me.bc56.tanners_sewing_kit.common.TeleportMixinAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;

public class TpAcceptCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tpaccept").executes(context -> acceptRequest(context)));
    }

    public static int acceptRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity caller = source.getPlayer();

        PlayerTeleportRequest request = ((TeleportMixinAccess) caller).getIncomingRequest();
        if (request != null) {
            request.execute();
        }

        return 0;
    }
}
