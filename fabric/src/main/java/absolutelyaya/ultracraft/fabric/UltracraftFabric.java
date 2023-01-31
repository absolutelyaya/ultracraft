package absolutelyaya.ultracraft.fabric;

import absolutelyaya.ultracraft.Ultracraft;
import net.fabricmc.api.ModInitializer;

public class UltracraftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Ultracraft.init();
    }
}
