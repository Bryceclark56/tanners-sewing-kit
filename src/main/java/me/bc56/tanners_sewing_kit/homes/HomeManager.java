package me.bc56.tanners_sewing_kit.homes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class HomeManager {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final int HOME_LIMIT = 3;
    public static final String DEFAULT_HOME_NAME = "home";

    public static final Map<ServerPlayerEntity, ReentrantReadWriteLock> saveLockMap = new HashMap<>(); // Default
                                                                                                       // capacity of 16

    public static void initialize() {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            saveLockMap.put(player, new ReentrantReadWriteLock());

            Map<String, PlayerHome> homes = null;
            try {
                Future<Map<String, PlayerHome>> future = ThreadManager.EXECUTOR.submit(() -> readHomes(player));
                homes = future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Unable to load homes from file of player: {}", player.getName().asString(), e);
                return;
            }

            if (homes == null) return; // TODO: Better confirmation of no pre-existing homes

            ((HomeMixinAccess) player).getHomes().putAll(homes);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;

            saveLockMap.remove(player);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            // Transfer homes to new object
            Map<String, PlayerHome> homes = ((HomeMixinAccess) newPlayer).getHomes();
            homes.putAll(((HomeMixinAccess) oldPlayer).getHomes());

            // Transfer homes save file lock to new player object
            ReentrantReadWriteLock lock = saveLockMap.remove(oldPlayer);
            if (lock == null) {
                lock = new ReentrantReadWriteLock();
            }
            saveLockMap.put(newPlayer, lock);
        });
    }

    public static void setNewHome(ServerPlayerEntity player) throws CommandSyntaxException {
        setNewHome(player, DEFAULT_HOME_NAME);
    }

    public static void setNewHome(ServerPlayerEntity player, String homeName) throws CommandSyntaxException {
        Map<String, PlayerHome> homes = ((HomeMixinAccess)player).getHomes();
        homeName = homeName.toLowerCase();

        if (((HomeMixinAccess)player).getHomes().size() >= HOME_LIMIT && !homes.containsKey(homeName)) {
            throw new SimpleCommandExceptionType(new LiteralText("You have reached the limit of how many homes you may have. (" + HOME_LIMIT + ")").formatted(Formatting.RED)).create();
        }

        PlayerHome newHome = PlayerHome.createAt(player);
        homes.put(homeName, newHome);

        // Only show name if not default home
        if (homeName.equals("home")) {
            player.sendMessage(new LiteralText("Home set").formatted(Formatting.GOLD), false);
        }
        else {
            player.sendMessage(new LiteralText("Home ").formatted(Formatting.GOLD).append(new LiteralText(homeName).formatted(Formatting.RESET).formatted(Formatting.WHITE)).append(new LiteralText(" set").formatted(Formatting.GOLD)), false);
        }

        syncHomesFile(player);
    }

    public static void sendToHome(ServerPlayerEntity player) {
        sendToHome(player, DEFAULT_HOME_NAME);
    }

    public static void sendToHome(ServerPlayerEntity player, String homeName) {
        homeName = homeName.toLowerCase();

        PlayerHome home = ((HomeMixinAccess) player).getHome(homeName);

        if (home == null) return;

        BackCommand.setLastLocation(player);

        // Ensure chunk is loaded
        ChunkPos chunkPos = new ChunkPos(new BlockPos(home.x, home.y, home.z));
        home.dimension.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
        
        player.teleport(home.dimension, home.x, home.y, home.z, home.yaw, home.pitch);
        player.networkHandler.syncWithPlayerPosition(); // Not sure why this is needed, but it prevents a warning in the console
    }

    public static void removeHome(ServerPlayerEntity player) {
        removeNamedHome(player, DEFAULT_HOME_NAME);
    }

    public static void removeNamedHome(ServerPlayerEntity player, String homeName) {
        Map<String, PlayerHome> homes = ((HomeMixinAccess) player).getHomes();
        homeName = homeName.toLowerCase();

        if (!homes.containsKey(homeName)) return;

        homes.remove(homeName);


        syncHomesFile(player);
    }

    // Only useful for writes
    public static void syncHomesFile(ServerPlayerEntity player) {
        ThreadManager.EXECUTOR.submit(() -> writeHomes(player)); //TODO: Determine if this is too often
    }

    public static void writeHomes(ServerPlayerEntity player) {
        ReentrantReadWriteLock lock = saveLockMap.get(player);
        lock.writeLock().lock();

        Map<String, PlayerHome> homes = ((HomeMixinAccess) player).getHomes();

        if (homes == null || homes.isEmpty()) {
            lock.writeLock().unlock();
            return;
        };

        File saveFile = getSaveFile(player);

        try {
            saveFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            lock.writeLock().unlock();
            return;
        }

        Gson gson = new GsonBuilder()
                        .setPrettyPrinting()
                        .registerTypeAdapter(PlayerHome.class, new PlayerHomeTypeAdapter())
                        .create();

        try(FileWriter writer = new FileWriter(saveFile, false)) {

            String jsonEncoded = gson.toJson(homes);

            writer.write(jsonEncoded);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static Map<String, PlayerHome> readHomes(ServerPlayerEntity player) {
        ReentrantReadWriteLock lock = saveLockMap.get(player);
        lock.readLock().lock();

        File saveFile = getSaveFile(player);

        if (!saveFile.exists()) {
            lock.readLock().unlock();
            return null; // TODO: Throw custom exception instead
        }

        FileReader reader = null;
        try {
            reader = new FileReader(saveFile);
        } catch (FileNotFoundException e) {
            // Hopefully this doesn't happen
            e.printStackTrace();

            lock.readLock().unlock();
            return null;
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(PlayerHome.class, new PlayerHomeTypeAdapter())
            .create();

        Type homeMapType = new TypeToken<LinkedHashMap<String, PlayerHome>>(){}.getType();

        Map<String, PlayerHome> map = gson.fromJson(new JsonReader(reader), homeMapType);
        lock.readLock().unlock();
        return map;
    }

    public static File getSaveFile(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();

        // TODO: Grab actual world folder name
        File savePath = server.getFile("world/playerdata/playerhomes/");
        savePath.mkdirs(); // Ensures folders exist to prevent exception
        File saveFile =  savePath.toPath().resolve(player.getUuidAsString() + ".json").toFile();

        return saveFile;
    }
}
