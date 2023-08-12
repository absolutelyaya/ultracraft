package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CerberusBlockEntity extends BlockEntity implements GeoBlockEntity
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation IDLE_EMPTY_ANIM = RawAnimation.begin().thenLoop("idle_empty");
	private static final RawAnimation STAND_UP_ANIM = RawAnimation.begin().thenPlay("stand_up").thenLoop("idle_empty");
	int cooldown;
	
	public CerberusBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.CERBERUS, pos, state);
	}
	
	public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState state, T blockEntity)
	{
		if(state.get(CerberusBlock.SPAWNING) || state.get(CerberusBlock.EMPTY) || state.get(CerberusBlock.PROXIMITY) == 0)
			return;
		if(blockEntity instanceof CerberusBlockEntity cerb)
		{
			if(cerb.cooldown > 0)
				cerb.cooldown--;
			if(cerb.cooldown <= 0)
			{
				if(world.getEntitiesByType(TypeFilter.instanceOf(PlayerEntity.class), new Box(pos).expand(16),
						e -> !e.isCreative() && !e.isSpectator()).size() > 0)
				{
					world.setBlockState(pos, state.with(CerberusBlock.SPAWNING, true), Block.NOTIFY_LISTENERS);
					world.scheduleBlockTick(pos, BlockRegistry.CERBERUS, 60);
				}
				cerb.cooldown = 20;
			}
		}
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		controllers.add(new AnimationController<>(this, state -> {
			BlockState bstate = state.getAnimatable().getWorld().getBlockState(pos);
			if(!bstate.isOf(BlockRegistry.CERBERUS))
				return PlayState.STOP;
			boolean empty = bstate.get(CerberusBlock.EMPTY);
			boolean spawning = bstate.get(CerberusBlock.SPAWNING);
			if(!empty && spawning)
				return state.setAndContinue(STAND_UP_ANIM);
			if (empty)
				return state.setAndContinue(IDLE_EMPTY_ANIM);
			else
				return state.setAndContinue(IDLE_ANIM);
		}).setParticleKeyframeHandler(event -> {
			if(event.getKeyframeData().getEffect().equals("dust") && world != null)
			{
				world.playSound(null, pos, SoundEvents.BLOCK_DEEPSLATE_BREAK, SoundCategory.HOSTILE, 2f, 0.5f);
				for (int i = 0; i < 16; i++)
				{
					Vec3d ppos = new Vec3d(pos.getX() + world.random.nextDouble() * 1.25, pos.getY() + 1f + world.random.nextDouble() * 2,
							pos.getZ() + world.random.nextDouble() * 1.25);
					world.addParticle(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, Blocks.STONE.getDefaultState()),
							ppos.x, ppos.y, ppos.z, 0f, 0f, 0f);
				}
			}
		}));
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains("proximity", NbtElement.INT_TYPE))
			world.setBlockState(getPos(), getCachedState().with(CerberusBlock.PROXIMITY, nbt.getInt("proximity")));
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("proximity", getCachedState().get(CerberusBlock.PROXIMITY));
	}
}
