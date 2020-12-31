package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.bc56.tanners_sewing_kit.common.PlayerTeleportRequest;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class TpaCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("tpa")
            .then(argument("target", player())
                .executes(context -> makeRequest(context))
            )
        );
    }

    public static int makeRequest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        ServerPlayerEntity caller = source.getPlayer();
        ServerPlayerEntity target = getPlayer(context, "target");

        // Feedback confirming request
        LiteralText confirmMessage = new LiteralText("Sending teleport request to ");
        LiteralText targetName = (LiteralText) new LiteralText(target.getEntityName()).append(new LiteralText(".").formatted(Formatting.GOLD));
        source.sendFeedback(confirmMessage.formatted(Formatting.GOLD).append(targetName.formatted(Formatting.AQUA)), false);

        // Set last request in players
        PlayerTeleportRequest.createAndSet(caller, target);

        // Send message to target informing of request from caller
        LiteralText informMessage = new LiteralText(" wishes to teleport to you.");
        LiteralText sourceName = new LiteralText(caller.getEntityName());
        target.sendMessage(sourceName.formatted(Formatting.AQUA).append(informMessage.formatted(Formatting.GOLD)), false);
        target.sendMessage(new LiteralText("Enter the command ").formatted(Formatting.GOLD).append(new LiteralText("/tpaccept").formatted(Formatting.LIGHT_PURPLE)).append(" to accept the request."), false);
        
        return Command.SINGLE_SUCCESS;
    }
    
}
