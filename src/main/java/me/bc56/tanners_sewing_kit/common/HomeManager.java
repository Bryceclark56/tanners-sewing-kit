package me.bc56.tanners_sewing_kit.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class HomeManager {
    public static final int HOME_LIMIT = 3;
    public static final String DEFAULT_HOME_NAME = "home";

    public static MinecraftServer server;

    public static void initHomeManager(MinecraftServer server) {
        HomeManager.server = server;
    }

    public static void setNewHome(ServerPlayerEntity player) throws CommandSyntaxException {
        setNewHome(player, DEFAULT_HOME_NAME);
    }

    public static void setNewHome(ServerPlayerEntity player, String homeName) throws CommandSyntaxException {
        Map<String, PlayerHome> homes = ((HomeMixinAccess)player).getHomes();

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

        writeHomes(player); //TODO: Determine if this is too often
    }

    public static void sendToHome(ServerPlayerEntity player) {
        sendToHome(player, DEFAULT_HOME_NAME);
    }

    public static void sendToHome(ServerPlayerEntity player, String homeName) {
        PlayerHome home = ((HomeMixinAccess) player).getHome(homeName);

        if (home == null) return;

        player.teleport(home.dimension, home.x, home.y, home.z, home.yaw, home.pitch);
        player.networkHandler.syncWithPlayerPosition(); // Not sure why this is needed, but it prevents a warning in the console
    }

    public static void removeHome(ServerPlayerEntity player) {
        removeNamedHome(player, DEFAULT_HOME_NAME);
    }

    public static void removeNamedHome(ServerPlayerEntity player, String homeName) {
        Map<String, PlayerHome> homes = ((HomeMixinAccess) player).getHomes();

        if (!homes.containsKey(homeName)) return;

        homes.remove(homeName);
        writeHomes(player);
    }

    public static void writeHomes(ServerPlayerEntity player) {
        Map<String, PlayerHome> homes = ((HomeMixinAccess) player).getHomes();

        if (homes == null || homes.isEmpty()) return;

        File saveFile = getSaveFile(player);

        try {
            saveFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
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
        }
    }

    public static Map<String, PlayerHome> readHomes(ServerPlayerEntity player) {
        File saveFile = getSaveFile(player);

        if (!saveFile.exists()) {
            return null;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(saveFile);
        } catch (FileNotFoundException e) {
            // Hopefully this doesn't happen
            e.printStackTrace();

            return null;
        }

        Gson gson = new GsonBuilder()
            .registerTypeAdapter(PlayerHome.class, new PlayerHomeTypeAdapter())
            .create();

        Type homeMapType = new TypeToken<HashMap<String, PlayerHome>>(){}.getType();

        return gson.fromJson(new JsonReader(reader), homeMapType);
    }

    public static File getSaveFile(ServerPlayerEntity player) {
        // TODO: Grab actual world folder name
        File savePath = server.getFile("world/playerdata/playerhomes/");
        savePath.mkdirs(); // Ensures folders exist to prevent exception
        File saveFile =  savePath.toPath().resolve(player.getUuidAsString() + ".json").toFile();

        return saveFile;
    }
}
