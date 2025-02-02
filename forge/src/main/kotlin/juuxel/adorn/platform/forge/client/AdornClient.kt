package juuxel.adorn.platform.forge.client

import juuxel.adorn.client.gui.screen.AdornMenuScreens
import juuxel.adorn.client.gui.screen.GuideBookScreen
import juuxel.adorn.client.gui.screen.MainConfigScreen
import juuxel.adorn.platform.PlatformBridges
import net.minecraft.client.MinecraftClient
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.util.Identifier
import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object AdornClient {
    fun init() {
        MOD_BUS.addListener(this::setup)
        MOD_BUS.addListener(AdornRenderers::registerRenderers)
        val resourceManager = MinecraftClient.getInstance().resourceManager as ReloadableResourceManagerImpl
        resourceManager.registerReloader(PlatformBridges.resources.bookManager)
        resourceManager.registerReloader(PlatformBridges.resources.colorManager)
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { _, parent -> MainConfigScreen(parent) }
        }
    }

    private fun setup(event: FMLClientSetupEvent) {
        AdornMenuScreens.register()
    }

    fun openBookScreen(bookId: Identifier) {
        MinecraftClient.getInstance().setScreen(GuideBookScreen(PlatformBridges.resources.bookManager[bookId]))
    }
}
