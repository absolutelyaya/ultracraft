package absolutelyaya.ultracraft.data;

import absolutelyaya.ultracraft.Ultracraft;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TerminalScreensaverManager extends JsonDataLoader
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static Map<Identifier, String[]> screensavers = ImmutableMap.of();
	private static Random random;
	
	public TerminalScreensaverManager()
	{
		super(GSON, "ultracraft/screensavers");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return new Identifier(Ultracraft.MOD_ID, "ultracraft/screensavers");
			}
			
			@Override
			public void reload(ResourceManager manager)
			{
				apply(prepare(manager, null), manager, null);
			}
		});
		random = Random.create();
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> map, ResourceManager manager, Profiler profiler)
	{
		
		ImmutableMap.Builder<Identifier, String[]> builder = ImmutableMap.builder();
		for (Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
			Identifier id = entry.getKey();
			try
			{
				JsonArray array = JsonHelper.getArray(entry.getValue().getAsJsonObject(), "lines");
				List<String> list = new ArrayList<>();
				array.forEach(i -> list.add(i.getAsString()));
				builder.put(id, list.toArray(new String[0]));
			}
			catch (JsonParseException | IllegalArgumentException exception)
			{
				Ultracraft.LOGGER.error("Failed to parse Screensaver '" + id + "'" , exception);
			}
		}
		screensavers = builder.build();
		Ultracraft.LOGGER.info("Loaded " + screensavers.size() + " Default Screensavers");
	}
	
	public static String[] getRandomScreensaver()
	{
		Identifier[] keys = screensavers.keySet().toArray(new Identifier[0]);
		return screensavers.get(keys[random.nextInt(keys.length)]);
	}
}
