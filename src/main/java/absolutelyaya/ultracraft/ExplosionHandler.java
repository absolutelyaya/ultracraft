package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import absolutelyaya.ultracraft.registry.TagRegistry;
import absolutelyaya.ultracraft.registry.GameruleRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.Random;

public class ExplosionHandler
{
	static final Random random = new java.util.Random();
	
	public static void explosion(Entity ignored, World world, Vec3d pos, DamageSource source, float damage, float falloff, float radius, boolean breakBlocks)
	{
		if(world.isClient && damage > 0f)
			explosionClient((ClientWorld)world, pos, radius);
		else
		{
			explosionServer(ignored, (ServerWorld)world, pos, source, damage, falloff, radius, breakBlocks);
			if(damage <= 0f)
				return;
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeDouble(pos.x);
			buf.writeDouble(pos.y);
			buf.writeDouble(pos.z);
			buf.writeDouble(radius);
			for (ServerPlayerEntity p : ((ServerWorld)world).getPlayers())
				ServerPlayNetworking.send(p, PacketRegistry.EXPLOSION_PACKET_ID, buf);
		}
	}
	
	public static void explosionClient(ClientWorld world, Vec3d pos, float radius)
	{
		world.playSound(pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, Math.max(radius * 0.75f, 1f),
				(float)(1 + (random.nextFloat() - 0.5f) * 0.1), true);
		if(radius <= 2)
		{
			world.addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 0f, 0f, 0f);
			return;
		}
		for (int i = 0; i < Math.ceil(radius / 5f); i++)
		{
			world.addParticle(ParticleTypes.EXPLOSION_EMITTER,
					pos.x + (random.nextFloat() - 0.5) * 2 * (radius / 5),
					pos.y + (random.nextFloat() - 0.5) * 2 * (radius / 5),
					pos.z + (random.nextFloat() - 0.5) * 2 * (radius / 5),
					0f, 0f, 0f);
		}
	}
	
	private static void explosionServer(Entity ignored, ServerWorld world, Vec3d pos, DamageSource source, float damage, float falloff, float radius, boolean breakBlocks)
	{
		Box box = new Box(pos.subtract(radius, radius, radius), pos.add(radius, radius, radius));
		if(damage > 0f)
		{
			world.getOtherEntities(ignored, box, Entity::isLiving).forEach(e -> {
				float normalizedDistance = (float)e.getPos().distanceTo(pos) / radius;
				if((e instanceof LivingEntityAccessor living && living.takePunchKnockback()) || e instanceof ProjectileEntity)
					e.addVelocity(e.getPos().subtract(pos).add(0.0, 1f - normalizedDistance, 0.0).normalize()
										  .multiply(Math.min(radius * 0.75, 1.75f) * (normalizedDistance == 0f ? 0.75f : Math.min(1.5f - normalizedDistance, 1f))));
				boolean unUltra = !(e instanceof AbstractUltraHostileEntity);
				e.damage(source, MathHelper.lerp(normalizedDistance, damage * (unUltra ? 1.5f : 1f), Math.max(damage - falloff, 0f) * (unUltra ? 1.5f : 1f)));
				if(!(e instanceof PlayerEntity))
					e.setOnFireFor(10);
			});
		}
		Entity exploder = source.getSource();
		GameRules rules = world.getGameRules();
		if(breakBlocks && rules.getBoolean(GameruleRegistry.EXPLOSION_DAMAGE) && (exploder instanceof PlayerEntity || rules.getBoolean(GameRules.DO_MOB_GRIEFING)))
		{
			BlockPos center = new BlockPos((int)Math.floor(pos.x), (int)Math.floor(pos.y), (int)Math.floor(pos.z));
			for (int y = (int)(-radius); y <= radius; y++)
			{
				for (int x = (int)Math.ceil(-radius); x <= Math.ceil(radius); x++)
				{
					for (int z = (int)Math.ceil(-radius); z <= Math.ceil(radius); z++)
					{
						if(new Vec3d(pos.x + x, pos.y + y, pos.z + z).distanceTo(pos) > radius)
							continue;
						BlockPos pos1 = new BlockPos(center.getX() + x, center.getY() + y, center.getZ() + z);
						//explosions with 0 damage can only break fragile blocks, as they don't actually count as explosions
						//and are used for misc block breaking like the piercer revolvers alt fire
						if(world.getBlockState(pos1).isIn(damage > 0f ? TagRegistry.EXPLOSION_BREAKABLE : TagRegistry.FRAGILE) && exploder.canModifyAt(world, pos1))
							world.breakBlock(pos1, true, exploder);
					}
				}
			}
		}
	}
}
