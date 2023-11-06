package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.entity.other.AbstractOrbEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class OrbItem extends Item
{
	final EntityType<? extends AbstractOrbEntity> type;
	
	public OrbItem(Settings settings, EntityType<? extends AbstractOrbEntity> type)
	{
		super(settings);
		this.type = type;
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context)
	{
		World world = context.getWorld();
		if (!(world instanceof ServerWorld serverWorld))
			return ActionResult.SUCCESS;
		if(!context.getPlayer().isCreative())
			context.getStack().decrement(1);
		world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, context.getBlockPos().up(2));
		type.spawn(serverWorld, context.getBlockPos().up(2), SpawnReason.SPAWN_EGG);
		return ActionResult.CONSUME;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		return TypedActionResult.fail(user.getStackInHand(hand));
	}
}
