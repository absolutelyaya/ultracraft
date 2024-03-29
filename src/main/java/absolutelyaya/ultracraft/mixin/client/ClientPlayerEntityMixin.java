package absolutelyaya.ultracraft.mixin.client;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import absolutelyaya.ultracraft.client.UltracraftClient;
import absolutelyaya.ultracraft.client.gui.screen.TerminalScreen;
import absolutelyaya.ultracraft.client.gui.screen.WingCustomizationScreen;
import absolutelyaya.ultracraft.compat.PlayerAnimator;
import absolutelyaya.ultracraft.components.player.IWingDataComponent;
import absolutelyaya.ultracraft.components.player.IWingedPlayerComponent;
import absolutelyaya.ultracraft.item.AbstractWeaponItem;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import absolutelyaya.ultracraft.registry.StatusEffectRegistry;
import absolutelyaya.ultracraft.registry.TagRegistry;
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
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
	
	@Shadow public abstract boolean damage(DamageSource source, float amount);
	
	@Shadow public abstract boolean isUsingItem();
	
	@Shadow private @Nullable Hand activeHand;
	
	@Shadow public abstract boolean isMainPlayer();
	
	Vec3d dashDir = Vec3d.ZERO;
	Vec3d slideDir = Vec3d.ZERO;
	boolean slamming, lastSlamming, strongGroundPound, lastJumping, lastSprintPressed, lastTouchedWater, wasHiVel, slamStored,
			slideStartedSideways;
	int slamTicks, slamCooldown, slamJumpTimer = -1, slideTicks, wallJumps = 3, coyote, disableJumpTicks, jumpTicks, slidePreservationTicks;
	float slideVelocity = 0.33f;
	final float baseJumpVel = 0.42f;
	TerminalBlockEntity focusedTerminal;
	
	void tryDash()
	{
		IWingDataComponent wings = UltraComponents.WING_DATA.get(this);
		if(wings.isActive() && !getAbilities().flying && wouldPoseNotCollide(EntityPose.STANDING))
		{
			if(isSneaking() && !lastSneaking)
			{
				IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(this);
				if(!winged.consumeStamina())
					return;
				if(slamming)
					slamming = false;
				if(slamStored)
					slamStored = false;
				Vec3d dir = new Vec3d(input.movementSideways, 0f, input.movementForward).rotateY(-(float)Math.toRadians(getYaw())).normalize();
				if(dir.lengthSquared() < 0.9f)
					dir = Vec3d.fromPolar(0f, getYaw()).normalize();
				
				if(isMainPlayer())
				{
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeDouble(dir.x);
					buf.writeDouble(dir.y);
					buf.writeDouble(dir.z);
					ClientPlayNetworking.send(PacketRegistry.DASH_C2S_PACKET_ID, buf);
					PlayerAnimator.playAnimation(client.player, forwardSpeed >= 0 ? PlayerAnimator.DASH_FORWARD : PlayerAnimator.DASH_BACK, 5, false);
				}
				setVelocity(dir);
				dashDir = dir;
				winged.onDash();
				if(isSprinting())
					setSliding(false, true);
			}
		}
	}
	
	@Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
	public void onSendMovementPackets(CallbackInfo ci)
	{
		if(getFocusedTerminal() != null)
		{
			if(!lastSneaking && isSneaking())
				setFocusedTerminal(null); //exit focused Terminal
		}
		
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(this);
		IWingDataComponent wings = UltraComponents.WING_DATA.get(this);
		if(wings.isActive() && !getAbilities().flying && !isSpectator())
		{
			boolean grounded = isGrounded(0.1f);
			if(slamCooldown > 0)
			{
				slamCooldown--;
				if(slamCooldown == 0 && !isSprinting())
					slideVelocity = 0.33f;
			}
			if(wasHiVel != wings.isActive() && lastSprintPressed)
				setSliding(true, false); //when switching into HiVelMode while sprinting, reinitiate sliding
			if(slamJumpTimer > -1 && slamJumpTimer < 4)
				slamJumpTimer++;
			//start slide or groundpound
			if(client.options.sprintKey.isPressed() && !lastSprintPressed && !slamming)
			{
				//start ground pound
				if(isUnSolid(posToBlock(getPos().subtract(0f, 0.99f, 0f))) && !verticalCollision && slamCooldown == 0)
				{
					winged.cancelDash();
					slamTicks = 0;
					slamming = true;
					strongGroundPound = true;
					setSprinting(false);
					if(isMainPlayer())
						PlayerAnimator.playAnimation(client.player, PlayerAnimator.SLAM_LOOP, 5, false);
				}
				//start slide
				else if(!horizontalCollision && !jumping && !winged.isDashing() && !winged.wasDashing(2))
				{
					BlockPos pos = posToBlock(getPos().add(Vec3d.fromPolar(0f, getYaw()).normalize()));
					setSliding((isGrounded(0.79f) || verticalCollision) &&
										 !getWorld().getBlockState(pos).isSolidBlock(getWorld(), pos), lastSprintPressed);
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
				if(jumping && (grounded || coyote > 0))
				{
					setVelocity(slideDir.multiply(1f + 0.05 * UltracraftClient.speed).multiply(slideVelocity * 1.25));
					addVelocity(0, baseJumpVel, 0);
					winged.setIgnoreSlowdown(true); //don't slow down from air friction during movement tech
				}
				boolean moved = new Vec3d(lastX, lastBaseY, lastZ).distanceTo(getPos()) > slideVelocity / 2f || Ultracraft.isTimeFrozen() || slideTicks < 1;
				setSprinting(client.options.sprintKey.isPressed() && !slamming && moved && !jumping);
				slideTicks++;
				if(!grounded)
					slideTicks = 0;
				ci.cancel();
			}
			//slide velocity
			if(isSprinting())
			{
				if(isSprinting() != lastSprinting)
					sendSprintingPacket();
				setVelocity(slideDir.multiply(1f + 0.2 * UltracraftClient.speed).multiply(slideVelocity / 1.2f).add(0f, getVelocity().y, 0f));
				ci.cancel();
			}
			//skim on liquids
			BlockPos belowPos = posToBlock(getPos().subtract(0f, 0.1f, 0f));
			FluidState fluidBelow = getWorld().getBlockState(belowPos).getFluidState();
			if(isSprinting() && !fluidBelow.getFluid().equals(Fluids.EMPTY) && !fluidBelow.isIn(TagRegistry.UNSKIMMABLE_FLUIDS)
					   && getWorld().getFluidState(belowPos.up()).getFluid().equals(Fluids.EMPTY))
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
					slideVelocity = slamStored ? 1f : 0.66f;
					if(isMainPlayer())
						PlayerAnimator.playAnimation(client.player, PlayerAnimator.SLAM_IMPACT, 0, false);
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
						winged.setIgnoreSlowdown(true);
						setVelocity(Vec3d.fromPolar(0, getYaw()).multiply(slamStored ? 4f : 1.5f).add(0, getJumpVelocity(), 0));
						if(isMainPlayer())
							PlayerAnimator.playAnimation(client.player,
									slamStored ? PlayerAnimator.SLAMSTORE_DIVE : PlayerAnimator.SLAM_DIVE, 0, false);
					}
					else
					{
						setVelocity(0, slamTicks / 20f + getJumpVelocity() * 1.5f + (slamStored ? 3f : 0f), 0);
						if(isMainPlayer())
							PlayerAnimator.playAnimation(client.player, PlayerAnimator.SLAM_JUMP, 0, false);
					}
					if(slamStored)
						jumpTicks = 0;
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
			if(winged.isDashing())
			{
				float f = hasStatusEffect(StatusEffectRegistry.IMPALED) ? 0.05f : 1f;
				setVelocity(dashDir.multiply(f + 0.2 * UltracraftClient.speed));
				ci.cancel();
			}
			//dash jump (preserves velocity)
			if(winged.wasDashing() && input.jumping && !lastJumping && (isGrounded(0.49f) || coyote > 0))
			{
				winged.onDashJump();
				if(!winged.consumeStamina())
					setVelocity(dashDir.multiply(0.3));
				addVelocity(0f, baseJumpVel, 0f);
				winged.setIgnoreSlowdown(true); //don't slow down from air friction during movement tech
				if(isMainPlayer())
					PlayerAnimator.playAnimation(client.player, forwardSpeed >= 0 ? PlayerAnimator.DASH_FORWARD : PlayerAnimator.DASH_BACK, 5, false);
			}
			//stop dashing
			if(winged.wasDashing() && !winged.isDashing())
			{
				float slipandslide = getSteppingBlockState().getBlock().getSlipperiness();
				if(!grounded)
					setVelocity(dashDir.multiply(hasStatusEffect(StatusEffectRegistry.IMPALED) ? 0.03 : 0.3));
				else if(slipandslide > 0.6)
					setVelocity(dashDir.multiply(Math.min(slipandslide - 0.5, 0.6)));
				else
					setVelocity(Vec3d.ZERO);
				ci.cancel();
			}
			//stop ignoring slowdown when not sliding/dashing and on ground
			if((verticalCollision && grounded) &&
					   !isSprinting() && winged.shouldIgnoreSlowdown() && !winged.isDashing())
				winged.setIgnoreSlowdown(false);
			//reset walljumps upon landing
			if(!lastOnGround && isOnGround())
			{
				if(grounded)
					coyote = 4;
				wallJumps = 3;
				winged.setAirControlIncreased(false);
				if(slamming)
					slamming = false;
				slidePreservationTicks = 5;
			}
			if((!isOnGround() || !grounded) && coyote > 0)
				coyote--;
			//wall sliding / fall slow-down
			Iterable<VoxelShape> temp = getWorld().getBlockCollisions(this, getBoundingBox().expand(0.1f, 0, 0f));
			ArrayList<VoxelShape> touchingWalls = new ArrayList<>(StreamSupport.stream(temp.spliterator(), false).toList());
			temp = getWorld().getBlockCollisions(this, getBoundingBox().expand(0f, 0, 0.1f));
			touchingWalls.addAll(StreamSupport.stream(temp.spliterator(), false).toList());
			if(!slamming && !isSprinting() && touchingWalls.size() > 0 && !grounded)
			{
				Vec3d vel = getVelocity();
				setVelocity(new Vec3d(vel.x, Math.max(vel.y, -0.2), vel.z));
				ci.cancel();
			}
			//wall jump
			if(wallJumps > 0 && !isGrounded(0.5f) &&
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
				winged.setIgnoreSlowdown(false);
				winged.setAirControlIncreased(true);
				ci.cancel();
			}
			//update movement data
			if(ci.isCancelled())
			{
				networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(getX(), getY(), getZ(), getYaw(), getPitch(), isOnGround()));
				lastX = getX();
				lastBaseY = getY();
				lastZ = getZ();
				ticksSinceLastPositionPacketSent = 0;
				lastYaw = getYaw();
				autoJumpEnabled = client.options.getAutoJump().getValue();
				lastTouchedWater = getWorld().getBlockState(posToBlock(getPos().subtract(0f, 0.1, 0f))).getBlock() instanceof FluidBlock;
				if(lastSlamming != slamming)
				{
					boolean strong = strongGroundPound && !slamming && winged.consumeStamina();
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
			lastSprintPressed = client.options.sprintKey.isPressed() && !winged.isDashing() && !winged.wasDashing(2);
			lastJumping = jumping;
			lastOnGround = isOnGround();
			if(lastSneaking != isSneaking())
				networkHandler.sendPacket(new ClientCommandC2SPacket(this,
						isSneaking() ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
			lastSneaking = isSneaking();
		}
		else
		{
			if (winged.shouldIgnoreSlowdown())
				winged.setIgnoreSlowdown(false);
			if((!wasHiVel || getAbilities().flying || isSpectator()) && wings.isActive() && isSprinting())
				setSliding(false, true);
			if(slamming)
				cancelGroundPound();
		}
		wasHiVel = wings.isActive();
	}
	
	@Inject(method = "tickNewAi", at = @At("RETURN"))
	void onTickAI(CallbackInfo ci)
	{
		jumping = jumping && disableJumpTicks == 0;
		if(disableJumpTicks > 0)
			disableJumpTicks--;
		if(jumpTicks > 0)
			jumpTicks--;
		if(slidePreservationTicks > 0)
		{
			slidePreservationTicks--;
			if(slidePreservationTicks == 0)
				slideVelocity = 0.33f;
		}
	}
	
	@Inject(method = "damage", at = @At("RETURN"))
	void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(WingCustomizationScreen.MenuOpen)
			WingCustomizationScreen.Instance.close();
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
			if(isMainPlayer())
				PlayerAnimator.playAnimation(client.player, PlayerAnimator.START_SLIDE, 0, false);
		}
		else if(!sliding && last && isMainPlayer())
			PlayerAnimator.playAnimation(client.player, PlayerAnimator.STOP_SLIDE, 0, false);
		if(!UltraComponents.WINGED_ENTITY.get(this).wasDashing())
			slideVelocity = Math.max(0.33f, Math.max((float)getVelocity().multiply(1f, 0f, 1f).length(), last ? 0f : slideVelocity * 0.75f));
		else
			slideVelocity = 0.33f;
		slideTicks = 0;
		slidePreservationTicks = -1;
	}
	
	boolean isUnSolid(BlockPos pos)
	{
		BlockState state = getWorld().getBlockState(pos);
		return !state.hasSolidTopSurface(getWorld(), pos, this);
	}
	
	public boolean isGrounded(float distance)
	{
		if(isBlockHit(groundCheck(getPos(), distance)))
			return true;
		Box box = getBoundingBox();
		float y = (float)box.getMin(Direction.Axis.Y);
		if(isBlockHit(groundCheck(new Vec3d(box.getMin(Direction.Axis.X), y, box.getMin(Direction.Axis.Z)), distance)))
			return true;
		if(isBlockHit(groundCheck(new Vec3d(box.getMax(Direction.Axis.X) - 0.05, y, box.getMin(Direction.Axis.Z)), distance)))
			return true;
		if(isBlockHit(groundCheck(new Vec3d(box.getMin(Direction.Axis.X), y, box.getMax(Direction.Axis.Z) - 0.05), distance)))
			return true;
		return isBlockHit(groundCheck(new Vec3d(box.getMax(Direction.Axis.X) - 0.05, y, box.getMax(Direction.Axis.Z) - 0.05), distance));
	}
	
	BlockHitResult groundCheck(Vec3d start, float distance)
	{
		return getWorld().raycast(new RaycastContext(start, start.subtract(0, distance, 0),
				RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
	}
	
	boolean isBlockHit(BlockHitResult hit)
	{
		return hit != null && hit.getType().equals(HitResult.Type.BLOCK);
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z"))
	boolean onTickMovement(Input instance)
	{
		IWingDataComponent wings = UltraComponents.WING_DATA.get(this);
		if(wings.isActive())
			return true;
		else
			return input.hasForwardMovement();
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V"))
	void onTickMovement(ClientPlayerEntity instance, boolean sprinting)
	{
		IWingDataComponent wings = UltraComponents.WING_DATA.get(this);
		//cancel normal sprint triggers when in HiVelMode
		if(!wings.isActive())
			setSprinting(sprinting);
	}
	
	@Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	boolean redirectIsUsingItem(ClientPlayerEntity instance)
	{
		if(activeHand != null && getStackInHand(activeHand).getItem() instanceof AbstractWeaponItem)
			return false;
		return isUsingItem();
	}
	
	@Override
	public void setSprinting(boolean sprinting)
	{
		IWingDataComponent wings = UltraComponents.WING_DATA.get(this);
		if(wings.isActive() && isSprinting() != sprinting)
			setSliding(sprinting, lastSprinting);
		super.setSprinting(sprinting);
	}
	
	@Inject(method = "canSprint", at = @At(value = "HEAD"), cancellable = true)
	void onCanSprint(CallbackInfoReturnable<Boolean> cir)
	{
		IWingDataComponent wings = UltraComponents.WING_DATA.get(this);
		IWingedPlayerComponent winged = UltraComponents.WINGED_ENTITY.get(this);
		if((wings.isActive() && !isSprinting() && !isOnGround()) || winged.isDashing())
			cir.setReturnValue(false);
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
	
	@Override
	public void jump()
	{
		jumpTicks = 20;
		super.jump();
	}
	
	@Override
	public boolean hasJustJumped()
	{
		return jumpTicks > 0;
	}
	
	@Override
	public TerminalBlockEntity getFocusedTerminal()
	{
		return focusedTerminal;
	}
	
	@Override
	public void setFocusedTerminal(TerminalBlockEntity terminal)
	{
		MinecraftClient client = MinecraftClient.getInstance();
		if(terminal != null)
		{
			if(this.focusedTerminal != terminal)
				sendMessage(Text.translatable("screen.ultracraft.terminal.unfocus"), true);
			if(getWorld().isClient)
			{
				MinecraftClient.getInstance().gameRenderer.setRenderHand(false);
				client.setScreen(new TerminalScreen(terminal));
			}
		}
		else
		{
			this.focusedTerminal.unFocus(this);
			client.gameRenderer.setRenderHand(true);
			if(client.currentScreen instanceof TerminalScreen)
				client.setScreen(null);
		}
		this.focusedTerminal = terminal;
	}
}
