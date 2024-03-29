package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;

public class HellObserverBlockEntity extends BlockEntity
{
	HellOperator playerOperator = HellOperator.IGNORE, enemyOperator = HellOperator.IGNORE;
	int tick, playerCount, enemyCount;
	boolean lastCheck, halfClosed, requireBoth, previewArea = true;
	Vec3i checkDimensions = new Vec3i(3, 3, 3), checkOffset = new Vec3i(0, 0, 0);
	
	public HellObserverBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.HELL_OBSERVER, pos, state);
	}
	
	public Vec3i getCheckDimensions()
	{
		return checkDimensions;
	}
	
	public void setCheckDimensions(Vec3i v)
	{
		checkDimensions = v;
	}
	
	public Vec3i getCheckOffset()
	{
		return checkOffset;
	}
	
	public void setCheckOffset(Vec3i v)
	{
		checkOffset = v;
	}
	
	public int getRedstoneStrength()
	{
		return lastCheck ? 15 : 0;
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, HellObserverBlockEntity observer)
	{
		observer.tick++;
		if(observer.tick % world.getGameRules().getInt(GameruleRegistry.HELL_OBSERVER_INTERVAL) != 0)
			return;
		Vec3i size = observer.checkDimensions;
		Vec3i offset = observer.getCheckOffset();
		offset = new Vec3i((int)(offset.getX() - Math.floor(size.getX() / 2f)), (int)(offset.getY() - Math.floor(size.getY() / 2f)), (int)(offset.getZ() - Math.floor(size.getZ() / 2f)));
		Box box = new Box(pos).stretch(size.getX() - 1, size.getY() - 1, size.getZ() - 1).offset(offset.getX(), offset.getY(), offset.getZ());
		List<PlayerEntity> players = world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), box, p -> !p.isSpectator());
		List<HostileEntity> enemies = world.getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), box, e -> true);
		
		boolean success = check(observer, players.size(), enemies.size());
		if(observer.halfClosed)
		{
			world.setBlockState(pos, state.with(HellObserverBlock.ACTIVE, success ? 2 : 0));
			observer.halfClosed = false;
		}
		if(observer.lastCheck != success)
		{
			world.setBlockState(pos, state.with(HellObserverBlock.ACTIVE, 1));
			observer.halfClosed = true;
		}
		observer.lastCheck = success;
	}
	
	static boolean check(HellObserverBlockEntity observer, int players, int enemies)
	{
		boolean pCheck = observer.playerOperator.check(players, observer.playerCount);
		boolean eCheck = observer.enemyOperator.check(enemies, observer.enemyCount);
		if(observer.requireBoth && !observer.playerOperator.equals(HellOperator.IGNORE) && !observer.enemyOperator.equals(HellOperator.IGNORE))
			return pCheck && eCheck;
		else
			return pCheck || eCheck;
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		NbtCompound check = new NbtCompound();
		check.putIntArray("offset", new int[] {checkOffset.getX(), checkOffset.getY(), checkOffset.getZ()});
		check.putIntArray("size", new int[] {checkDimensions.getX(), checkDimensions.getY(), checkDimensions.getZ()});
		nbt.put("check", check);
		nbt.putInt("playerCount", playerCount);
		nbt.putInt("enemyCount", enemyCount);
		nbt.putByte("playerOperator", (byte)playerOperator.ordinal());
		nbt.putByte("enemyOperator", (byte)enemyOperator.ordinal());
		nbt.putBoolean("requireBoth", requireBoth);
		nbt.putBoolean("previewArea", previewArea);
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains("check", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound check = nbt.getCompound("check");
			if(check.contains("offset", NbtElement.INT_ARRAY_TYPE))
				checkOffset = intArrayToVector(check.getIntArray("offset"));
			if(check.contains("size", NbtElement.INT_ARRAY_TYPE))
				checkDimensions = intArrayToVector(check.getIntArray("size"));
		}
		if(nbt.contains("playerCount", NbtElement.INT_TYPE))
			playerCount = nbt.getInt("playerCount");
		if(nbt.contains("enemyCount", NbtElement.INT_TYPE))
			enemyCount = nbt.getInt("enemyCount");
		if(nbt.contains("playerOperator", NbtElement.BYTE_TYPE))
			playerOperator = HellOperator.values()[nbt.getByte("playerOperator")];
		if(nbt.contains("enemyOperator", NbtElement.BYTE_TYPE))
			enemyOperator = HellOperator.values()[nbt.getByte("enemyOperator")];
		if(nbt.contains("requireBoth", NbtElement.BYTE_TYPE))
			requireBoth = nbt.getBoolean("requireBoth");
		if(nbt.contains("previewArea", NbtElement.BYTE_TYPE))
			previewArea = nbt.getBoolean("previewArea");
	}
	
	Vec3i intArrayToVector(int[] array)
	{
		if(array == null || array.length < 3)
			return new Vec3i(3, 3, 3);
		return new Vec3i(array[0], array[1], array[2]);
	}
	
	public void sync(int playerCount, int playerOperator, int enemyCount, int enemyOperator, boolean requireBoth, Vec3i offset, Vec3i size, boolean previewArea)
	{
		this.playerCount = playerCount;
		this.playerOperator = HellOperator.values()[playerOperator];
		this.enemyCount = enemyCount;
		this.enemyOperator = HellOperator.values()[enemyOperator];
		this.requireBoth = requireBoth;
		this.checkOffset = offset;
		this.checkDimensions = size;
		this.previewArea = previewArea;
		markDirty();
		world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
	}
	
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket()
	{
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	@Override
	public NbtCompound toInitialChunkDataNbt()
	{
		return createNbt();
	}
	
	public int getPlayerCount()
	{
		return playerCount;
	}
	
	public HellOperator getPlayerOperator()
	{
		return playerOperator;
	}
	
	public int getEnemyCount()
	{
		return enemyCount;
	}
	
	public HellOperator getEnemyOperator()
	{
		return enemyOperator;
	}
	
	public boolean getRequireBoth()
	{
		return requireBoth;
	}
	
	public Vec3d getCamOffset(float rot)
	{
		float f = checkDimensions.getX() + checkDimensions.getY() + checkDimensions.getZ();
		return new Vec3d(checkOffset.getX(), checkOffset.getY(), checkOffset.getZ()).add(new Vec3d(0f, f / 4f, f / 5f)
					   .rotateY((getCachedState().get(HellObserverBlock.FACING).asRotation() + rot) * -MathHelper.RADIANS_PER_DEGREE));
	}
	
	public boolean shouldPreviewArea()
	{
		return previewArea;
	}
}
