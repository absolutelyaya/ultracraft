package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.registry.ParticleRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public abstract class AbstractOrbEntity extends Entity
{
	private static final TrackedData<Boolean> INVISIBLE = DataTracker.registerData(AbstractOrbEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public AbstractOrbEntity(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		dataTracker.startTracking(INVISIBLE, false);
	}
	
	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
	
	}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
	
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(!isInvisible())
		{
			if(age % 50 == 0)
				getWorld().addParticle(ParticleRegistry.BIG_CIRCLE, getX(), getY() + 0.5, getZ(), 0, 0, 0);
			if(age % 70 == 0)
				playSound(SoundEvents.BLOCK_BEACON_AMBIENT, 1f, 1.25f);
		}
		if(getPos().distanceTo(getBlockPos().toCenterPos()) > 0.05f)
		{
			Vec3d pos = getPos().lerp(getBlockPos().toCenterPos(), 0.02f);
			setPos(pos.x, pos.y, pos.z);
		}
	}
	
	@Override
	protected void pushOutOfBlocks(double x, double y, double z)
	{
	
	}
	
	@Override
	public void pushAwayFrom(Entity entity)
	{
	
	}
	
	@Override
	public boolean isPushedByFluids()
	{
		return false;
	}
	
	@Override
	public boolean canBeHitByProjectile()
	{
		return false;
	}
	
	@Override
	public void onPlayerCollision(PlayerEntity player)
	{
		super.onPlayerCollision(player);
		onRemoved();
		remove(RemovalReason.KILLED);
	}
	
	@Override
	public void onRemoved()
	{
		for (int i = 0; i < 16; i++)
		{
			Vec3d dir = new Vec3d(0, 0, 0).addRandom(random, 1f);
			getWorld().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), dir.x, dir.y, dir.z);
			getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, getParticleBlockstate()), getX(), getY(), getZ(), dir.x, dir.y, dir.z);
		}
		getWorld().playSound(null, getBlockPos(), SoundRegistry.BARRIER_BREAK, SoundCategory.PLAYERS, 0.9f, 1f);
		super.onRemoved();
	}
	
	protected BlockState getParticleBlockstate()
	{
		return Blocks.AIR.getDefaultState();
	}
	
	public abstract Identifier getTexture();
	
	public abstract Vec3i getGlowColor();
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains("Invisible", NbtElement.BYTE_TYPE))
			dataTracker.set(INVISIBLE, nbt.getBoolean("Invisible"));
	}
	
	@Override
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		if(dataTracker.get(INVISIBLE))
			nbt.putBoolean("Invisible", true);
		return super.writeNbt(nbt);
	}
	
	@Override
	public boolean isInvisible()
	{
		return super.isInvisible() || dataTracker.get(INVISIBLE);
	}
}
