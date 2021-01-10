package me.bc56.tanners_sewing_kit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.bc56.tanners_sewing_kit.command.Commands;
import me.bc56.tanners_sewing_kit.homes.HomeManager;
import me.bc56.tanners_sewing_kit.sleep.SleepyBois;
import net.fabricmc.api.DedicatedServerModInitializer;
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

        HomeManager.initialize();
        SleepyBois.initialize();

        Commands.register();
    }
    
}
