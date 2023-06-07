package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.MarksmanRevolverRenderer;
import absolutelyaya.ultracraft.entity.projectile.ThrownCoinEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2i;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MarksmanRevolverItem extends AbstractRevolverItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	
	public MarksmanRevolverItem(Settings settings)
	{
		super(settings, 15f, 0f);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity) MinecraftClient.getInstance().player).getGunCooldownManager();
		ItemStack itemStack = user.getStackInHand(hand);
		user.setCurrentHand(hand);
		if(!itemStack.hasNbt())
		{
			itemStack.getOrCreateNbt();
			itemStack.getNbt().putInt("coins", 4);
		}
		int coins = itemStack.getNbt().getInt("coins");
		if(!world.isClient && coins > 0)
		{
			if(coins == 4)
				cdm.setCooldown(this, 200, GunCooldownManager.SECONDARY);
			itemStack.getNbt().putInt("coins", coins - 1);
			ThrownCoinEntity coin = ThrownCoinEntity.spawn(user, world);
			Vec3d pos = user.getEyePos().add(user.getRotationVector());
			coin.setPos(pos.x, pos.y, pos.z);
			coin.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 0.5f, 0f);
			coin.addVelocity(0f, 0.5f, 0f);
			coin.addVelocity(user.getVelocity());
			world.spawnEntity(coin);
		}
		return TypedActionResult.pass(itemStack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		PlayerEntity player = MinecraftClient.getInstance().player;
		if(player == null)
			return;
		GunCooldownManager cdm = ((WingedPlayerEntity)player).getGunCooldownManager();
		super.inventoryTick(stack, world, entity, slot, selected);
		if(!stack.hasNbt())
		{
			stack.getOrCreateNbt();
			stack.getNbt().putInt("coins", 4);
		}
		if(stack.hasNbt() && stack.getNbt().contains("coins"))
		{
			int coins = stack.getNbt().getInt("coins");
			if(coins < 4 && cdm.isUsable(this, GunCooldownManager.SECONDARY))
			{
				stack.getNbt().putInt("coins", coins + 1);
				cdm.setCooldown(this, 200, GunCooldownManager.SECONDARY);
				player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.1f, 1.75f);
			}
		}
	}
	
	@Override
	public Vector2i getHUDTexture()
	{
		return new Vector2i(1, 0);
	}
	
	@Override
	String getControllerName()
	{
		return "marksmanRevolverController";
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.NONE;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, getControllerName(), 1, state -> PlayState.STOP)
										.triggerableAnim("charging", AnimationCharge)
										.triggerableAnim("discharge", AnimationDischarge)
										.triggerableAnim("shot", AnimationShot)
										.triggerableAnim("shot2", AnimationShot2) //this animation purely exists to cancel shot animations.
										.triggerableAnim("stop", AnimationStop));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void createRenderer(Consumer<Object> consumer)
	{
		consumer.accept(new RenderProvider() {
			private MarksmanRevolverRenderer renderer;
			
			@Override
			public BuiltinModelItemRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new MarksmanRevolverRenderer();
				
				return renderer;
			}
		});
	}
	
	@Override
	public Supplier<Object> getRenderProvider()
	{
		return renderProvider;
	}
	
	@Override
	public boolean isItemBarVisible(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity) MinecraftClient.getInstance().player).getGunCooldownManager();
		return !cdm.isUsable(stack.getItem(), GunCooldownManager.PRIMARY) || (stack.hasNbt() && stack.getNbt().getInt("coins") < 4);
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return (int)(cdm.getCooldownPercent(stack.getItem(), GunCooldownManager.PRIMARY) * 14);
		else
			return (int)((1f - cdm.getCooldownPercent(stack.getItem(), GunCooldownManager.SECONDARY)) * 14);
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		GunCooldownManager cdm = ((WingedPlayerEntity)MinecraftClient.getInstance().player).getGunCooldownManager();
		if(cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return 0xdfb728;
		return 0x28df53;
	}
}
