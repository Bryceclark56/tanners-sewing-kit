package me.bc56.tanners_sewing_kit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.world.chunk.ChunkStatus;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class GenChunksCommand {

    public static boolean inProgress = false;

    public static double percent = 0;
    public static int last = 0;

    public static long chunkTotal = 0;
    public static long chunkCount = 0;

    public static Thread taskThread = null;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("genchunks")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("start")
                .then(argument("chunkradius", integer())
                    .executes(GenChunksCommand::genChunks)
                )
            )
            .then(literal("stop")
                .executes(GenChunksCommand::stop)
            )
        );
    }

    public static int genChunks(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();

        if (inProgress) {
            source.sendError(new LiteralText("Already busy generating chunks.\nTry again later.").formatted(Formatting.RED));
            return 0;
        }

        inProgress = true;

        int radius = getInteger(context, "chunkradius");
        ServerPlayerEntity caller = source.getPlayer();
        ServerChunkManager chunkManager = caller.getServerWorld().getChunkManager();

        int playerChunkX = (int) Math.floor(caller.getX() / 16.0);
        int playerChunkZ = (int) Math.floor(caller.getZ() / 16.0);

        source.sendFeedback(new LiteralText("Generating chunks in a radius of ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(radius + " chunks").formatted(Formatting.AQUA)).append(" around you"), true);
        
        

        Runnable taskRunnable = () -> {
            percent = 0;
            chunkTotal = radius*radius;
            chunkCount = 0;

            source.sendFeedback(new LiteralText("This might cause lag").formatted(Formatting.LIGHT_PURPLE), true);

            caller.sendMessage(new LiteralText( ((int) percent) + "% of chunks generated | " + chunkCount +  "/" + chunkTotal).formatted(Formatting.LIGHT_PURPLE), true);

            for (int r = 1; r < radius && inProgress; ++r) {
                chunkManager.getChunk(playerChunkX+r, playerChunkZ+r, ChunkStatus.FULL, true);
                chunkManager.getChunk(playerChunkX-r, playerChunkZ-r, ChunkStatus.FULL, true);
                chunkManager.getChunk(playerChunkX-r, playerChunkZ+r, ChunkStatus.FULL, true);
                chunkManager.getChunk(playerChunkX+r, playerChunkZ-r, ChunkStatus.FULL, true);
                chunkCount+=4;
                //informPlayers(source, caller);

                for (int x = -r+1; x < r && inProgress; ++x) {
                    chunkManager.getChunk(playerChunkX+x, playerChunkZ+r, ChunkStatus.FULL, true);
                    chunkManager.getChunk(playerChunkX+x, playerChunkZ-r, ChunkStatus.FULL, true);

                    chunkManager.getChunk(playerChunkX+r, playerChunkZ+x, ChunkStatus.FULL, true);
                    chunkManager.getChunk(playerChunkX-r, playerChunkZ+x, ChunkStatus.FULL, true);

                    chunkCount+=4;
                    informPlayers(source, caller);
                }
            }

            source.sendFeedback(new LiteralText(chunkCount + " chunks generated").formatted(Formatting.LIGHT_PURPLE), true);

            inProgress = false;
        };

        taskThread = new Thread(taskRunnable);
        taskThread.setName("TannerKit-GenChunksCommand");
        taskThread.setDaemon(true);
        taskThread.start();

        return 0;
    }

    public static void informPlayers(ServerCommandSource source, ServerPlayerEntity player) {
        percent = Math.floor(chunkCount*1.0/chunkTotal * 100.0);
        if (((int) percent) > last && ((int) percent) % 10 == 0 ) {
            source.sendFeedback(new LiteralText( ((int) percent) + "% of chunks generated | " + chunkCount +  "/" + chunkTotal).formatted(Formatting.LIGHT_PURPLE), true);
        }
        last = (int) percent;

        player.sendMessage(new LiteralText( ((int) percent) + "% of chunks generated | " + chunkCount +  "/" + chunkTotal).formatted(Formatting.LIGHT_PURPLE), true);
    }

    public static int stop(CommandContext<ServerCommandSource> context) {
        if (taskThread != null && taskThread.isAlive()) {
            inProgress = false;
        }

        return 0;
    }
}
