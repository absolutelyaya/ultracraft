package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
	
	public ItemStack getDefaultStack(String name, String entityName)
	{
		ItemStack stack = super.getDefaultStack();
		stack.getOrCreateNbt().putString("realname", name);
		NbtCompound entity = stack.getOrCreateSubNbt("EntityTag");
		entity.putString("CustomName", Text.Serializer.toJson(Text.translatable(entityName)));
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
		if (stack.getNbt() != null && stack.getNbt().getString("realname").equals("item.ultracraft.swordsmachine_spawn_egg.special") &&
					((SpawnEggItem)stack.getItem()).getEntityType(stack.getNbt()).equals(EntityRegistry.SWORDSMACHINE))
		{
			tooltip.add(Text.translatable("item.ultracraft.swordsmachine_spawn_egg.special.lore"));
			if (context.isAdvanced())
				tooltip.add(Text.translatable("item.ultracraft.swordsmachine_spawn_egg.special.hiddenlore"));
		}
	}
}
