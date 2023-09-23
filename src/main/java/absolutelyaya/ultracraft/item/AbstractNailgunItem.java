package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.entity.projectile.NailEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;

public abstract class AbstractNailgunItem extends AbstractWeaponItem implements GeoItem
{
	public AbstractNailgunItem(Settings settings)
	{
		super(settings, 0.5f, 15f);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		if(isCanFirePrimary(user))
		{
			if(world.isClient)
			{
				super.onPrimaryFire(world, user, userVelocity);
				return true;
			}
			NailEntity nail = new NailEntity(EntityRegistry.NAIL, world);
			nail.setPosition(user.getEyePos().subtract(0, 0.25, 0));
			nail.setOwner(user);
			nail.setVelocity(user, user.getPitch(), user.getYaw(), 0f, 1f, 10f);
			world.spawnEntity(nail);
			return true;
		}
		else
			return false;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND))
			return TypedActionResult.fail(itemStack);
		onAltFire(world, user);
		return super.use(world, user, hand);
	}
	
	@Override
	Item[] getVariants()
	{
		return new Item[0] /*{ItemRegistry.ATTRACTOR_NAILGUN }*/;
	}
	
	@Override
	int getSwitchCooldown()
	{
		return 10;
	}
}
