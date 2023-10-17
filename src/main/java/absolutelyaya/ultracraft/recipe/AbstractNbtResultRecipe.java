package absolutelyaya.ultracraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public abstract class AbstractNbtResultRecipe implements CraftingRecipe
{
	
	protected final Identifier id;
	protected final String group;
	protected final CraftingRecipeCategory category;
	protected final ItemStack output;
	protected final DefaultedList<Ingredient> input;
	
	public AbstractNbtResultRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input)
	{
		this.id = id;
		this.group = group;
		this.category = category;
		this.output = output;
		this.input = input;
	}
	
	@Override
	public boolean matches(RecipeInputInventory inventory, World world)
	{
		RecipeMatcher recipeMatcher = new RecipeMatcher();
		int i = 0;
		for (int j = 0; j < inventory.size(); ++j) {
			ItemStack itemStack = inventory.getStack(j);
			if (itemStack.isEmpty()) continue;
			++i;
			recipeMatcher.addInput(itemStack, 1);
		}
		return i == this.input.size() && recipeMatcher.match(this, null);
	}
	
	@Override
	public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager)
	{
		return output.copy();
	}
	
	@Override
	public boolean fits(int width, int height)
	{
		return true;
	}
	
	@Override
	public ItemStack getOutput(DynamicRegistryManager registryManager)
	{
		return output;
	}
	
	@Override
	public Identifier getId()
	{
		return id;
	}
	
	@Override
	public DefaultedList<Ingredient> getIngredients()
	{
		return input;
	}
	
	@Override
	public CraftingRecipeCategory getCategory()
	{
		return category;
	}
}
