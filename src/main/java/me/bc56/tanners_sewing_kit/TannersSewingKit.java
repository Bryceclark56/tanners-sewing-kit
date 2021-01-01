package me.bc56.tanners_sewing_kit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.bc56.tanners_sewing_kit.command.FunCommand;
import me.bc56.tanners_sewing_kit.command.HomeCommand;
import me.bc56.tanners_sewing_kit.command.TpAcceptCommand;
import me.bc56.tanners_sewing_kit.command.TpHereCommand;
import me.bc56.tanners_sewing_kit.command.TpaCommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class TannersSewingKit implements DedicatedServerModInitializer {
    public static Logger logger = LogManager.getLogger();

    @Override
    public void onInitializeServer() {
        logger.info("OwO What's this?");
        logger.info("A server that wants me?");
        logger.info("UwU");

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            logger.info("Registering commands");
            TpaCommand.register(dispatcher);
            TpHereCommand.register(dispatcher);
            TpAcceptCommand.register(dispatcher);
            HomeCommand.register(dispatcher);
            FunCommand.register(dispatcher);
        });
    }
    
}
