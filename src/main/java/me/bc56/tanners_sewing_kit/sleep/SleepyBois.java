package me.bc56.tanners_sewing_kit.sleep;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import me.bc56.tanners_sewing_kit.mixin.ServerWorldSleepMixin;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

public class SleepyBois {
    // Minimum percentage of players that must be sleeping
    public static final double GOAL_PERCENT = 0.5;

    public static Set<ServerPlayerEntity> previouslySleeping = new HashSet<>(10);

    public static void initialize() {
        //TODO: Deal with possible sleeping on multiple worlds
        // Not an issue on most vanilla servers, but still possible
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (!world.getDimension().isBedWorking()) {
                return;
            }

            List<ServerPlayerEntity> players = world.getPlayers();

            if (players.isEmpty()) {
                return;
            }

            Set<ServerPlayerEntity> currentlySleeping = players.stream().filter(p -> p.isSleepingLongEnough())
                    .collect(Collectors.toSet());

            // Minimum number of players that must be sleeping
            int goal = (int)Math.ceil(players.size() * GOAL_PERCENT);

            // Remove anyone not actively sleeping from previouslySleeping
            previouslySleeping.retainAll(currentlySleeping);

            MinecraftServer server = world.getServer();
            currentlySleeping.stream().filter(p -> !previouslySleeping.contains(p)).forEach(p -> {
                previouslySleeping.add(p);

                MutableText playerNameText = new LiteralText(p.getName().getString()).formatted(Formatting.WHITE);
                MutableText baseMessage = new LiteralText(" is now sleeping. (" + previouslySleeping.size() + "/" + goal + ")").formatted(Formatting.GREEN);

                server.getPlayerManager().broadcastChatMessage(playerNameText.append(baseMessage), MessageType.CHAT, Util.NIL_UUID);
            });

            if (previouslySleeping.size() >= goal) {
                if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                    long l = world.getLevelProperties().getTimeOfDay() + 24000L;
                    world.setTimeOfDay(l - l % 24000L);
                }

                players.forEach(p -> {
                    p.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
                    p.wakeUp();
                });

                if (world.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
                    ((ServerWorldSleepMixin)world).invokeResetWeather();
                }
                
                previouslySleeping.clear();
            }
        });
    }
}
