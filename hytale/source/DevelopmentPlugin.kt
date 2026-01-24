package dev.scaffoldit.hytale

import com.hypixel.hytale.event.IBaseEvent
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.plugin.PluginManager
import com.hypixel.hytale.server.core.universe.Universe
import net.bytebuddy.agent.ByteBuddyAgent
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import java.util.function.Consumer


class DevelopmentPlugin(init: JavaPluginInit) : JavaPlugin(init) {
    companion object {
        private val log = HytaleLogger.forEnclosingClass().atInfo()
    }

    private var isHotSwapping = false

    init {
        instrumentHotSwapping()
    }

    override fun setup() {
        eventRegistry.register(
            PlayerConnectEvent::class.java,
            Consumer { event: PlayerConnectEvent ->
                if (isHotSwapping) event.playerRef.sendMessage(Message.raw("[DEV] Hot Swapping is enabled, watch for the chat for updates!"))
            })
    }

    fun collectPluginScopes() {
        val plugins = PluginManager.get().plugins.filterNotNull()
            .filter { it.manifest.main != null && !it.manifest.main!!.startsWith("com.hypixel.hytale") }
            .associate { it.identifier to it.manifest.main!! }

        log.log("Plugins to HotSwap: $plugins")
    }

    fun instrumentHotSwapping() {
        try {
            ByteBuddyAgent.install().addTransformer(HotSwapListener(), true)
            log.log("HotSwap detection enabled via ByteBuddy")
            isHotSwapping = true
            collectPluginScopes()
        } catch (e: Exception) {
            log.log("Could not attach instrumentation: ${e.message}")
        }
    }

    private class HotSwapListener : ClassFileTransformer {
        override fun transform(
            loader: ClassLoader?,
            className: String,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray
        ): ByteArray? {
            if (classBeingRedefined != null) {
                log.log("HotSwap: ${className.replace('/', '.')}")
                Universe.get().players.forEach { playerRef ->
                    playerRef.sendMessage(Message.raw("[DEV] Hot Swapping $className"))
                }
            }
            return null
        }
    }
}