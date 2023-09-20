package absolutelyaya.ultracraft.recipe;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.components.IProgressionComponent;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Map;

public class UltraRecipe
{
	final Identifier id;
	final Map<Item, Integer> material;
	final Identifier result;
	final List<ItemStack> extraOutput;
	final List<Identifier> unlocks;
	
	public UltraRecipe(Identifier id, Map<Item, Integer> material, Identifier result, List<ItemStack> extraOutput, List<Identifier> unlocks)
	{
		this.id = id;
		this.material = material;
		this.result = result;
		this.extraOutput = extraOutput;
		this.unlocks = unlocks;
	}
	
	public int canCraft(PlayerEntity player)
	{
		if(UltraComponents.PROGRESSION.get(player).isOwned(result))
		{
			player.sendMessage(Text.of("[Error#1] Result is already owned."));
			return 1; //result is already owned
		}
		if(player.isCreativeLevelTwoOp())
			return 2;
		int result = 2;
		for(Item item : material.keySet())
		{
			int count = 0;
			for (ItemStack stack : player.getInventory().main)
			{
				if(stack.isOf(item))
				{
					count += stack.getCount();
					if(stack.hasCustomName() || stack.hasEnchantments())
						result = 3; //can craft, but some items that could get consumed might not be intended to be used for it
					if(count >= material.get(item))
						break; //item count is sufficient, lets stop counting
				}
			}
			if(count < material.get(item))
			{
				player.sendMessage(Text.of("[Error#0]Insufficient Materials"));
				return 0; //an item is insufficient; cannot craft
			}
		}
		return result;
	}
	
	public void craft(PlayerEntity player)
	{
		if(canCraft(player) <= 1)
			return;
		if(player.getWorld().isClient)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeIdentifier(id);
			ClientPlayNetworking.send(PacketRegistry.TERMINAL_WEAPON_CRAFT_PACKET_ID, buf);
			return;
		}
		//grant owned progression Entry
		IProgressionComponent progression = UltraComponents.PROGRESSION.get(player);
		progression.obtain(result);
		for (Identifier unlockID : unlocks)
		{
			progression.unlock(unlockID);
			System.out.println(unlockID);
		}
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
	
	public static UltraRecipe deserialize(Identifier id, JsonObject json)
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
		JsonArray unlocksIn = json.getAsJsonArray("unlocks");
		ImmutableList.Builder<Identifier> unlocksBuilder = ImmutableList.builder();
		if(unlocksIn != null)
			unlocksIn.forEach(i -> unlocksBuilder.add(Identifier.tryParse(i.getAsString())));
		return new UltraRecipe(id, materialsBuilder.build(), result, extraOutputsBuilder.build(), unlocksBuilder.build());
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
		NbtList unlocksIn = nbt.getList("unlocks", NbtElement.STRING_TYPE);
		ImmutableList.Builder<Identifier> unlocksBuilder = ImmutableList.builder();
		if(unlocksIn != null)
			unlocksIn.forEach(i -> unlocksBuilder.add(Identifier.tryParse(i.asString())));
		return new Pair<>(id, new UltraRecipe(id, materialsBuilder.build(), result, extraOutputsBuilder.build(), unlocksBuilder.build()));
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
		NbtList unlocks = new NbtList();
		for (Identifier entry : recipe.unlocks)
			unlocks.add(NbtString.of(entry.toString()));
		nbt.put("unlocks", unlocks);
		buf.writeNbt(nbt);
	}
	
	public Map<Item, Integer> getMaterials()
	{
		return material;
	}
}
