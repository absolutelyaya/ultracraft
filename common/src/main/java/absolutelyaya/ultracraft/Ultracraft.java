package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.*;
import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import org.slf4j.Logger;

public class Ultracraft
{
    public static final String MOD_ID = "ultracraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    static int freezeTicks;
    
    public static void init()
    {
        EntityRegistry.register();
        BlockRegistry.register();
        BlockEntityRegistry.register();
        ItemRegistry.register();
        PacketRegistry.register();
        BlockTagRegistry.register();
        SoundRegistry.register();
    
        TickEvent.SERVER_POST.register(minecraft -> {
            if(freezeTicks > 0)
            {
                freezeTicks--;
            }
        });
        
        if(Platform.isForge())
            LifecycleEvent.SETUP.register(UltracraftClient::init);
        
        LOGGER.info("Ultracraft initialized.");
    }
    
    public static boolean isTimeFrozen()
    {
        return freezeTicks > 0;
    }
    
    public static void freeze(int ticks)
    {
        freezeTicks += ticks;
        LOGGER.info("Freezing for " + ticks + " ticks. (Intentional Visual Effect! Do not report!)");
        //TODO: add freeze effect option. It'd be a good idea to make it server based and synced to the client upon joining due to otherwise unavoidable desyncing.
    }
}
