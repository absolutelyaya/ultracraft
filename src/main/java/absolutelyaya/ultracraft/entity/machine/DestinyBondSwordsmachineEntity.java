package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.IDestinyBond;
import absolutelyaya.ultracraft.item.MachineSwordItem;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;
import java.util.UUID;

public class DestinyBondSwordsmachineEntity extends SwordsmachineEntity implements IDestinyBond
{
	protected static final TrackedData<Integer> PARTNER = DataTracker.registerData(DestinyBondSwordsmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> UN_STUN_TICKS = DataTracker.registerData(DestinyBondSwordsmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> HEALING = DataTracker.registerData(DestinyBondSwordsmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Integer> VARIANT = DataTracker.registerData(DestinyBondSwordsmachineEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final RawAnimation STUN_START_ANIM = RawAnimation.begin().thenPlay("stun_start").thenLoop("stun_loop");
	private static final RawAnimation STUN_STOP_ANIM = RawAnimation.begin().thenPlay("stun_end");
	protected static final byte ANIMATION_STUN_START = 8;
	protected static final byte ANIMATION_STUN_STOP = 9;
	boolean wasStunned = false;
	UUID shipUUID;
	boolean initalized = false;
	
	public DestinyBondSwordsmachineEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	DestinyBondSwordsmachineEntity(EntityType<? extends HostileEntity> entityType, World world, int variant)
	{
		super(entityType, world);
		dataTracker.set(VARIANT, variant);
		refreshSword();
		if(variant == 1)
			addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1, false, false));
		actuallyInitGoals();
	}
	
	public static DefaultAttributeContainer.Builder getDefaultAttributes()
	{
		return HostileEntity.createMobAttributes()
					   .add(EntityAttributes.GENERIC_MAX_HEALTH, 90.0d)
					   .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3d)
					   .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 8.0d)
					   .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0d)
					   .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0d);
	}
	
	@Override
	protected void initDataTracker()
	{
		dataTracker.startTracking(PARTNER, -1);
		dataTracker.startTracking(UN_STUN_TICKS, 0);
		dataTracker.startTracking(HEALING, 0);
		dataTracker.startTracking(VARIANT, 0);
		super.initDataTracker();
	}
	
	@Override
	public void onTrackedDataSet(TrackedData<?> data)
	{
		super.onTrackedDataSet(data);
		if(data.equals(HAS_SWORD) && dataTracker.get(HAS_SWORD))
			dataTracker.set(THROW_COOLDOWN, 200);
		if(data.equals(BREAKDOWN_TICKS))
		{
			if (dataTracker.get(BREAKDOWN_TICKS) == 0)
			{
				wasStunned = false;
				dataTracker.set(ANIMATION, ANIMATION_STUN_STOP);
				dataTracker.set(HEALING, 10);
				Entity partner = getWorld().getEntityById(getPartner());
				if(partner instanceof DestinyBondSwordsmachineEntity partnerSM)
					setHealth(partnerSM.getHealth());
			}
		}
		if(data.equals(UN_STUN_TICKS) && dataTracker.get(UN_STUN_TICKS) == 20)
		{
			dataTracker.set(ANIMATION, ANIMATION_IDLE);
			dataTracker.set(UN_STUN_TICKS, 0);
			dataTracker.set(HAS_SHOTGUN, true);
		}
	}
	
	public static List<Entity> spawn(World world, Vec3d pos, float yaw)
	{
		Vec3d facing = Vec3d.fromPolar(0, yaw);
		Vec3d right = facing.normalize().rotateY(90).multiply(1f);
		
		DestinyBondSwordsmachineEntity tundra = new DestinyBondSwordsmachineEntity(EntityRegistry.DESTINY_SWORDSMACHINE, world, 0);
		tundra.setPosition(pos.add(right));
		tundra.setCustomName(Text.translatable("item.ultracraft.machinesword.lore.tundra"));
		tundra.setBodyYaw(yaw);
		
		DestinyBondSwordsmachineEntity agony = new DestinyBondSwordsmachineEntity(EntityRegistry.DESTINY_SWORDSMACHINE, world, 1);
		agony.setPosition(pos.subtract(right));
		agony.setCustomName(Text.translatable("item.ultracraft.machinesword.lore.agony"));
		agony.setBodyYaw(yaw);
		
		world.spawnEntity(tundra);
		world.spawnEntity(agony);
		tundra.setPartner(agony.getId());
		agony.setPartner(tundra.getId());
		UUID ship = UUID.randomUUID();
		tundra.shipUUID = agony.shipUUID = ship;
		return List.of(tundra, agony);
	}
	
	@Override
	protected void initGoals()
	{
	}
	
	//I need this top happen after the variant has been set.
	void actuallyInitGoals()
	{
		if(getVariant() == 0) //Tundra only
		{
			goalSelector.add(0, new ShotgunGoal(this));
			goalSelector.add(1, new ThrowSwordGoal(this));
		}
		goalSelector.add(1, new SlashGoal(this));
		goalSelector.add(1, new ComboGoal(this));
		if(getVariant() == 0) //Tundra only
			goalSelector.add(1, new SpinGoal(this));
		goalSelector.add(3, new ChaseGoal(this));
		goalSelector.add(4, new LookAroundGoal(this));
		
		targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
		targetSelector.add(1, new RevengeGoal(this));
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("ship"))
			shipUUID = nbt.getUuid("ship");
		if(nbt.contains("variant", NbtElement.INT_TYPE))
		{
			dataTracker.set(VARIANT, nbt.getInt("variant"));
			refreshSword();
			if(getVariant() == 1)
				addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, Integer.MAX_VALUE, 1, false, false));
			actuallyInitGoals();
		}
	}
	
	private UUID getShipUUID()
	{
		return shipUUID;
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		if(shipUUID != null)
			nbt.putUuid("ship", shipUUID);
		nbt.putInt("variant", getVariant());
	}
	
	@Override
	public int getPartner()
	{
		return dataTracker.get(PARTNER);
	}
	
	@Override
	public void setPartner(int id)
	{
		dataTracker.set(PARTNER, id);
	}
	
	public boolean isLonely()
	{
		return getPartner() == -1;
	}
	
	@Override
	public boolean isStunned()
	{
		return dataTracker.get(BREAKDOWN_TICKS) > 0;
	}
	
	void bondShip()
	{
		if(shipUUID != null && isLonely())
		{
			getWorld().getEntitiesByType(TypeFilter.instanceOf(DestinyBondSwordsmachineEntity.class), getBoundingBox().expand(32), e -> e != this).forEach(e -> {
				if(e.getShipUUID().equals(shipUUID))
				{
					setPartner(e.getId());
					e.setPartner(getId());
				}
			});
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		if(dataTracker.get(HEALING) > 0)
		{
			bossBar.setPercent(MathHelper.lerp((10 - dataTracker.get(HEALING)) / 10f, 0f, getHealth() / getMaxHealth()));
			dataTracker.set(HEALING, dataTracker.get(HEALING) - 1);
		}
		if(dataTracker.get(ANIMATION) == ANIMATION_STUN_STOP)
			dataTracker.set(UN_STUN_TICKS, dataTracker.get(UN_STUN_TICKS) + 1);
		if(!initalized && getWorld().isChunkLoaded(getChunkPos().x, getChunkPos().z))
		{
			bondShip();
		}
	}
	
	@Override
	public void tickMovement()
	{
		super.tickMovement();
		if(getWorld().isClient && isLonely() && age % 6 == 0)
		{
			Vec3d pos = getEyePos().addRandom(random, 0.3f);
			getWorld().addParticle(ParticleTypes.SPLASH, pos.x, pos.y, pos.z, 0, 0, 0);
		}
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		Entity attacker = source.getAttacker();
		Entity ssource = source.getSource();
		Entity partner = getWorld().getEntityById(dataTracker.get(PARTNER));
		if((attacker != null && attacker.equals(partner)) || (ssource != null && ssource.equals(partner)))
			return false;
		if(!(partner instanceof DestinyBondSwordsmachineEntity partnerSM))
			return super.damage(source, amount);
		if(!source.isOf(DamageTypes.OUT_OF_WORLD) && isStunned() && !partnerSM.isStunned())
			return false;
		boolean b = super.damage(source, amount);
		if(b && dataTracker.get(HEALING) > 0)
			dataTracker.set(HEALING, 0);
		if(getHealth() <= 0 && !partnerSM.isStunned())
		{
			setHealth(1f);
			if(!wasStunned)
			{
				wasStunned = true;
				dataTracker.set(BREAKDOWN_TICKS, 100);
				dataTracker.set(ANIMATION, ANIMATION_STUN_START);
			}
			return false;
		}
		return b;
	}
	
	@Override
	public boolean canTakeDamage()
	{
		boolean b = super.canTakeDamage();
		Entity partner = getWorld().getEntityById(dataTracker.get(PARTNER));
		if(!(partner instanceof DestinyBondSwordsmachineEntity partnerSM))
			return b;
		if(getHealth() <= 1 && !partnerSM.isStunned())
			return false;
		return b;
	}
	
	@Override
	protected Identifier getLootTableId()
	{
		return getVariant() == 0 ? new Identifier(Ultracraft.MOD_ID, "entities/swordsmachine_tundra_death") :
					   new Identifier(Ultracraft.MOD_ID, "entities/swordsmachine_agony_death");
	}
	
	@Override
	public void onDeath(DamageSource damageSource)
	{
		if(!damageSource.isOf(DamageTypes.OUT_OF_WORLD) && getWorld().getEntityById(getPartner()) instanceof DestinyBondSwordsmachineEntity partnerSM && !partnerSM.isStunned())
			return;
		if(!isLonely())
		{
			Entity partner = getWorld().getEntityById(getPartner());
			if(partner instanceof DestinyBondSwordsmachineEntity destiny)
				destiny.setPartner(-1);
		}
		super.onDeath(damageSource);
	}
	
	@Override
	protected <E extends GeoEntity> PlayState predicate(AnimationState<E> event)
	{
		byte anim = dataTracker.get(ANIMATION);
		AnimationController<?> controller = event.getController();
		super.predicate(event);
		switch (anim)
		{
			case ANIMATION_STUN_START -> controller.setAnimation(STUN_START_ANIM);
			case ANIMATION_STUN_STOP -> controller.setAnimation(STUN_STOP_ANIM);
		}
		return PlayState.CONTINUE;
	}
	
	@Override
	protected boolean canLoseShotgun()
	{
		return false;
	}
	
	@Override
	public void setCustomName(@Nullable Text name)
	{
		super.setCustomName(name);
		bossBar.setName(name);
	}
	
	public int getVariant()
	{
		return dataTracker.get(VARIANT);
	}
	
	@Override
	protected MachineSwordItem.Type getSwordType()
	{
		if(getVariant() > 1)
			return MachineSwordItem.Type.NORMAL;
		return MachineSwordItem.Type.values()[getVariant() + 1];
	}
	
	void refreshSword()
	{
		dataTracker.set(SWORD_STACK, ItemRegistry.MACHINE_SWORD.getDefaultStack(getSwordType()));
	}
	
	@Override
	public void onInterrupt(PlayerEntity parrier)
	{
		if(getVariant() == 0)
			dataTracker.set(BREAKDOWN_TICKS, 50);
	}
}
