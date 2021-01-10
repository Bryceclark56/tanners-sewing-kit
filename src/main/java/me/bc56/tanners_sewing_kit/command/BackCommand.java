package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.bc56.tanners_sewing_kit.common.PlayerLocation;
import me.bc56.tanners_sewing_kit.tpa.TeleportMixinAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class BackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("back").executes(BackCommand::goBack));
    }

    public static int goBack(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();

        PlayerLocation lastPos = ((TeleportMixinAccess)player).getLastLocation();

        if (lastPos == null) {
            sendFailureMessage(player);
            return 0;
        }

        player.teleport(lastPos.dimension, lastPos.x, lastPos.y, lastPos.z, player.getYaw(1.0F), player.getPitch(1.0F));

        ((TeleportMixinAccess)player).setLastLocation(null);

        return 0;
    }

    public static void sendFailureMessage(ServerPlayerEntity player) {
        player.sendMessage(new LiteralText("You have nowhere to go back to!").formatted(Formatting.RED), false);
    }

    public static void setLastLocation(ServerPlayerEntity player) {
        PlayerLocation location = new PlayerLocation(player.getServerWorld(), player.getX(), player.getY(), player.getZ());
        ((TeleportMixinAccess)player).setLastLocation(location);
    }
}
