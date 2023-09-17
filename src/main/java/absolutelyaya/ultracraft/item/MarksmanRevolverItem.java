package absolutelyaya.ultracraft.item;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.GunCooldownManager;
import absolutelyaya.ultracraft.client.rendering.item.MarksmanRevolverRenderer;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
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
	public ItemStack getDefaultStack()
	{
		ItemStack stack = new ItemStack(this);
		setCoins(stack, 4);
		return stack;
	}
	
	public ItemStack getStackedMarksman()
	{
		ItemStack stack = getDefaultStack();
		setCoins(stack, 64);
		return stack;
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
	{
		ItemStack itemStack = user.getStackInHand(hand);
		if(hand.equals(Hand.OFF_HAND))
			return TypedActionResult.fail(itemStack);
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(user).getGunCooldownManager();
		user.setCurrentHand(hand);
		int coins = getCoins(itemStack);
		if(coins <= 0)
			return TypedActionResult.pass(itemStack);
		((LivingEntityAccessor)user).punch();
		if(!world.isClient && coins > 0)
		{
			if(coins == 4)
				cdm.setCooldown(this, 200, GunCooldownManager.SECONDARY);
			setCoins(itemStack, coins - 1);
		}
		if(world.isClient && coins > 0)
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeVector3f(user.getEyePos().add(user.getRotationVector()).toVector3f());
			buf.writeVector3f(user.getVelocity().toVector3f());
			buf.writeBoolean(((WingedPlayerEntity)user).hasJustJumped());
			ClientPlayNetworking.send(PacketRegistry.THROW_COIN_PACKET_ID, buf);
		}
		return TypedActionResult.pass(itemStack);
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected)
	{
		if(!(entity instanceof PlayerEntity player))
			return;
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(player).getGunCooldownManager();
		super.inventoryTick(stack, world, entity, slot, selected);
		int coins = getCoins(stack);
		if(coins < 4 && cdm.isUsable(this, GunCooldownManager.SECONDARY))
		{
			setCoins(stack, coins + 1);
			if(coins + 1 < 4)
				cdm.setCooldown(this, 200, GunCooldownManager.SECONDARY);
			player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.1f, 1.75f);
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
										.triggerableAnim("shot", AnimationShot)
										.triggerableAnim("shot2", AnimationShot2)); //this animation purely exists to cancel shot animations.
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
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		return !cdm.isUsable(stack.getItem(), GunCooldownManager.PRIMARY) || getCoins(stack) < 4;
	}
	
	@Override
	public int getItemBarStep(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		if(!cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return (int)(cdm.getCooldownPercent(stack.getItem(), GunCooldownManager.PRIMARY) * 14);
		else
			return (int)((1f - cdm.getCooldownPercent(stack.getItem(), GunCooldownManager.SECONDARY)) * 14);
	}
	
	@Override
	public int getItemBarColor(ItemStack stack)
	{
		GunCooldownManager cdm = UltraComponents.WINGED_ENTITY.get(MinecraftClient.getInstance().player).getGunCooldownManager();
		if(cdm.isUsable(this, GunCooldownManager.PRIMARY))
			return 0xdfb728;
		return 0x28df53;
	}
	
	@Override
	public String getCountString(ItemStack stack)
	{
		return Formatting.GOLD + String.valueOf(getCoins(stack));
	}
	
	public int getCoins(ItemStack stack)
	{
		if(!stack.hasNbt() || !stack.getNbt().contains("coins", NbtElement.INT_TYPE))
			stack.getOrCreateNbt().putInt("coins", 4);
		return stack.getNbt().getInt("coins");
	}
	
	public void setCoins(ItemStack stack, int i)
	{
		stack.getOrCreateNbt().putInt("coins", i);
	}
}
