package me.bc56.tanners_sewing_kit;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.bc56.tanners_sewing_kit.command.Commands;
import me.bc56.tanners_sewing_kit.common.PlayerLocation;
import me.bc56.tanners_sewing_kit.homes.HomeManager;
import me.bc56.tanners_sewing_kit.sleep.SleepyBois;
import me.bc56.tanners_sewing_kit.tpa.TeleportMixinAccess;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class TannersSewingKit implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    public static MinecraftServer server;

    @Override
    public void onInitializeServer() {
        LOGGER.info("OwO What's this?");
        LOGGER.info("A server that wants me?");
        LOGGER.info("UwU");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            TannersSewingKit.server = server;
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            try {
                ThreadManager.EXECUTOR.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Problem while shutting down executor", e);
                ThreadManager.EXECUTOR.shutdown();
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            PlayerLocation location = new PlayerLocation(oldPlayer.getServerWorld(), oldPlayer.getX(), oldPlayer.getY(), oldPlayer.getZ());
            ((TeleportMixinAccess)newPlayer).setLastLocation(location);
        });

        HomeManager.initialize();
        SleepyBois.initialize();

        Commands.register();
    }
    
}
