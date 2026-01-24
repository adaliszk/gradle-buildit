package dev.scaffoldit.example;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin.
 * Use the setup method to register into game registries or add event listeners!
 */
public class JavaExample extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public JavaExample(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Initialized " + this.getName());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Loading " + this.getName());
    }
}