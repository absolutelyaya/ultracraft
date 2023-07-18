package absolutelyaya.ultracraft.entity.machine;

import absolutelyaya.ultracraft.accessor.IDestinyBond;
import absolutelyaya.ultracraft.item.MachineSwordItem;
import absolutelyaya.ultracraft.registry.EntityRegistry;
import absolutelyaya.ultracraft.registry.ItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
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
	
	public DestinyBondSwordsmachineEntity(EntityType<? extends HostileEntity> entityType, World world)
	{
		super(entityType, world);
	}
	
	@Override
	protected void initDataTracker()
	{
		dataTracker.startTracking(PARTNER, 0);
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
		
		DestinyBondSwordsmachineEntity tundra = new DestinyBondSwordsmachineEntity(EntityRegistry.DESTINY_SWORDSMACHINE, world);
		tundra.setPosition(pos.add(right));
		tundra.dataTracker.set(VARIANT, 0);
		tundra.refreshSword();
		tundra.setCustomName(Text.of("Tundra").getWithStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.DARK_BLUE)).withBold(true)).get(0));
		tundra.setBodyYaw(yaw);
		
		DestinyBondSwordsmachineEntity agony = new DestinyBondSwordsmachineEntity(EntityRegistry.DESTINY_SWORDSMACHINE, world);
		agony.setPosition(pos.subtract(right));
		agony.dataTracker.set(VARIANT, 1);
		agony.refreshSword();
		agony.setCustomName(Text.of("Agony").getWithStyle(Style.EMPTY.withColor(TextColor.fromFormatting(Formatting.DARK_RED)).withBold(true)).get(0));
		agony.setBodyYaw(yaw);
		
		world.spawnEntity(tundra);
		world.spawnEntity(agony);
		tundra.setPartner(agony.getId());
		agony.setPartner(tundra.getId());
		return List.of(tundra, agony);
	}
	
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt)
	{
		super.readCustomDataFromNbt(nbt);
		if(nbt.contains("partner", NbtElement.INT_TYPE))
			setPartner(nbt.getInt("partner"));
		if(nbt.contains("variant", NbtElement.INT_TYPE))
			dataTracker.set(VARIANT, nbt.getInt("variant"));
	}
	
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt)
	{
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("partner", getPartner());
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
	
	@Override
	public boolean isStunned()
	{
		return dataTracker.get(BREAKDOWN_TICKS) > 0;
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
	}
	
	@Override
	public boolean damage(DamageSource source, float amount)
	{
		Entity attacker = source.getAttacker();
		if(attacker != null && attacker.getId() == dataTracker.get(PARTNER))
			return false;
		Entity partner = getWorld().getEntityById(dataTracker.get(PARTNER));
		if(!(partner instanceof DestinyBondSwordsmachineEntity partnerSM))
			return super.damage(source, amount);
		if(isStunned() && !partnerSM.isStunned())
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
				dataTracker.set(BREAKDOWN_TICKS, 200);
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
	public void onDeath(DamageSource damageSource)
	{
		if(!damageSource.isOf(DamageTypes.OUT_OF_WORLD) && getWorld().getEntityById(getPartner()) instanceof DestinyBondSwordsmachineEntity partnerSM && !partnerSM.isStunned())
			return;
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
		if(name.getString().equalsIgnoreCase("tundra") || name.getString().equalsIgnoreCase("agony"))
			bossBar.setName(name);
		else
			bossBar.setName(Text.translatable("entity.ultracraft.swordsmachine-named", name));
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
}
