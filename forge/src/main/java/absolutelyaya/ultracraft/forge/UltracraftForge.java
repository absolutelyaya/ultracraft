package absolutelyaya.ultracraft.forge;

import absolutelyaya.ultracraft.Ultracraft;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Ultracraft.MOD_ID)
public class UltracraftForge
{
    public UltracraftForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Ultracraft.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Ultracraft.init();
    }
}
