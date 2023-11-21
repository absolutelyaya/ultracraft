package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.UltraComponents;
import absolutelyaya.ultracraft.damage.DamageTypeTags;
import absolutelyaya.ultracraft.item.PlushieItem;
import absolutelyaya.ultracraft.particle.ExplosionParticleEffect;
import absolutelyaya.ultracraft.registry.SoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity
{
	@Shadow private int health;
	
	public ItemEntityMixin(EntityType<?> type, World world)
	{
		super(type, world);
	}
	
	@Shadow public abstract ItemStack getStack();
	
	@Shadow public abstract @Nullable Entity getOwner();
	
	@Inject(method = "damage", at = @At("HEAD"))
	void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
	{
		if(getStack().getItem() instanceof PlushieItem && source.isIn(DamageTypeTags.EXPLODE_PLUSHIE) && health - amount <= 0f)
		{
			playSound(SoundRegistry.BAD_EXPLOSION, 0.75f, 1f);
			getWorld().sendEntityStatus(this, EntityStatuses.ADD_DEATH_PARTICLES);
			if(getOwner() instanceof PlayerEntity player)
			{
				for (int i = 0; i < getStack().getCount(); i++)
					UltraComponents.EASTER.get(player).addPlushie();
			}
		}
	}
	
	@Override
	public void handleStatus(byte status)
	{
		super.handleStatus(status);
		if (status != EntityStatuses.ADD_DEATH_PARTICLES)
			return;
		Vec3d pos = getPos();
		getWorld().addParticle(new ExplosionParticleEffect(1f), pos.x, pos.y - 0.5, pos.z, 0f, 0f, 0f);
	}
}
