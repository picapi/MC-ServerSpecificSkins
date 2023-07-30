package io.github.picapi.mc.fabric.client.serverspecificskins;

import io.github.picapi.mc.fabric.client.serverspecificskins.migrations.config.ConfigMigrationManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSpecificSkins implements ModInitializer {

    public static final Logger LOGGER
            = LoggerFactory.getLogger("ServerSpecificSkins");
    @Override
    public void onInitialize() {
        LOGGER.info("Mod initialising...");

        LOGGER.debug("Applying any relevant config migrations...");
        ConfigMigrationManager.applyMigrations();

        LOGGER.info("Mod initialised!");
    }
}
