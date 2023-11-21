package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;

public class HellSpawnerBlockEntity extends BlockEntity implements GeoBlockEntity
{
	private final String CONTROLLER_NAME = "anim_controller";
	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation SPAWN_ANIM = RawAnimation.begin().thenPlay("spawn").thenLoop("idle");
	
	ItemStack spawnStack;
	
	public HellSpawnerBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.HELL_SPAWNER, pos, state);
	}
	
	public void setSpawnStack(ItemStack stack)
	{
		this.spawnStack = stack;
		if(world != null)
			world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
	}
	
	public void spawn(ServerWorld world, BlockPos pos, BlockState state)
	{
		BlockPos spawnPos = getFirstFreeSpot(world, pos, state.get(HellSpawnerBlock.FACING));
		if(spawnPos == null)
		{
			if(!world.getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT))
				return;
			world.getPlayers().forEach(p -> {
				if(p.isCreativeLevelTwoOp())
					p.sendMessage(Text.translatable("message.ultracraft.hell_spawner.fail-spawn.blocked", pos));
			});
			return;
		}
		if(spawnStack != null && spawnStack.getItem() instanceof SpawnEggItem eggItem)
		{
			eggItem.getEntityType(spawnStack.getNbt()).spawnFromItemStack(world, spawnStack, null,
					spawnPos, SpawnReason.DISPENSER, true, false);
		}
		triggerAnim(CONTROLLER_NAME, "spawn");
	}
	
	public BlockPos getFirstFreeSpot(World world, BlockPos pos, Direction direction)
	{
		int range = 16;
		for (int i = 1; i <= range; i++)
		{
			BlockPos pos1 = pos.offset(direction, i);
			if(world.isAir(pos1) && world.isAir(pos1.up()))
				return pos1;
		}
		return null;
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		controllers.add(new AnimationController<>(this, CONTROLLER_NAME, state -> {
			BlockState bstate = state.getAnimatable().getWorld().getBlockState(pos);
			if(!bstate.isOf(BlockRegistry.HELL_SPAWNER))
				return PlayState.STOP;
			if(hasSpawnStack() && bstate.get(HellSpawnerBlock.TRIGGERED))
				state.setAnimation(SPAWN_ANIM);
			else
				state.setAnimation(IDLE_ANIM);
			return PlayState.CONTINUE;
		}));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	public boolean hasSpawnStack()
	{
		return spawnStack != null;
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		if(hasSpawnStack())
			nbt.put("SpawnStack", spawnStack.writeNbt(new NbtCompound()));
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains("SpawnStack", NbtElement.COMPOUND_TYPE))
			spawnStack = ItemStack.fromNbt(nbt.getCompound("SpawnStack"));
	}
	
	public ItemStack getSpawnStack()
	{
		return spawnStack;
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
}
