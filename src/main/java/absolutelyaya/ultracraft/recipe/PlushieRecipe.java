package absolutelyaya.ultracraft.recipe;

import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.RecipeSerializers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class PlushieRecipe implements CraftingRecipe
{
	final Identifier id;
	final String group;
	final CraftingRecipeCategory category;
	final ItemStack output;
	final DefaultedList<Ingredient> input;
	
	public PlushieRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input)
	{
		this.id = id;
		this.group = group;
		this.category = category;
		this.output = output;
		this.input = input;
	}
	
	@Override
	public boolean matches(CraftingInventory inventory, World world)
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
	public ItemStack craft(CraftingInventory inventory, DynamicRegistryManager registryManager)
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
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.PLUSHIE_SERIALIZER;
	}
	
	@Override
	public CraftingRecipeCategory getCategory()
	{
		return category;
	}
	
	public static class PlushieRecipeSerializer implements RecipeSerializer<PlushieRecipe>
	{
		@Override
		public PlushieRecipe read(Identifier id, JsonObject json)
		{
			String string = JsonHelper.getString(json, "group", "");
			CraftingRecipeCategory craftingRecipeCategory = CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC);
			DefaultedList<Ingredient> defaultedList = getIngredients(JsonHelper.getArray(json, "ingredients"));
			if (defaultedList.isEmpty())
				throw new JsonParseException("No ingredients for shapeless recipe");
			if (defaultedList.size() > 9)
				throw new JsonParseException("Too many ingredients for shapeless recipe");
			ItemStack itemStack = outputFromJson(JsonHelper.getObject(json, "result"));
			return new PlushieRecipe(id, string, craftingRecipeCategory, itemStack, defaultedList);
		}
		
		private DefaultedList<Ingredient> getIngredients(JsonArray json)
		{
			DefaultedList<Ingredient> defaultedList = DefaultedList.of();
			for (int i = 0; i < json.size(); ++i) {
				Ingredient ingredient = Ingredient.fromJson(json.get(i));
				if (ingredient.isEmpty()) continue;
				defaultedList.add(ingredient);
			}
			return defaultedList;
		}
		
		public static ItemStack outputFromJson(JsonObject json)
		{
			String type = JsonHelper.getString(json, "type");
			int i = JsonHelper.getInt(json, "count", 1);
			if (i < 1)
				throw new JsonSyntaxException("Invalid output count: " + i);
			return ItemRegistry.PLUSHIE.getDefaultStack(type);
		}
		
		@Override
		public PlushieRecipe read(Identifier id, PacketByteBuf buf)
		{
			String string = buf.readString();
			CraftingRecipeCategory craftingRecipeCategory = buf.readEnumConstant(CraftingRecipeCategory.class);
			int i = buf.readVarInt();
			DefaultedList<Ingredient> defaultedList = DefaultedList.ofSize(i, Ingredient.EMPTY);
			defaultedList.replaceAll(ignored -> Ingredient.fromPacket(buf));
			ItemStack itemStack = buf.readItemStack();
			return new PlushieRecipe(id, string, craftingRecipeCategory, itemStack, defaultedList);
		}
		
		@Override
		public void write(PacketByteBuf buf, PlushieRecipe recipe)
		{
			buf.writeString(recipe.group);
			buf.writeEnumConstant(recipe.category);
			buf.writeVarInt(recipe.input.size());
			for (Ingredient ingredient : recipe.input)
				ingredient.write(buf);
			buf.writeItemStack(recipe.output);
		}
	}
}
