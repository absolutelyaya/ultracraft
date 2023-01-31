package absolutelyaya.ultracraft.fabric.client;

import absolutelyaya.ultracraft.client.UltracraftClient;
import net.fabricmc.api.ClientModInitializer;

public class UltracraftClientFabric implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		UltracraftClient.init();
	}
}
