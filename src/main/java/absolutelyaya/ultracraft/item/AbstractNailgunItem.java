package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.entity.projectile.NailEntity;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
//TODO: fix third person Transform
public abstract class AbstractNailgunItem extends AbstractWeaponItem implements GeoItem
{
	final RawAnimation AnimationFireLoop = RawAnimation.begin().thenPlay("fire_loop");
	final RawAnimation AnimationFireStop = RawAnimation.begin().thenPlay("fire_stop");
	
	public AbstractNailgunItem(Settings settings)
	{
		super(settings, 0.5f, 15f);
	}
	
	@Override
	public boolean onPrimaryFire(World world, PlayerEntity user, Vec3d userVelocity)
	{
		ItemStack stack = user.getMainHandStack();
		if(isCanFirePrimary(user) && getNbt(user.getMainHandStack(), "nails") > 0)
		{
			if(world.isClient)
			{
				super.onPrimaryFire(world, user, userVelocity);
				return true;
			}
			NailEntity nail = new NailEntity(EntityRegistry.NAIL, world);
			nail.setPosition(user.getEyePos().subtract(0, 0.25, 0).add(user.getRotationVector().rotateY((float)Math.toRadians(90))
																			   .multiply(user.getMainArm().equals(Arm.RIGHT) ? -0.3 : 0.3)));
			nail.setOwner(user);
			nail.setVelocity(user, user.getPitch(), user.getYaw(), 0f, 1f, 10f);
			world.spawnEntity(nail);
			setNbt(stack, "nails", getNbt(stack, "nails") - 1);
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void onPrimaryFireStart(World world, PlayerEntity user)
	{
		super.onPrimaryFireStart(world, user);
		if(!world.isClient)
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), "fire_loop");
	}
	
	@Override
	public void onPrimaryFireStop(World world, PlayerEntity user)
	{
		super.onPrimaryFireStop(world, user);
		if(!world.isClient)
			triggerAnim(user, GeoItem.getOrAssignId(user.getMainHandStack(), (ServerWorld)world), getControllerName(), "fire_stop");
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
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		super.inventoryTick(stack, world, entity, slot, selected);
		int nails = getNbt(stack, "nails");
		if(nails < 100 && entity.age % 5 == 0 &&
				   (!selected || (entity instanceof PlayerEntity player && !UltraComponents.WINGED_ENTITY.get(player).isPrimaryFiring()) || nails == 0))
			setNbt(stack, "nails", nails + 1);
	}
	
	@Override
	public String getCountString(ItemStack stack)
	{
		return Formatting.GOLD + String.valueOf(getNbt(stack, "nails"));
	}
	
	@Override
	Item[] getVariants()
	{
		return new Item[] { ItemRegistry.ATTRACTOR_NAILGUN };
	}
	
	@Override
	int getSwitchCooldown()
	{
		return 10;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, getControllerName(), 1, state -> PlayState.STOP)
										.triggerableAnim("fire_loop", AnimationFireLoop)
										.triggerableAnim("fire_stop", AnimationFireStop));
	}
	
	@Override
	public int getNbtDefault(String nbt)
	{
		if(nbt.equals("nails"))
			return 100;
		return 0;
	}
}
