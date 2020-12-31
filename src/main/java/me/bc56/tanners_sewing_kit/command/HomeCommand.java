package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import me.bc56.tanners_sewing_kit.common.HomeMixinAccess;
import me.bc56.tanners_sewing_kit.common.PlayerHome;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("sethome").executes(HomeCommand::setHome));
        dispatcher.register(literal("home").executes(HomeCommand::goHome));
    }

    public static int goHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        int result = ((HomeMixinAccess)player).teleportToHome();

        if (result == 1) {
            throw new SimpleCommandExceptionType(new LiteralText("Home not set")).create();
        }

        return 0;
    }

    public static int setHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ((HomeMixinAccess)player).setHome(PlayerHome.createAt(player));
        player.sendMessage(new LiteralText("Home set").formatted(Formatting.GOLD), true);

        return 0;
    }
}
