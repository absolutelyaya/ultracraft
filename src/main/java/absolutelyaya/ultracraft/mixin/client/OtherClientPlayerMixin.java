package absolutelyaya.ultracraft.mixin.client;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.ParticleRegistry;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OtherClientPlayerEntity.class)
public abstract class OtherClientPlayerMixin extends PlayerEntity implements WingedPlayerEntity
{
	boolean wasSprinting = false, wasDashing = false;
	Vec3d lastSlidePos = new Vec3d(0f, 0f, 0f), lastDashPos = new Vec3d(0, 0, 0);
	
	public OtherClientPlayerMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile)
	{
		super(world, pos, yaw, gameProfile);
	}
	
	@Inject(method = "tickMovement", at = @At("HEAD"))
	void onTickMovement(CallbackInfo ci)
	{
		World world = MinecraftClient.getInstance().world;
		MinecraftClient.getInstance().execute(() -> {
			if(UltraComponents.WINGED_ENTITY.get(this).getDashingTicks() >= -1)
			{
				if(!wasDashing)
				{
					lastDashPos = getPos();
					wasDashing = true;
					return;
				}
				Vec3d dir = getPos().subtract(lastDashPos);
				Vec3d particleVel = new Vec3d(-dir.x, 0, -dir.z).multiply(random.nextDouble() * 0.33 + 0.1);
				Vec3d pos = getPos().add((random.nextDouble() - 0.5) * getWidth(),
						random.nextDouble() * getHeight(), (random.nextDouble() - 0.5) * getWidth()).add(dir.multiply(0.25));
				world.addParticle(ParticleRegistry.DASH, true, pos.x, pos.y, pos.z, particleVel.x, particleVel.y, particleVel.z);
				lastDashPos = getPos();
			}
			if(isSprinting() && UltraComponents.WING_DATA.get(this).isVisible())
			{
				if(!wasSprinting)
				{
					lastSlidePos = getPos();
					wasSprinting = true;
					return;
				}
				Vec3d slideDir = getPos().subtract(lastSlidePos);
				Vec3d dir = slideDir.multiply(1.0, 0.0, 1.0).normalize();
				Vec3d particleVel = new Vec3d(-dir.x, -dir.y, -dir.z).multiply(random.nextDouble() * 0.1 + 0.025);
				Vec3d pos = getPos().add(dir.multiply(1.5));
				world.addParticle(ParticleRegistry.SLIDE, true, pos.x, pos.y + 0.1, pos.z, particleVel.x, particleVel.y, particleVel.z);
				lastSlidePos = getPos();
			}
			wasSprinting = isSprinting();
			wasDashing = UltraComponents.WINGED_ENTITY.get(this).getDashingTicks() >= -1;
		});
	}
}
