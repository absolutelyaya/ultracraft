package absolutelyaya.ultracraft.recipe;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.components.IProgressionComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Map;

public class UltraRecipe
{
	final Map<Item, Integer> material;
	final Identifier result;
	final List<ItemStack> extraOutput;
	
	public UltraRecipe(Map<Item, Integer> material, Identifier result, List<ItemStack> extraOutput)
	{
		this.material = material;
		this.result = result;
		this.extraOutput = extraOutput;
	}
	
	public int canCraft(PlayerEntity player)
	{
		if(player.isCreativeLevelTwoOp())
			return 1;
		int result = 1;
		for(Item item : material.keySet())
		{
			int count = 0;
			for (ItemStack stack : player.getInventory().main)
			{
				if(stack.isOf(item))
				{
					count += stack.getCount();
					if(stack.hasCustomName() || stack.hasEnchantments())
						result = 2; //can craft, but some items that could get consumed might not be intended to be used for it
					if(count >= material.get(item))
						break; //item count is sufficient, lets stop counting
				}
			}
			if(count < material.get(item))
				return 0; //an item is insufficient; cannot craft
		}
		return result;
	}
	
	public void craft(PlayerEntity player) //TODO: move to C2S packet and only run crafting logic and checks on Server
	{
		if(canCraft(player) == 0)
			return;
		//grant owned progression Entry
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(player);
		progression.obtain(result);
		progression.sync();
		//dispense extra output
		for (ItemStack stack : extraOutput)
			player.giveItemStack(stack);
		if(player.isCreativeLevelTwoOp())
			return;
		//consume items
		for(Item item : material.keySet())
		{
			int count = material.get(item);
			for (ItemStack stack : player.getInventory().main)
			{
				if(!stack.isOf(item))
					continue;
				int decrement = Math.min(count, stack.getCount());
				stack.decrement(decrement);
				count -= decrement;
			}
		}
	}
	
	public static UltraRecipe deserialize(JsonObject json)
	{
		JsonArray materialsIn = json.getAsJsonArray("materials");
		ImmutableMap.Builder<Item, Integer> materialsBuilder = ImmutableMap.builder();
		materialsIn.forEach(i -> {
			JsonObject object = i.getAsJsonObject();
			if(object.has("item") && object.has("count"))
				materialsBuilder.put(JsonHelper.getItem(object, "item"), JsonHelper.getInt(object, "count"));
		});
		Identifier result = Identifier.tryParse(json.get("result").getAsString());
		JsonArray extraOutputsIn = json.getAsJsonArray("extra-outputs");
		ImmutableList.Builder<ItemStack> extraOutputsBuilder = ImmutableList.builder();
		if(extraOutputsIn != null)
		{
			extraOutputsIn.forEach(i -> {
				JsonObject object = i.getAsJsonObject();
				if(object.has("item") && object.has("count"))
					extraOutputsBuilder.add(new ItemStack(JsonHelper.getItem(object, "item"), JsonHelper.getInt(object, "count")));
			});
		}
		return new UltraRecipe(materialsBuilder.build(), result, extraOutputsBuilder.build());
	}
	
	public JsonObject serialize()
	{
		JsonObject json = new JsonObject();
		JsonArray materials = new JsonArray();
		for (Map.Entry<Item, Integer> entry : this.material.entrySet())
		{
			JsonObject object = new JsonObject();
			object.add("item", new JsonPrimitive(Registries.ITEM.getId(entry.getKey()).toString()));
			object.add("count", new JsonPrimitive(entry.getValue()));
		}
		json.add("materials", materials);
		json.add("result", new JsonPrimitive(result.toString()));
		JsonArray extraOutputs = new JsonArray();
		for (ItemStack entry : this.extraOutput)
		{
			JsonObject object = new JsonObject();
			object.add("item", new JsonPrimitive(Registries.ITEM.getId(entry.getItem()).toString()));
			object.add("count", new JsonPrimitive(entry.getCount()));
		}
		json.add("extra-outputs", extraOutputs);
		return json;
	}
	
	public static Pair<Identifier, UltraRecipe> deserialize(PacketByteBuf buf)
	{
		Identifier id = buf.readIdentifier();
		NbtCompound nbt = buf.readNbt();
		NbtList materialsIn = nbt.getList("materials", NbtElement.COMPOUND_TYPE);
		ImmutableMap.Builder<Item, Integer> materialsBuilder = ImmutableMap.builder();
		materialsIn.forEach(i -> {
			NbtCompound object = (NbtCompound)i;
			if(object.contains("item") && object.contains("count"))
				materialsBuilder.put(Registries.ITEM.get(Identifier.tryParse(object.getString("item"))), object.getInt("count"));
		});
		Identifier result = Identifier.tryParse(nbt.getString("result"));
		NbtList extraOutputsIn = nbt.getList("extra-outputs", NbtElement.COMPOUND_TYPE);
		ImmutableList.Builder<ItemStack> extraOutputsBuilder = ImmutableList.builder();
		extraOutputsIn.forEach(i -> {
			NbtCompound object = (NbtCompound)i;
			if(object.contains("item"))
				extraOutputsBuilder.add(ItemStack.fromNbt(object.getCompound("item")));
		});
		return new Pair<>(id, new UltraRecipe(materialsBuilder.build(), result, extraOutputsBuilder.build()));
	}
	
	public static void serialize(PacketByteBuf buf, Pair<Identifier, UltraRecipe> pair)
	{
		buf.writeIdentifier(pair.getLeft());
		UltraRecipe recipe = pair.getRight();
		NbtCompound nbt = new NbtCompound();
		NbtList materials = new NbtList();
		for (Map.Entry<Item, Integer> entry : recipe.material.entrySet())
		{
			NbtCompound mat = new NbtCompound();
			mat.putString("item", Registries.ITEM.getId(entry.getKey()).toString());
			mat.putInt("count", entry.getValue());
			materials.add(mat);
		}
		nbt.put("materials", materials);
		nbt.putString("result", recipe.result.toString());
		NbtList extraOutputs = new NbtList();
		for (ItemStack entry : recipe.extraOutput)
		{
			NbtCompound stack = new NbtCompound();
			entry.writeNbt(stack);
			extraOutputs.add(stack);
		}
		nbt.put("extra-outputs", extraOutputs);
		buf.writeNbt(nbt);
	}
}
