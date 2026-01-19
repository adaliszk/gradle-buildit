package dev.buildit.Example

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit


/**
 * This class serves as the entrypoint for your plugin.
 * Use the setup method to register into game registries or add event listeners!
 */
class Plugin(init: JavaPluginInit) : JavaPlugin(init) {
    companion object {
        private val LOGGER = HytaleLogger.forEnclosingClass()
    }

    init {
        LOGGER.atInfo().log("Initialized $name@${manifest.version}")
    }

    override fun setup() {
        LOGGER.atFine().log(":setup")
    }
}
