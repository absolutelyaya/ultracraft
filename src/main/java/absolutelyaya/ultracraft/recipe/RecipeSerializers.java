package absolutelyaya.ultracraft.recipe;

import absolutelyaya.ultracraft.Ultracraft;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RecipeSerializers
{
	public static final PlushieRecipe.Serializer PLUSHIE_SERIALIZER =
			Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Ultracraft.MOD_ID, "plushie"), new PlushieRecipe.Serializer());
	public static final ReinforceWindowRecipe.Serializer REINFORCE_WINDOW_SERIALIZER =
			Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Ultracraft.MOD_ID, "reinforce_window"), new ReinforceWindowRecipe.Serializer());
	
	public static void register()
	{
	
	}
}
