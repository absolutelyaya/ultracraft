package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.registry.TagRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements WingedPlayerEntity
{
	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile)
	{
		super(world, profile);
	}
	
	@Shadow public abstract boolean isSneaking();
	
	@Shadow private double lastX;
	
	@Shadow private double lastZ;
	
	@Shadow public abstract float getPitch(float tickDelta);
	
	@Shadow private boolean lastSneaking;
	
	@Shadow @Final public ClientPlayNetworkHandler networkHandler;
	@Shadow private double lastBaseY;
	@Shadow private int ticksSinceLastPositionPacketSent;
	@Shadow private float lastYaw;
	@Shadow private boolean lastOnGround;
	@Shadow private boolean autoJumpEnabled;
	@Shadow @Final protected MinecraftClient client;
	
	@Shadow protected abstract void sendSprintingPacket();
	
	@Shadow private boolean lastSprinting;
	@Shadow public Input input;
	
	@Shadow public abstract boolean isSubmergedInWater();
	
	@Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);
	
	Vec3d dashDir = Vec3d.ZERO;
	Vec3d slideDir = Vec3d.ZERO;
	boolean slamming, lastSlamming, strongGroundPound, lastJumping, lastSprintPressed, lastTouchedWater, wasHiVel, slamStored, slideStartedSideways;
	int slamTicks, slamCooldown, slamJumpTimer = -1, slideTicks, wallJumps = 3, coyote, disableJumpTicks;
	float slideVelocity;
	final float baseJumpVel = 0.42f;
	
	void tryDash()
	{
		if(UltracraftClient.isHiVelEnabled() && !getAbilities().flying)
		{
			if(isSneaking() && !lastSneaking)
			{
				if(!consumeStamina())
					return;
				if(slamming)
					slamming = false;
				if(slamStored)
					slamStored = false;
				Vec3d dir = new Vec3d(input.movementSideways, 0f, input.movementForward).rotateY(-(float)Math.toRadians(getYaw())).normalize();
				if(dir.lengthSquared() < 0.9f)
					dir = Vec3d.fromPolar(0f, getYaw()).normalize();
				
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeDouble(dir.x);
				buf.writeDouble(dir.y);
				buf.writeDouble(dir.z);
				ClientPlayNetworking.send(PacketRegistry.DASH_C2S_PACKET_ID, buf);
				setVelocity(dir);
				dashDir = dir;
				onDash();
				playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.5f, 1.6f);
			}
		}
	}
	
	@Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
	public void onSendMovementPackets(CallbackInfo ci)
	{
		if(UltracraftClient.isHiVelEnabled() && !getAbilities().flying && !isSpectator())
		{
			if(slamCooldown > 0)
				slamCooldown--;
			if(wasHiVel != UltracraftClient.isHiVelEnabled() && lastSprintPressed)
				setSliding(true, false); //when switching into HiVelMode while sprinting, reinitiate sliding
			if(slamJumpTimer > -1 && slamJumpTimer < 4)
				slamJumpTimer++;
			//start slide or groundpound
			if(client.options.sprintKey.isPressed() && !lastSprintPressed && !slamming)
			{
				//start ground pound
				if(isUnSolid(posToBlock(getPos().subtract(0f, 0.99f, 0f))) && !verticalCollision && slamCooldown == 0)
				{
					cancelDash();
					slamTicks = 0;
					slamming = true;
					strongGroundPound = true;
					setSprinting(false);
				}
				//start slide
				else if(!horizontalCollision && !jumping && !isDashing() && !wasDashing(2))
				{
					BlockPos pos = posToBlock(getPos().add(Vec3d.fromPolar(0f, getYaw()).normalize()));
					setSliding((!isUnSolid(posToBlock(getPos().subtract(0f, 0.79f, 0f))) || verticalCollision) &&
										 !world.getBlockState(pos).isSolidBlock(world, pos), lastSprintPressed);
				}
				//cancel slide because it shouldn't be possible rn anyways
				else if(isSprinting())
					setSprinting(false);
				ci.cancel();
			}
			//cancel strong groundpound if key is let go during it
			if(strongGroundPound && !client.options.sprintKey.isPressed())
				strongGroundPound = false;
			//stop sliding once conditions aren't met anymore
			else if(isSprinting())
			{
				if(jumping && (!isUnSolid(posToBlock(getPos().subtract(0f, 0.1f, 0f))) || coyote > 0))
				{
					setVelocity(slideDir.multiply(1f + 0.05 * UltracraftClient.speed).multiply(slideVelocity * 1.5));
					addVelocity(0, baseJumpVel, 0);
					setIgnoreSlowdown(true); //don't slow down from air friction during movement tech
				}
				boolean moved = new Vec3d(lastX, lastBaseY, lastZ).distanceTo(getPos()) > slideVelocity / 2f || Ultracraft.isTimeFrozen() || slideTicks < 1;
				setSprinting(client.options.sprintKey.isPressed() && !slamming && moved && !jumping);
				slideTicks++;
				if(isUnSolid(posToBlock(getPos().subtract(0f, 0.25f, 0f))))
					slideTicks = 0;
				ci.cancel();
			}
			//slide velocity
			if(isSprinting())
			{
				if(isSprinting() != lastSprinting)
					sendSprintingPacket();
				setVelocity(slideDir.multiply(1f + 0.2 * UltracraftClient.speed).multiply(slideVelocity / 1.5f).add(0f, getVelocity().y, 0f));
				ci.cancel();
			}
			//skim on liquids
			BlockPos belowPos = posToBlock(getPos().subtract(0f, 0.1f, 0f));
			FluidState fluidBelow = world.getBlockState(belowPos).getFluidState();
			if(isSprinting() && !fluidBelow.getFluid().equals(Fluids.EMPTY) && !fluidBelow.isIn(TagRegistry.UNSKIMMABLE_FLUIDS)
					   && world.getFluidState(belowPos.up()).getFluid().equals(Fluids.EMPTY))
			{
				Vec3d vel = getVelocity();
				setVelocity(new Vec3d(vel.x, Math.max(baseJumpVel / 2f, vel.y * -0.75), vel.z));
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				buf.writeVector3f(getPos().toVector3f());
				ClientPlayNetworking.send(PacketRegistry.SKIM_C2S_PACKET_ID, buf);
			}
			//ground pound velocity
			if(slamming)
			{
				slamTicks++;
				if(!slamStored)
					setVelocity(0, -2, 0);
				//landing
				if(verticalCollision && getVelocity().y < 0f)
				{
					slamming = false;
					slamJumpTimer = 0;
					slamCooldown = 5;
				}
				if(jumping && lastJumping)
					disableJumpTicks = 8;
				ci.cancel();
			}
			//high jump after ground pound
			if(jumping && !lastJumping)
			{
				if(slamJumpTimer > -1 && slamJumpTimer < 4)
				{
					slamJumpTimer = -1;
					if(client.options.sprintKey.isPressed() && !strongGroundPound) //Dive / Ultradive
					{
						setIgnoreSlowdown(true);
						setVelocity(Vec3d.fromPolar(0, getYaw()).multiply(slamStored ? 4f : 1.5f).add(0, getJumpVelocity(), 0));
					}
					else
						setVelocity(0, slamTicks / 20f + getJumpVelocity() * 1.5f + (slamStored ? 3f : 0f), 0);
					slamStored = false;
					slamTicks = 0;
					ci.cancel();
				}
				slamStored = false;
			}
			//start dash
			if(isSneaking() && !lastSneaking)
			{
				tryDash();
				ci.cancel();
			}
			//dash velocity
			if(isDashing())
			{
				setVelocity(dashDir.multiply(1f + 0.2 * UltracraftClient.speed));
				ci.cancel();
			}
			//dash jump (preserves velocity)
			if(wasDashing() && input.jumping && !lastJumping && (!isUnSolid(posToBlock(getPos().subtract(0f, 0.49f, 0f))) || coyote > 0))
			{
				onDashJump();
				if(!consumeStamina())
					setVelocity(dashDir.multiply(0.3));
				addVelocity(0f, baseJumpVel, 0f);
				setIgnoreSlowdown(true); //don't slow down from air friction during movement tech
				playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.5f, 1.8f);
			}
			//stop dashing
			if(wasDashing() && !isDashing())
			{
				float slipandslide = getSteppingBlockState().getBlock().getSlipperiness();
				if(isUnSolid(posToBlock(getPos().subtract(0f, 0.1f, 0f))))
					setVelocity(dashDir.multiply(0.3));
				else if(slipandslide > 0.6)
					setVelocity(dashDir.multiply(Math.min(slipandslide - 0.5, 0.6)));
				else
					setVelocity(Vec3d.ZERO);
				ci.cancel();
			}
			//stop ignoring slowdown when not sliding/dashing and on ground
			if((verticalCollision && !isUnSolid(posToBlock(getPos().subtract(0f, 0.1f, 0f)))) &&
					   !isSprinting() && shouldIgnoreSlowdown() && !isDashing())
				setIgnoreSlowdown(false);
			//reset walljumps upon landing
			if(!lastOnGround && onGround)
			{
				if(!isUnSolid(posToBlock(getPos().subtract(0f, 0.1f, 0f))))
					coyote = 4;
				wallJumps = 3;
				if(slamming)
					slamming = false;
			}
			if((!onGround || isUnSolid(posToBlock(getPos().subtract(0f, 0.1f, 0f)))) && coyote > 0)
				coyote--;
			//wall sliding / fall slow-down
			Iterable<VoxelShape> temp = world.getBlockCollisions(this, getBoundingBox().expand(0.1f, 0, 0f));
			ArrayList<VoxelShape> touchingWalls = new ArrayList<>(StreamSupport.stream(temp.spliterator(), false).toList());
			temp = world.getBlockCollisions(this, getBoundingBox().expand(0f, 0, 0.1f));
			touchingWalls.addAll(StreamSupport.stream(temp.spliterator(), false).toList());
			if(!slamming && !isSprinting() && touchingWalls.size() > 0 && isUnSolid(posToBlock(getPos().subtract(0f, 0.2f, 0f))))
			{
				Vec3d vel = getVelocity();
				setVelocity(new Vec3d(vel.x, Math.max(vel.y, -0.2), vel.z));
				ci.cancel();
			}
			//wall jump
			if(wallJumps > 0 && isUnSolid(posToBlock(getPos().subtract(0f, 0.5f, 0f))) &&
					   jumping && !lastJumping && !lastOnGround && touchingWalls.size() > 0 && (UltracraftClient.isSlamStorageEnabled() || !slamming) &&
					   !isSprinting())
			{
				Vec3d vel = new Vec3d(0, 0, 0);
				Optional<Integer> X = Optional.empty();
				Optional<Integer> Z = Optional.empty();
				for (VoxelShape shape : touchingWalls)
				{
					Optional<Vec3d> opos = shape.getClosestPointTo(getPos());
					if(opos.isEmpty())
						continue;
					Vec3d v = unhorizontalize(getPos().multiply(1, 0, 1).subtract(opos.get().multiply(1, 0, 1)).normalize());
					if(X.isPresent() && v.z != 0 && Math.abs(X.get() - opos.get().x) < 1f)
						continue;
					if(Z.isPresent() && v.x != 0 && Math.abs(Z.get() - opos.get().z) < 1f)
						continue;
					if(v.x != 0)
						Z = Optional.of((int)opos.get().z);
					else
						X = Optional.of((int)opos.get().x);
					vel = vel.add(v);
				}
				vel = new Vec3d(MathHelper.clamp(vel.x, -1f, 1f), 0, MathHelper.clamp(vel.z, -1f, 1f));
				setVelocity(vel.normalize().multiply(0.33));
				addVelocity(0f, getJumpVelocity(), 0f);
				if(slamming && UltracraftClient.isSlamStorageEnabled())
					slamStored = true;
				if(!isCreative())
					wallJumps--;
				setIgnoreSlowdown(false);
				ci.cancel();
			}
			//update movement data
			if(ci.isCancelled())
			{
				networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(getX(), getY(), getZ(), getYaw(), getPitch(), onGround));
				lastX = getX();
				lastBaseY = getY();
				lastZ = getZ();
				ticksSinceLastPositionPacketSent = 0;
				lastYaw = getYaw();
				autoJumpEnabled = client.options.getAutoJump().getValue();
				lastTouchedWater = world.getBlockState(posToBlock(getPos().subtract(0f, 0.1, 0f))).getBlock() instanceof FluidBlock;
				if(lastSlamming != slamming)
				{
					boolean strong = strongGroundPound && !slamming && consumeStamina();
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeBoolean(slamming);
					buf.writeBoolean(strong);
					ClientPlayNetworking.send(PacketRegistry.GROUND_POUND_C2S_PACKET_ID, buf);
					if(slamming)
						startSlam();
					else
						endSlam(strong);
				}
				lastSlamming = slamming;
			}
			if(isSprinting() && slideVelocity > 0.33f && slideTicks > 50)
				slideVelocity = Math.max(0.33f, slideVelocity * 0.995f);
			lastSprintPressed = client.options.sprintKey.isPressed() && !isDashing() && !wasDashing(2);
			lastJumping = jumping;
			lastOnGround = onGround;
			if(lastSneaking != isSneaking())
				networkHandler.sendPacket(new ClientCommandC2SPacket(this,
						isSneaking() ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
			lastSneaking = isSneaking();
		}
		else
		{
			if (shouldIgnoreSlowdown())
				setIgnoreSlowdown(false);
			if((!wasHiVel && UltracraftClient.isHiVelEnabled() || (getAbilities().flying || isSpectator())) && isSprinting())
				setSliding(false, true);
			if(slamming)
				cancelGroundPound();
		}
		wasHiVel = UltracraftClient.isHiVelEnabled();
	}
	
	@Inject(method = "tickNewAi", at = @At("RETURN"))
	void onTickAI(CallbackInfo ci)
	{
		jumping = jumping && disableJumpTicks == 0;
		if(disableJumpTicks > 0)
			disableJumpTicks--;
	}
	
	Vec3d unhorizontalize(Vec3d in)
	{
		return Math.abs(in.x) > Math.abs(in.z) ? new Vec3d(in.x > 0f ? 1f : -1f, 0f, 0f) : new Vec3d(0f, 0f, in.z > 0f ? 1f : -1f);
	}
	
	void cancelGroundPound()
	{
		slamming = lastSlamming = false;
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBoolean(slamming);
		buf.writeBoolean(false);
		ClientPlayNetworking.send(PacketRegistry.GROUND_POUND_C2S_PACKET_ID, buf);
		endSlam(false);
	}
	
	void setSliding(boolean sliding, boolean last)
	{
		this.setFlag(3, sliding); //sprinting flag
		if(sliding && !last)
		{
			Vec2f movementDir = input.getMovementInput();
			slideStartedSideways = movementDir.x != 0f;
			if(movementDir.lengthSquared() == 0)
				slideDir = Vec3d.fromPolar(0f, getYaw()).normalize();
			else
				slideDir = new Vec3d(movementDir.x, 0, movementDir.y).rotateY((float)Math.toRadians(-getRotationClient().y)).normalize();
		}
		slideVelocity = Math.max(0.33f, (float)getVelocity().multiply(1.2f, 0f, 1.2f).length());
		slideTicks = 0;
	}
	
	boolean isUnSolid(BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		return !state.hasSolidTopSurface(world, pos, this);
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z"))
	boolean onTickMovement(Input instance)
	{
		if(UltracraftClient.isHiVelEnabled())
			return true;
		else
			return input.hasForwardMovement();
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V"))
	void onTickMovement(ClientPlayerEntity instance, boolean sprinting)
	{
		//cancel normal sprint triggers when in HiVelMode
		if(!UltracraftClient.isHiVelEnabled())
			setSprinting(sprinting);
	}
	
	@Inject(method = "tick", at = @At(value = "HEAD"))
	void onTick(CallbackInfo ci)
	{
		if(getWingAnimTime() < 1f)
			setWingAnimTime(getWingAnimTime() + MinecraftClient.getInstance().getTickDelta());
	}
	
	@Override
	public void setSprinting(boolean sprinting)
	{
		if(UltracraftClient.isHiVelEnabled() && isSprinting() != sprinting)
			setSliding(sprinting, lastSprinting);
		super.setSprinting(sprinting);
	}
	
	@Inject(method = "canSprint", at = @At(value = "HEAD"), cancellable = true)
	void onCanSprint(CallbackInfoReturnable<Boolean> cir)
	{
		WingedPlayerEntity winged = this;
		if((UltracraftClient.isHiVelEnabled() && !isSprinting() && !onGround) || winged.isDashing())
			cir.setReturnValue(false);
	}
	
	@Override
	public boolean isWingsActive()
	{
		return UltracraftClient.isHiVelEnabled();
	}
	
	@Override
	public Vec3d getSlideDir()
	{
		return slideDir;
	}
	
	BlockPos posToBlock(Vec3d vec)
	{
		return new BlockPos(new Vec3i((int)Math.floor(vec.x), (int)Math.floor(vec.y), (int)Math.floor(vec.z)));
	}
}
