package io.github.picapi.mc.fabric.client.serverspecificskins;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSpecificSkins implements ModInitializer {

    public static final Logger LOGGER
            = LoggerFactory.getLogger("ServerSpecificSkins");
    @Override
    public void onInitialize() {
        LOGGER.info("Mod initialising...");
        LOGGER.info("Mod initialised!");
    }
}
