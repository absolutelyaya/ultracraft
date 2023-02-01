package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.BlockTagRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import dev.architectury.event.events.common.TickEvent;

public class Ultracraft
{
    public static final String MOD_ID = "ultracraft";
    static int freezeTicks;
    
    public static void init()
    {
        BlockRegistry.register();
        ItemRegistry.register();
        PacketRegistry.register();
        BlockTagRegistry.register();
    
        TickEvent.SERVER_POST.register(minecraft -> {
            if(freezeTicks > 0)
            {
                freezeTicks--;
            }
        });
    }
    
    public static boolean isTimeFrozen()
    {
        return freezeTicks > 0;
    }
    
    public static void Freeze(int ticks)
    {
        freezeTicks += ticks;
    }
}
