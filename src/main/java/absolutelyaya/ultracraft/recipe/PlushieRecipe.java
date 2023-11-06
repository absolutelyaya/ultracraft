package absolutelyaya.ultracraft.recipe;

import absolutelyaya.ultracraft.item.PlushieItem;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;

public class PlushieRecipe extends AbstractNbtResultRecipe
{
	
	public PlushieRecipe(Identifier id, String group, CraftingRecipeCategory category, ItemStack output, DefaultedList<Ingredient> input)
	{
		super(id, group, category, output, input);
	}
	
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.PLUSHIE_SERIALIZER;
	}
	
	public static class Serializer extends AbstractNbtRecipeSerializer<PlushieRecipe>
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
		
		public ItemStack outputFromJson(JsonObject json)
		{
			String type = JsonHelper.getString(json, "type");
			int i = JsonHelper.getInt(json, "count", 1);
			if (i < 1)
				throw new JsonSyntaxException("Invalid output count: " + i);
			Item item = JsonHelper.getItem(json, "item", ItemRegistry.PLUSHIE);
			if(item instanceof PlushieItem plushie)
				return plushie.getDefaultStack(type);
			else
				throw new JsonSyntaxException("Not a PlushieItem: " + item);
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
