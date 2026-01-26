// Copyright © Ádám Liszkai, "Kicsivazz" - MIT in LICENSE.md - SPDX-License-Identifier: MIT

package dev.scaffoldit.devtools

import com.hypixel.hytale.common.plugin.PluginIdentifier
import com.hypixel.hytale.logger.HytaleLogger
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent
import com.hypixel.hytale.server.core.plugin.JavaPlugin
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import com.hypixel.hytale.server.core.plugin.PluginBase
import com.hypixel.hytale.server.core.plugin.PluginManager
import com.hypixel.hytale.server.core.universe.Universe
import kotlinx.coroutines.*
import net.bytebuddy.agent.ByteBuddyAgent
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
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
    eventRegistry.register(PlayerConnectEvent::class.java) { event ->
      if (isHotSwapping) event.playerRef.sendMessage(
        Message.raw("[DEV] Hot Swapping is enabled, watch for chat and console for updates!")
      )
    }
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
    private val console = HytaleLogger.get("HotReload")
    private val pendingReloads = ConcurrentHashMap<PluginIdentifier, Long>()
    private val debounceMs = 300L

    val pluginScopes: Map<String, PluginBase>
      get() = PluginManager.get().plugins.filterNotNull()
        .filter { it.manifest.main != null && !it.manifest.main!!.startsWith("com.hypixel.hytale") }
        .associateBy { it.manifest.main!!.substringBeforeLast('.') }

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
        scheduleReload(foundPlugin)
      }
      return null
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private fun scheduleReload(plugin: PluginBase) {
      val now = System.currentTimeMillis()
      pendingReloads[plugin.identifier] = now
      scope.launch {
        delay(debounceMs)
        if (pendingReloads[plugin.identifier] != now) return@launch
        pendingReloads.remove(plugin.identifier)
        reload(plugin).also {
          console.atInfo().log("Reload finished for ${plugin.identifier}")
          updatePlayers("[DEV] Reloaded ${plugin.identifier} successfully!")
        }
      }
    }

    private fun updatePlayers(message: String) {
      Universe.get().players.forEach {
        it.sendMessage(Message.raw(message))
      }
    }

    private fun reload(plugin: PluginBase) {
      when (true) {
        plugin.manifest.dependencies.isNotEmpty() -> reloadFallback(plugin)
        else -> {
          console.atInfo().log("Reload ${plugin.identifier} via PluginManager")
          PluginManager.get().reload(plugin.identifier)
        }
      }
    }

    private val pluginShutdown = PluginBase::class.java
      .getDeclaredMethod("shutdown").apply { isAccessible = true }

    private val pluginSetup = PluginBase::class.java
      .getDeclaredMethod("setup").apply { isAccessible = true }

    private val pluginStart = PluginBase::class.java
      .getDeclaredMethod("start").apply { isAccessible = true }

    private fun reloadFallback(plugin: PluginBase) {
      console.atInfo().log(
        "Reload ${plugin.identifier} via fallback simulation, " +
          "This is a temporary workaround until a proper reload with dependencies added."
      )
      /**
       * Core lifecycle done in the Plugin Manager but without modifying
       * the actual plugin list or checking for any dependency states.
       *
       * We skip unloading the whole Java class instance itself; that is
       * intended to be kept alive with Hot Swapping, and so anything
       * stored within the instance.
       *
       * Note that this may have unintended consequences, and setting
       * no dependencies in the manifest while expecting them to be there
       * is more stable, until Hypixel adds a proper reload feature.
       */
      with(HytaleServer.get()) {
        pluginShutdown.invoke(plugin).also { doneStop(plugin) }
        console.atWarning().log(
          "Initialization skipped for ${plugin.identifier}, " +
            "remove your dependencies in the manifest for the full reload!"
        )
        pluginSetup.invoke(plugin).also { doneSetup(plugin) }
        pluginStart.invoke(plugin).also { doneStart(plugin) }
      }.also {
        updatePlayers("[DEV] Warnings logged during the reload, please check your console!")
      }
    }
  }
}