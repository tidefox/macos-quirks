package io.github.tidefox.clientquirks;

import io.github.tidefox.clientquirks.audio.AudioWatcher;
import io.github.tidefox.clientquirks.audio.SoundUi;
import io.github.tidefox.clientquirks.input.KeyResync;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Client-only fixes for 1.12.2 input and audio quirks (mostly macOS). Servers never need it.
 * The fixes themselves live in early mixins enqueued by {@code core.CQLoadingPlugin}.
 */
@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        clientSideOnly = true, acceptableRemoteVersions = "*",
        acceptedMinecraftVersions = "[1.12.2]")
public class ClientQuirks {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_ID);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(SoundUi.RELOAD_KEY);
        MinecraftForge.EVENT_BUS.register(ClientQuirks.class);
        MinecraftForge.EVENT_BUS.register(KeyResync.class);
        MinecraftForge.EVENT_BUS.register(SoundUi.class);
        MinecraftForge.EVENT_BUS.register(AudioWatcher.class);
        if (SelfTest.ENABLED) MinecraftForge.EVENT_BUS.register(SelfTest.class);
        LOGGER.info("Client Quirks {} ready", Tags.VERSION);
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (Tags.MOD_ID.equals(event.getModID())) ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
    }
}
