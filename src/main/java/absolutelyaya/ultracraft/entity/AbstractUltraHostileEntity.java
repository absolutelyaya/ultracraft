package absolutelyaya.ultracraft.entity;

import absolutelyaya.ultracraft.particle.ParryIndicatorParticleEffect;
import absolutelyaya.ultracraft.particle.TeleportParticleEffect;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractUltraHostileEntity extends HostileEntity
{
	protected static final TrackedData<Byte> ANIMATION = DataTracker.registerData(AbstractUltraHostileEntity.class, TrackedDataHandlerRegistry.BYTE);
	protected static final TrackedData<Boolean> BOSS = DataTracker.registerData(AbstractUltraHostileEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	protected ServerBossBar bossBar;
	boolean wasBossbarVisible;
	
	protected AbstractUltraHostileEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	public byte getAnimation()
	{
		return dataTracker.get(ANIMATION);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(ANIMATION, (byte)0);
		dataTracker.startTracking(BOSS, getBossDefault());
	}
	
	@Nullable
	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt)
	{
		return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("boss", NbtElement.BYTE_TYPE))
			dataTracker.set(BOSS, nbt.getBoolean("boss"));
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putBoolean("boss", dataTracker.get(BOSS));
	}
	
	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet)
	{
		super.onSpawnPacket(packet);
		getWorld().addParticle(new TeleportParticleEffect(getTeleportParticleSize()), packet.getX(), packet.getY(), packet.getZ(), 0f, 0f, 0f);
		getWorld().playSound(getX(), getY(), getZ(), SoundRegistry.GENERIC_SPAWN, SoundCategory.HOSTILE, 0.75f, 1.3f + random.nextFloat() * 0.15f, false);
	}
	
	protected ServerBossBar initBossBar()
	{
		return new ServerBossBar(getDisplayName(), BossBar.Color.RED, ClassTinkerers.getEnum(BossBar.Style.class, "ULTRA"));
	}
	
	protected double getTeleportParticleSize()
	{
		return 1.0;
	}
	
	public void addParryIndicatorParticle(Vec3d offset, boolean useYaw, boolean unparriable)
	{
		if(useYaw)
			offset = offset.rotateY(-(float)Math.toRadians(getYaw() + 180));
		if(!getWorld().isClient)
		{
			((ServerWorld)getWorld()).spawnParticles(new ParryIndicatorParticleEffect(unparriable),
					getX() + offset.x, getY() + offset.y, getZ() + offset.z, 1, 0f, 0f, 0f, 0f);
		}
	}
	
	@Override
	protected boolean shouldSwimInFluids()
	{
		return !getWorld().getFluidState(getBlockPos()).isIn(FluidTags.WATER);
	}
	
	@Override
	public int getAir()
	{
		return getMaxAir();
	}
	
	@Override
	protected int computeFallDamage(float fallDistance, float damageMultiplier)
	{
		return super.computeFallDamage(fallDistance - 3, damageMultiplier);
	}
	
	@Override
	public void setCustomName(@Nullable Text name)
	{
		super.setCustomName(name);
		if(isBoss() && isBossBarVisible())
			bossBar.setName(getDisplayName());
	}
	
	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player)
	{
		super.onStartedTrackingBy(player);
		if(isBoss() && isBossBarVisible())
			bossBar.addPlayer(player);
	}
	
	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player)
	{
		super.onStoppedTrackingBy(player);
		if(isBoss() && isBossBarVisible())
			bossBar.removePlayer(player);
	}
	
	@Override
	protected void mobTick()
	{
		super.mobTick();
		if(isBoss() && isBossBarVisible())
			bossBar.setPercent(getHealth() / getMaxHealth());
		if(getWorld().isClient || !isBoss())
			return;
		if(wasBossbarVisible && !isBossBarVisible())
			bossBar.clearPlayers();
		else if(!wasBossbarVisible && isBossBarVisible())
		{
			getWorld().getPlayers(TargetPredicate.DEFAULT, this, getBoundingBox().expand(64))
					.forEach(p -> bossBar.addPlayer((ServerPlayerEntity)p));
		}
		wasBossbarVisible = isBossBarVisible();
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		boolean b = super.damage(source, amount);
		if(isBoss() && isBossBarVisible())
			bossBar.setPercent(getHealth() / getMaxHealth());
		return b;
	}
	
	protected boolean getBossDefault()
	{
		return false;
	}
	
	public boolean isBoss()
	{
		boolean boss = dataTracker.get(BOSS);
		if(boss && bossBar == null)
			bossBar = initBossBar();
		return boss;
	}
	
	public boolean isBossBarVisible()
	{
		return true;
	}
	
	protected EnemySoundType getSoundType()
	{
		return EnemySoundType.GENERIC;
	}
	
	@Override
	protected SoundEvent getHurtSound(DamageSource source)
	{
		return switch(getSoundType()) {
			case HUSK -> SoundRegistry.HUSK_DAMAGE;
			case MACHINE -> SoundRegistry.MACHINE_DAMAGE;
			case GENERIC -> SoundEvents.ENTITY_GENERIC_HURT;
		};
	}
	
	@Override
	protected SoundEvent getDeathSound()
	{
		return switch(getSoundType()) {
			case HUSK -> SoundRegistry.HUSK_DEATH;
			case MACHINE -> SoundRegistry.MACHINE_DEATH;
			case GENERIC -> SoundEvents.ENTITY_GENERIC_DEATH;
		};
	}
	
	protected void playFireSound()
	{
		playSound(SoundRegistry.GENERIC_FIRE, 1, 1);
	}
}
