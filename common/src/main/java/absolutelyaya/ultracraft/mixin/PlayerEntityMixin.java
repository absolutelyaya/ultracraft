package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
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
	int dashingTicks = -2;
	
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
		if(isDashing())
		{
			if(wingState != 2)
				setWingState((byte)0);
		}
		else if (wingState == 0 && isOnGround())
			setWingState((byte)1);
		return wingState;
	}
	
	@Override
	public void onDash()
	{
		dashingTicks = 3;
	}
	
	@Override
	public void onDashJump()
	{
		dashingTicks = -2;
	}
	
	public boolean isDashing()
	{
		return dashingTicks > 0;
	}
	
	@Override
	public boolean wasDashing()
	{
		return dashingTicks + 1 == 0;
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
		if(dashingTicks >= -1)
		{
			dashingTicks--;
			Vec3d dir = getVelocity();
			Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(random.nextDouble() * 0.33 + 0.1);
			Vec3d pos = getPos().add((random.nextDouble() - 0.5) * getWidth(),
					random.nextDouble() * getHeight(), (random.nextDouble() - 0.5) * getWidth()).add(dir.multiply(0.25));
			world.addParticle(ParticleRegistry.DASH.get(), pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
		}
	}
}
