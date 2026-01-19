package dev.buildit.hytale

import com.hypixel.hytale.codec.builder.BuilderCodec
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit


class CreatePlugin(init: JavaPluginInit) : JavaPlugin(init)
{
    companion object
    {
        private val LOGGER = HytaleLogger.forEnclosingClass()
    }

    init
    {
        LOGGER.atInfo().log("Initialized $name@${manifest.version}")
    }

    override fun setup()
    {
        LOGGER.atFine().log(":setup")
    }
}
