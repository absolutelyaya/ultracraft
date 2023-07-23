package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.gui.screen.PedestalScreen;
import absolutelyaya.ultracraft.client.gui.screen.PedestalScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ScreenHandlerRegistry
{
	public static final ScreenHandlerType<PedestalScreenHandler> PEDESTAL = Registry.register(Registries.SCREEN_HANDLER,
			new Identifier(Ultracraft.MOD_ID,  "pedestal"), new ScreenHandlerType<>(PedestalScreenHandler::createPedestalHandler, FeatureSet.empty()));
	
	public static void register()
	{
		HandledScreens.register(PEDESTAL, PedestalScreen::new);
	}
}
