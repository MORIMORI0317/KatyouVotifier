package red.felnull.katyouvotifier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import red.felnull.katyouvotifier.handler.ServerHandler;

@Mod(KatyouVotifier.MODID)
public class KatyouVotifier {
    private static final Logger LOGGER = LogManager.getLogger(KatyouVotifier.class);
    public static final String MODID = "katyouvotifier";
    public static ForgeVotifierPlugin PLUGIN;

    public KatyouVotifier() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        PLUGIN = new ForgeVotifierPlugin(LOGGER);
        MinecraftForge.EVENT_BUS.register(ServerHandler.class);
        // MinecraftForge.EVENT_BUS.register(TestHandler.class);
    }


}
