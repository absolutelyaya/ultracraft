package absolutelyaya.ultracraft.data;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.recipe.UltraRecipe;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UltraRecipeManager extends JsonDataLoader
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static Map<Identifier, UltraRecipe> recipes = ImmutableMap.of();
	
	public UltraRecipeManager()
	{
		super(GSON, "ultracraft/recipe");
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener()
		{
			@Override
			public Identifier getFabricId()
			{
				return new Identifier(Ultracraft.MOD_ID, "ultracraft/recipe");
			}
			
			@Override
			public void reload(ResourceManager manager)
			{
				apply(prepare(manager, null), manager, null);
			}
		});
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> map, ResourceManager manager, Profiler profiler)
	{
		ImmutableMap.Builder<Identifier, UltraRecipe> builder = ImmutableMap.builder();
		for (Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
			Identifier id = entry.getKey();
			try
			{
				UltraRecipe recipe = UltraRecipe.deserialize(id, (JsonObject)entry.getValue());
				builder.put(id, recipe);
			}
			catch (JsonParseException | IllegalArgumentException exception)
			{
				Ultracraft.LOGGER.error("Failed to parse UltraRecipe '" + id + "'", exception);
			}
		}
		recipes = builder.build();
		Ultracraft.LOGGER.info("Loaded " + recipes.size() + " Ultra-Recipes");
	}
	
	public static void setRecipes(Map<Identifier, UltraRecipe> recipes)
	{
		UltraRecipeManager.recipes = recipes;
	}
	
	public static UltraRecipe getRecipe(Identifier id)
	{
		return recipes.get(id);
	}
	
	public static void sync(ServerPlayerEntity player)
	{
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		List<Pair<Identifier, UltraRecipe>> list = new ArrayList<>();
		for (Map.Entry<Identifier, UltraRecipe> entry : recipes.entrySet())
			list.add(new Pair<>(entry.getKey(), entry.getValue()));
		buf.writeCollection(list, UltraRecipe::serialize);
		ServerPlayNetworking.send(player, PacketRegistry.ULTRA_RECIPE_PACKET_ID, buf);
	}
}
