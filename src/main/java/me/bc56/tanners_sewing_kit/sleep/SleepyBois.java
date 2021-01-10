package me.bc56.tanners_sewing_kit.sleep;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import me.bc56.tanners_sewing_kit.TannersSewingKit;
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
    public static final double GOAL_PERCENT = 0.5;

    public static Set<ServerPlayerEntity> existingSleepyBois = new HashSet<>();

    public static void initialize() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (!world.getDimension().isBedWorking())
                return;

            if (world.getPlayers().isEmpty()) {
                return;
            }

            Set<ServerPlayerEntity> playersSleeping = world.getPlayers().stream().filter(p -> p.isSleepingLongEnough())
                    .collect(Collectors.toSet());

            int goal = (int)Math.ceil(world.getServer().getCurrentPlayerCount() * GOAL_PERCENT);

            // Remove anyone not actively sleeping from existing sleepers set
            Set<ServerPlayerEntity> difference = new HashSet<>(existingSleepyBois);
            difference.removeAll(playersSleeping);
            existingSleepyBois.removeAll(difference);

            playersSleeping.stream().filter(p -> !existingSleepyBois.contains(p)).forEach(p -> {
                existingSleepyBois.add(p);

                MinecraftServer server = p.getServer();

                MutableText playerNameText = new LiteralText(p.getName().getString()).formatted(Formatting.WHITE);
                MutableText baseMessage = new LiteralText(" is now sleeping. (" + existingSleepyBois.size() + "/" + goal + ")").formatted(Formatting.GREEN);

                server.getPlayerManager().broadcastChatMessage(playerNameText.append(baseMessage), MessageType.CHAT, Util.NIL_UUID);
            });

            if (existingSleepyBois.size() >= goal) {
                if (world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                    long l = world.getLevelProperties().getTimeOfDay() + 24000L;
                    world.setTimeOfDay(l - l % 24000L);
                }
        
                playersSleeping.stream().forEach(p -> {
                    p.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
                    p.wakeUp();
                });

                TannersSewingKit.LOGGER.info("What is going on here?");

                if (world.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)) {
                    ((ServerWorldSleepMixin)world).invokeResetWeather();
                }
                
                existingSleepyBois.clear();
            }
        });
    }
}
