package io.github.tidefox.clientquirks.core;

import java.util.Map;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Coremod entry point. Its only job is to hand our mixin config to FermiumBooter
 * as an EARLY mixin (vanilla targets), and only on the physical client: every
 * target class is client-only, and {@code clientSideOnly} on the @Mod does not
 * stop a coremod from loading on a dedicated server.
 *
 * Constructor: if FermiumBooter is already on the classpath, enqueue through its API.
 *
 * injectData: FML constructs coremods in jar-name order, and "clientquirks-*.jar" sorts
 * before "fermiumbooter-*.jar", so the constructor usually runs too early. By the time
 * our injectData runs, Mixin has co-initialised FermiumBooter and FermiumBooter has
 * already handed its queue to Mixin (seen in a real client boot), so a late
 * enqueueMixin would be silently dropped. Instead we add the config through Mixin's
 * own API, exactly as FermiumBooter does; Mixin has not selected configs yet then.
 *
 * Presence is checked by resource lookup so a missing FermiumBooter/Mixin never
 * causes a sticky NoClassDefFoundError.
 */
@IFMLLoadingPlugin.Name("ClientQuirks")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(500)
@IFMLLoadingPlugin.TransformerExclusions({"io.github.tidefox.clientquirks.core."})
public class CQLoadingPlugin implements IFMLLoadingPlugin {

    public static final String MIXIN_CONFIG = "mixins.clientquirks.json";
    private static final Logger LOGGER = LogManager.getLogger("clientquirks");

    private static boolean done = false;

    public CQLoadingPlugin() {
        if (done || !onClient()) return;
        if (FermiumCompat.isPresent()) {
            FermiumCompat.enqueueEarly(MIXIN_CONFIG);
            done = true;
            LOGGER.info("Enqueued {} with FermiumBooter for early mixin application", MIXIN_CONFIG);
        }
    }

    @Override
    public void injectData(Map<String, Object> data) {
        if (done || !onClient()) return;
        done = true;
        if (MixinCompat.isPresent()) {
            MixinCompat.addConfiguration(MIXIN_CONFIG);
            LOGGER.info("Added {} to Mixin directly for early mixin application", MIXIN_CONFIG);
        } else {
            LOGGER.error("Mixin was not found. Client Quirks' fixes will NOT be applied. "
                    + "Install FermiumBooter (>= 1.4.1) in mods/.");
        }
    }

    private static boolean onClient() {
        if (FMLLaunchHandler.side().isClient()) return true;
        if (!done) {
            done = true;
            LOGGER.info("Dedicated server: Client Quirks is client-only, not enqueueing {}", MIXIN_CONFIG);
        }
        return false;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
