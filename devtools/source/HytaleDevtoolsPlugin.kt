package dev.scaffoldit.devtools

import com.hypixel.hytale.common.plugin.PluginIdentifier
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.plugin.PluginManager
import com.hypixel.hytale.server.core.universe.Universe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.bytebuddy.agent.ByteBuddyAgent
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import java.util.function.Consumer
import java.util.concurrent.ConcurrentHashMap


class HytaleDevtoolsPlugin(init: JavaPluginInit) : JavaPlugin(init) {
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

    fun instrumentHotSwapping() {
        try {
            ByteBuddyAgent.install().addTransformer(HotSwapListener(), true)
            log.log("HotSwap detection enabled via ByteBuddy")
            isHotSwapping = true
        } catch (e: Exception) {
            log.log("Could not attach instrumentation: ${e.message}")
        }
    }

    private class HotSwapListener : ClassFileTransformer {
        private val pendingReloads = ConcurrentHashMap<PluginIdentifier, Long>()
        private val debounceMs = 300L

        val pluginScopes: Map<String, PluginIdentifier>
            get() = PluginManager.get().plugins.filterNotNull()
                .filter { it.manifest.main != null && !it.manifest.main!!.startsWith("com.hypixel.hytale") }
                .associate { it.manifest.main!!.substringBeforeLast('.') to it.identifier }

        override fun transform(
            loader: ClassLoader?,
            className: String,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray
        ): ByteArray? {
            if (classBeingRedefined != null) {
                val simpleName = className.replace('/', '.')
                val foundPlugin = pluginScopes.filter { simpleName.startsWith(it.key) }
                    .values.firstOrNull() ?: return null

                log.log("HotSwap of '$simpleName' detected!")
                scheduleReload(foundPlugin)
            }
            return null
        }

        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        private fun scheduleReload(plugin: PluginIdentifier) {
            val now = System.currentTimeMillis()
            pendingReloads[plugin] = now

            scope.launch {
                delay(debounceMs)
                log.log("Scheduled reload for ${plugin.group}:${plugin.name}")
                if (pendingReloads[plugin] == now) {
                    pendingReloads.remove(plugin)
                    sendMessage("[DEV] Hot Swapping ${plugin.group}:${plugin.name}")
                    PluginManager.get().reload(plugin)
                    sendMessage("[DEV] Reloaded ${plugin.group}:${plugin.name}")
                }
            }
        }

        fun sendMessage(message: String) {
            Universe.get().players.forEach { it.sendMessage(Message.raw(message)) }
        }
    }
}