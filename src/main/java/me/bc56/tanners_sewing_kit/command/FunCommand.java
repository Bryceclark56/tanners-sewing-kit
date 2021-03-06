package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FunCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("fun")
            //.then(literal("hehe").executes(FunCommand::fakeStop))
            .then(literal("tanner").executes(FunCommand::sendToTanner))
            .executes(FunCommand::baseFun));
    }

    public static int baseFun(CommandContext<ServerCommandSource> context) {
        return 0;
    }

    public static int fakeStop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        MinecraftDedicatedServer server = (MinecraftDedicatedServer) source.getMinecraftServer();

        List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();

        ServerPlayerEntity randomPlayer = playerList.get(new Random().nextInt(playerList.size()));

        randomPlayer.sendMessage(
                new LiteralText("Server is restarting in 5 seconds").formatted(Formatting.LIGHT_PURPLE), false);

        
        try {
            TimeUnit.SECONDS.sleep(5); // TODO: Move off of main thread
        } catch (InterruptedException e) {
            throw new SimpleCommandExceptionType(new LiteralText("Something went wrong while pranking a player")).create();
        }
        randomPlayer.sendMessage(new LiteralText("SIKE!").formatted(Formatting.LIGHT_PURPLE), false);

        return 0;
    }

    public static int sendToTanner(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity caller = source.getPlayer();
        MinecraftDedicatedServer server = (MinecraftDedicatedServer) source.getMinecraftServer();

        ServerPlayerEntity tanner = server.getPlayerManager().getPlayer(UUID.fromString("fc0e539f-769d-478f-9d97-aa0fe599006d"));

        if (tanner != null) {
            caller.sendMessage(new LiteralText("You did this to yourself...").formatted(Formatting.RED), false);
            caller.teleport(tanner.getServerWorld(), tanner.getX(), tanner.getY(), tanner.getZ(), tanner.getYaw(1.0F), tanner.getPitch(1.0F));

            caller.sendMessage(new LiteralText("You whisper to " + tanner.getDisplayName().asString() + ": UwU"), false);
            tanner.sendMessage(new LiteralText(caller.getDisplayName().asString() + " whispers to you: UwU").formatted(Formatting.GRAY), false);
        }

        return 0;
    }
}
