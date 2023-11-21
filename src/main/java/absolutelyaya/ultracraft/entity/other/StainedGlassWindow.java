package absolutelyaya.ultracraft.entity.other;

import absolutelyaya.ultracraft.entity.projectile.IIgnoreSharpshooter;
import absolutelyaya.ultracraft.item.StainedGlassWindowItem;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StainedGlassWindow extends AbstractDecorationEntity implements IIgnoreSharpshooter
{
	protected static final TrackedData<Boolean> REINFORCED = DataTracker.registerData(StainedGlassWindow.class, TrackedDataHandlerRegistry.BOOLEAN);
	
	public StainedGlassWindow(EntityType<? extends AbstractDecorationEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	private StainedGlassWindow(World world, BlockPos pos)
	{
		super(EntityRegistry.STAINED_GLASS_WINDOW, world, pos);
	}
	
	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		dataTracker.startTracking(REINFORCED, false);
	}
	
	public static StainedGlassWindow place(World world, BlockPos pos, Direction facing)
	{
		StainedGlassWindow window = new StainedGlassWindow(world, pos);
		window.setFacing(facing);
		if(!window.canStayAttached())
			return null;
		return window;
	}
	
	@Override
	public int getWidthPixels()
	{
		return 32;
	}
	
	@Override
	public int getHeightPixels()
	{
		return 48;
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		if(dataTracker.get(REINFORCED) && !source.isOf(DamageTypes.PLAYER_ATTACK) || (source.getAttacker() instanceof PlayerEntity player && !player.canModifyBlocks()))
			return false;
		return super.damage(source, amount);
	}
	
	@Override
	public void onBreak(@Nullable Entity entity)
	{
		playSound(SoundRegistry.STAINED_GLASS_WINDOW_BREAK, 1.0f, 1.0f);
		if(entity instanceof PlayerEntity player && player.isCreative())
			return;
		dropStack(StainedGlassWindowItem.getStack(dataTracker.get(REINFORCED)));
	}
	
	@Override
	public void onPlace()
	{
		playSound(SoundRegistry.STAINED_GLASS_WINDOW_PLACE, 1.0f, 1.0f);
	}
	
	@Nullable
	@Override
	public ItemStack getPickBlockStack()
	{
		return ItemRegistry.STAINED_GLASS_WINDOW.getDefaultStack();
	}
	
	@Override
	public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch)
	{
		setPosition(x, y, z);
	}
	
	@Override
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate)
	{
		setPosition(x, y, z);
	}
	
	@Override
	public Vec3d getSyncedPos()
	{
		return Vec3d.of(attachmentPos);
	}
	
	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket()
	{
		return new EntitySpawnS2CPacket(this, facing.getId(), getDecorationBlockPos());
	}
	
	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet)
	{
		super.onSpawnPacket(packet);
		setFacing(Direction.byId(packet.getEntityData()));
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		dataTracker.set(REINFORCED, nbt.getBoolean("reinforced"));
		setFacing(Direction.fromHorizontal(nbt.getByte("facing")));
	}
	
	@Override
	public NbtCompound writeNbt(NbtCompound nbt)
	{
		nbt.putBoolean("reinforced", dataTracker.get(REINFORCED));
		nbt.putByte("facing", (byte)facing.getHorizontal());
		return super.writeNbt(nbt);
	}
	
	public void setReinforced(boolean b)
	{
		dataTracker.set(REINFORCED, b);
	}
	
	public boolean isReinforced()
	{
		return dataTracker.get(REINFORCED);
	}
}
