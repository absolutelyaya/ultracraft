package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.LivingEntityAccessor;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class ExplosionHandler
{
	static final Random random = new java.util.Random();
	
	public static void explosion(Entity ignored, World world, Vec3d pos, DamageSource source, float damage, float falloff, float radius)
	{
		if(world.isClient)
			explosionClient((ClientWorld)world, pos, radius);
		else
		{
			explosionServer(ignored, (ServerWorld)world, pos, source, damage, falloff, radius);
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
	
	private static void explosionServer(Entity ignored, ServerWorld world, Vec3d pos, DamageSource source, float damage, float falloff, float radius)
	{
		Box box = new Box(pos.subtract(radius, radius, radius), pos.add(radius, radius, radius));
		world.getOtherEntities(ignored, box, Entity::isLiving).forEach(e -> {
			float normalizedDistance = (float)e.squaredDistanceTo(pos) / (radius * radius);
			e.damage(source, MathHelper.lerp(normalizedDistance, damage, Math.max(damage - falloff, 0f)));
			if(!(e instanceof PlayerEntity))
				e.setOnFireFor(10);
			if(e instanceof LivingEntityAccessor living && !living.takePunchKnockback())
				return;
			e.addVelocity(pos.subtract(e.getPos()).normalize().multiply((radius / 5f) * (1f - normalizedDistance)));
		});
	}
}
