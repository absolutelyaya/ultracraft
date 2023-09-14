package absolutelyaya.ultracraft.entity.projectile;

import absolutelyaya.ultracraft.damage.DamageSources;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ThrownSoapEntity extends ThrownItemEntity
{
	public ThrownSoapEntity(EntityType<? extends ThrownItemEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected Item getDefaultItem()
	{
		return ItemRegistry.SOAP;
	}
	
	public static ThrownSoapEntity spawn(LivingEntity owner, ItemStack stack)
	{
		ThrownSoapEntity soap = new ThrownSoapEntity(EntityRegistry.SOAP, owner.getWorld());
		soap.setPosition(owner.getEyePos());
		soap.setVelocity(owner.getRotationVector());
		soap.setItem(stack);
		soap.setOwner(owner);
		owner.getWorld().spawnEntity(soap);
		return soap;
	}
	
	@Override
	protected void onEntityHit(EntityHitResult entityHitResult)
	{
		super.onEntityHit(entityHitResult);
		Entity e = entityHitResult.getEntity();
		if(!canHit(e))
			return;
		if(!getWorld().isClient)
		{
			e.damage(DamageSources.get(getWorld(), DamageSources.SOAP, this, getOwner()), 999999);
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeVector3f(e.getBoundingBox().getCenter().toVector3f());
			buf.writeDouble(e.getWidth());
			buf.writeDouble(e.getHeight());
			for (ServerPlayerEntity p : getWorld().getEntitiesByType(TypeFilter.instanceOf(ServerPlayerEntity.class), getBoundingBox().expand(128f), i -> true))
				ServerPlayNetworking.send(p, PacketRegistry.SOAP_KILL_PACKET_ID, buf);
		}
	}
	
	@Override
	protected void onBlockHit(BlockHitResult blockHitResult)
	{
		if(getOwner().isPlayer() && !((PlayerEntity)getOwner()).isCreative())
			dropItem(getStack().getItem());
		super.onBlockHit(blockHitResult);
	}
	
	@Override
	protected void onCollision(HitResult hitResult)
	{
		super.onCollision(hitResult);
		if (getWorld().isClient)
			return;
		getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
		discard();
	}
	
	@Override
	public void handleStatus(byte status) {
		if (status != EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
			return;
		for (int i = 0; i < 16; i++)
		{
			Vec3d pos = getPos().addRandom(random, 0.1f);
			Vec3d vel = getVelocity().addRandom(random, 1f);
			getWorld().addParticle(new ItemStackParticleEffect(ParticleTypes.ITEM, getStack()), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
		}
	}
}
