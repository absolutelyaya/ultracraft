package absolutelyaya.ultracraft.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpecialSpawnEggItem extends SpawnEggItem
{
	
	public SpecialSpawnEggItem(EntityType<? extends MobEntity> type, int primaryColor, int secondaryColor, Settings settings)
	{
		super(type, primaryColor, secondaryColor, settings);
	}
	
	public ItemStack getDefaultNamedStack(String name, String entityName)
	{
		ItemStack stack = super.getDefaultStack();
		stack.getOrCreateNbt().putString("realname", name);
		NbtCompound entity = stack.getOrCreateSubNbt("EntityTag");
		entity.putString("CustomName", Text.Serializer.toJson(Text.translatable(entityName)));
		return stack;
	}
	
	public ItemStack getDefaultBossStack(String name, boolean boss)
	{
		ItemStack stack = super.getDefaultStack();
		stack.getOrCreateNbt().putString("realname", name);
		NbtCompound entity = stack.getOrCreateSubNbt("EntityTag");
		entity.putBoolean("boss", boss);
		return stack;
	}
	
	public static ItemStack putLore(ItemStack stack, boolean hidden, String[] lines)
	{
		NbtList list = new NbtList();
		for (String s : lines)
			list.add(NbtString.of(s));
		stack.getOrCreateNbt().put(hidden ? "hiddenLore" : "lore", list);
		return stack;
	}
	
	public static ItemStack putLore(ItemStack stack, String[] lines, String[] hiddenLines)
	{
		putLore(stack, false, lines);
		putLore(stack, true, hiddenLines);
		return stack;
	}
	
	@Override
	public Text getName(ItemStack stack)
	{
		if(stack.getNbt() != null && stack.getNbt().contains("realname", NbtElement.STRING_TYPE))
			return Text.translatable(stack.getOrCreateNbt().getString("realname"));
		return super.getName(stack);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
	{
		super.appendTooltip(stack, world, tooltip, context);
		if(!stack.hasNbt())
			return;
		NbtCompound nbt = stack.getNbt();
		if(nbt.contains("lore", NbtElement.LIST_TYPE))
			nbt.getList("lore", NbtElement.STRING_TYPE).forEach(s -> tooltip.add(Text.translatable(s.asString())));
		if(nbt.contains("hiddenLore", NbtElement.LIST_TYPE) && context.isAdvanced())
			nbt.getList("hiddenLore", NbtElement.STRING_TYPE).forEach(s -> tooltip.add(Text.translatable(s.asString())));
	}
}
