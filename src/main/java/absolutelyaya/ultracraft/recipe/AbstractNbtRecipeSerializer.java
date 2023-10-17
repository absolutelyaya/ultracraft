package absolutelyaya.ultracraft.recipe;

import com.google.gson.JsonArray;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.collection.DefaultedList;

public abstract class AbstractNbtRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T>
{
	protected DefaultedList<Ingredient> getIngredients(JsonArray json)
	{
		DefaultedList<Ingredient> defaultedList = DefaultedList.of();
		for (int i = 0; i < json.size(); ++i) {
			Ingredient ingredient = Ingredient.fromJson(json.get(i));
			if (ingredient.isEmpty()) continue;
			defaultedList.add(ingredient);
		}
		return defaultedList;
	}
}
