package me.bc56.tanners_sewing_kit.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class Commands {
    public static final Logger LOGGER = LogManager.getLogger();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!dedicated) {
                LOGGER.warn("Not a dedicated server! Pwease don't use me here >_<");
                return;
            };

            LOGGER.debug("Registering commands");
            TpaCommands.register(dispatcher);
            HomeCommand.register(dispatcher);
            FunCommand.register(dispatcher);
            GenChunksCommand.register(dispatcher);
            BackCommand.register(dispatcher);
        });
    }
}
