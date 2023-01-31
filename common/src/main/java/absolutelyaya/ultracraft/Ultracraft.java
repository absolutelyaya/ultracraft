package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;

public class Ultracraft
{
    public static final String MOD_ID = "ultracraft";
    
    public static void init()
    {
        BlockRegistry.register();
        ItemRegistry.register();
    }
}
