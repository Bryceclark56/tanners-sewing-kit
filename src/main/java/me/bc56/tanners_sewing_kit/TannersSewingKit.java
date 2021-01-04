package me.bc56.tanners_sewing_kit;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.bc56.tanners_sewing_kit.command.FunCommand;
import me.bc56.tanners_sewing_kit.command.GenChunksCommand;
import me.bc56.tanners_sewing_kit.command.HomeCommand;
import me.bc56.tanners_sewing_kit.command.TpAcceptCommand;
import me.bc56.tanners_sewing_kit.command.TpHereCommand;
import me.bc56.tanners_sewing_kit.command.TpaCommand;
import me.bc56.tanners_sewing_kit.common.HomeManager;
import me.bc56.tanners_sewing_kit.common.HomeMixinAccess;
import me.bc56.tanners_sewing_kit.common.PlayerHome;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class TannersSewingKit implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeServer() {
        LOGGER.info("OwO What's this?");
        LOGGER.info("A server that wants me?");
        LOGGER.info("UwU");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            HomeManager.initHomeManager(server);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            Map<String, PlayerHome> homes = HomeManager.readHomes(player);


            if (homes == null) return;

            ((HomeMixinAccess) player).getHomes().putAll(homes);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!dedicated) return;

            LOGGER.debug("Registering commands");
            TpaCommand.register(dispatcher);
            TpHereCommand.register(dispatcher);
            TpAcceptCommand.register(dispatcher);
            HomeCommand.register(dispatcher);
            FunCommand.register(dispatcher);
            GenChunksCommand.register(dispatcher);
        });
    }
    
}
