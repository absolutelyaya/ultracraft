package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements WingedPlayerEntity
{
	boolean wingsActive;
	byte wingState, lastState;
	float wingAnimTime;
	
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	public void setWingState(byte state)
	{
		lastState = wingState;
		wingState = state;
		setWingAnimTime(0);
	}
	
	@Override
	public byte getWingState()
	{
		if(isSprinting() && !isOnGround())
		{
			if(wingState != 2)
				setWingState((byte)2);
		}
		else if (wingState == 2)
			setWingState((byte)1);
		return wingState;
	}
	
	@Override
	public byte getLastState()
	{
		return lastState;
	}
	
	@Override
	public float getWingAnimTime()
	{
		return wingAnimTime;
	}
	
	@Override
	public void setWingAnimTime(int i)
	{
		wingAnimTime = i;
	}
	
	@Override
	public void setWingsVisible(boolean b)
	{
		wingsActive = b;
		setWingState((byte)(b ? 1 : 0));
	}
	
	@Override
	public boolean isWingsVisible()
	{
		return wingsActive;
	}
	
	@Inject(method = "tickMovement", at = @At("HEAD"))
	void onTick(CallbackInfo ci)
	{
		if(getWingAnimTime() < 1f)
			wingAnimTime += MinecraftClient.getInstance().getTickDelta();
	}
}
