package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.recipe.PlushieRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RecipeSerializers
{
	public static PlushieRecipe.PlushieRecipeSerializer PLUSHIE_SERIALIZER =
			Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(Ultracraft.MOD_ID, "plushie"), new PlushieRecipe.PlushieRecipeSerializer());
	
	public static void register()
	{
	
	}
}
