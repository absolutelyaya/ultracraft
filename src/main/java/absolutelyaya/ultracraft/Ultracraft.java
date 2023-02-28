package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;

public class Ultracraft implements ModInitializer
{
    public static final String MOD_ID = "ultracraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    static int freezeTicks;
    public static Option FreezeOption = Option.FREE;
    public static Option HiVelOption = Option.FREE;
    
    @Override
    public void onInitialize()
    {
        EntityRegistry.register();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.register();
        ItemRegistry.register();
        PacketRegistry.registerC2S();
        BlockTagRegistry.register();
        SoundRegistry.register();
        KeybindRegistry.register();
    
        ServerTickEvents.END_SERVER_TICK.register(minecraft -> {
            if(freezeTicks > 0)
                freezeTicks--;
        });
    
        LOGGER.info("Ultracraft initialized.");
    }
    
    public static boolean isTimeFrozen()
    {
        return freezeTicks > 0;
    }
    
    public static void freeze(int ticks)
    {
        if(!UltracraftClient.isFreezeEnabled())
            return;
        freezeTicks += ticks;
        LOGGER.info("Freezing for " + ticks + " ticks. (Intentional Visual Effect! Do not report!)");
    }
    
    public enum Option
    {
        FORCE_ON,
        FORCE_OFF,
        FREE
    }
}
