package me.bc56.tanners_sewing_kit.homes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.bc56.tanners_sewing_kit.ThreadManager;
import me.bc56.tanners_sewing_kit.command.BackCommand;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class HomeManager {
    public static final Logger LOGGER = LogManager.getLogger();

    public static final int HOME_LIMIT = 3;
    public static final String DEFAULT_HOME_NAME = "home";
    public static final String DEFAULT_FOLDER_NAME = "playerhomes";

    public static final Map<ServerPlayerEntity, ReentrantReadWriteLock> LOCK_MAP = new HashMap<>(); // Default
                                                                                                    // capacity of 16

    public static final Gson GSON = new GsonBuilder()
                                        .setPrettyPrinting()
                                        .registerTypeAdapter(PlayerHome.class, new PlayerHomeTypeAdapter())
                                        .create();

    public static void initialize() {
        // When a player connects
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            LOCK_MAP.put(player, new ReentrantReadWriteLock());

            Map<String, PlayerHome> homes = null;
            try {
                homes = readHomes(player);
            } catch (FileNotFoundException e) {
                // This is expected for a player's first connection to the server
                return;
            } catch (IOException e) {
                handler.disconnect(
                        new LiteralText("Unable to load homes from file. Please reconnect or contact an admin."));
                return;
            }

            ((HomeMixinAccess) player).getHomes().putAll(homes);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            LOCK_MAP.remove(handler.player);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Transfer homes to new object
            Map<String, PlayerHome> homes = ((HomeMixinAccess) newPlayer).getHomes();
            homes.putAll(((HomeMixinAccess) oldPlayer).getHomes());

            // Transfer homes save file lock to new player object
            ReentrantReadWriteLock lock = LOCK_MAP.remove(oldPlayer);
            if (lock == null) {
                lock = new ReentrantReadWriteLock();
            }
            LOCK_MAP.put(newPlayer, lock);
        });
    }

    public static void setNewHome(ServerPlayerEntity player) throws CommandSyntaxException {
        setNewHome(player, DEFAULT_HOME_NAME);
    }

    public static void setNewHome(ServerPlayerEntity player, String homeName) throws CommandSyntaxException {
        Map<String, PlayerHome> homes = ((HomeMixinAccess) player).getHomes();
        homeName = homeName.toLowerCase();

        if (((HomeMixinAccess) player).getHomes().size() >= HOME_LIMIT && !homes.containsKey(homeName)) {
            throw (new SimpleCommandExceptionType(
                    new LiteralText("You have reached the limit of how many homes you may have. (" + HOME_LIMIT + ")")
                            .formatted(Formatting.RED))).create();
        }

        PlayerHome newHome = PlayerHome.createAt(player);
        homes.put(homeName, newHome);

        // Only show name if not default home
        if (homeName.equals("home")) {
            player.sendMessage(new LiteralText("Home set").formatted(Formatting.GOLD), false);
        } else {
            player.sendMessage(new LiteralText("Home ").formatted(Formatting.GOLD)
                    .append(new LiteralText(homeName).formatted(Formatting.RESET).formatted(Formatting.WHITE))
                    .append(new LiteralText(" set").formatted(Formatting.GOLD)), false);
        }

        syncWithFile(player);
    }

    public static void sendToHome(ServerPlayerEntity player) {
        sendToHome(player, DEFAULT_HOME_NAME);
    }

    public static void sendToHome(ServerPlayerEntity player, String homeName) {
        homeName = homeName.toLowerCase();

        PlayerHome home = ((HomeMixinAccess) player).getHome(homeName);

        if (home == null)
            return;

        BackCommand.setLastLocation(player);

        // Ensure chunk is loaded
        ChunkPos chunkPos = new ChunkPos(new BlockPos(home.x, home.y, home.z));
        home.dimension.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());

        player.teleport(home.dimension, home.x, home.y, home.z, home.yaw, home.pitch);
        player.networkHandler.syncWithPlayerPosition(); // Not sure why this is needed, but it prevents a warning in the
                                                        // console
    }

    public static void removeHome(ServerPlayerEntity player) {
        removeNamedHome(player, DEFAULT_HOME_NAME);
    }

    public static void removeNamedHome(ServerPlayerEntity player, String homeName) {
        Map<String, PlayerHome> homes = ((HomeMixinAccess) player).getHomes();
        homeName = homeName.toLowerCase();

        if (!homes.containsKey(homeName))
            return;

        homes.remove(homeName);

        syncWithFile(player);
    }

    // Only useful for writes
    public static void syncWithFile(ServerPlayerEntity player) {
        ThreadManager.EXECUTOR.execute(() -> writeHomes(player)); // TODO: Determine if this is too often
    }

    public static void writeHomes(ServerPlayerEntity player) {
        Map<String, PlayerHome> homes = ((HomeMixinAccess) player).getHomes();
        if (homes == null || homes.isEmpty())
            return;

        WriteLock writeLock = LOCK_MAP.get(player).writeLock();
        writeLock.lock();

        Path saveFile = getSaveFile(player);
        try (Writer writer = Files.newBufferedWriter(saveFile)) {
            GSON.toJson(homes, writer);
            writer.flush();
        } catch (IOException e) {
            // TODO: Handle errors properly
            LOGGER.error(e);
        } finally {
            writeLock.unlock();
        }
    }

    public static Map<String, PlayerHome> readHomes(ServerPlayerEntity player) throws IOException {
        ReadLock readLock = LOCK_MAP.get(player).readLock();
        readLock.lock();

        Path saveFile = getSaveFile(player);
        if (!Files.exists(saveFile)) {
            readLock.unlock();
            throw new FileNotFoundException(saveFile.toString());
        }

        Type homeMapType = (new TypeToken<LinkedHashMap<String, PlayerHome>>(){}).getType();
        Map<String, PlayerHome> map = GSON.fromJson(new JsonReader(Files.newBufferedReader(saveFile)), homeMapType);
        
        readLock.unlock();
        return map;
    }

    public static Path getSaveFile(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();

        Path saveFolder = server.getSavePath(WorldSavePath.PLAYERDATA).resolve(DEFAULT_FOLDER_NAME);
        try {
            Files.createDirectory(saveFolder);
        } catch (FileAlreadyExistsException e) { // We don't care if it already exists
        } catch (IOException e) {
            LOGGER.error("Unable to create directory for saving player homes", e);
        }

        Path saveFile = saveFolder.resolve(player.getUuidAsString() + ".json");

        return saveFile;
    }
}
