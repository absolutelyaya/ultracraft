package absolutelyaya.ultracraft.entity.demon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

public class HideousPart extends Entity
{
	public final HideousMassEntity owner;
	public final String name;
	private EntityDimensions dimensions;
	private final boolean deflect;
	
	boolean enabled = true;
	Vec2f targetDimensions;
	
	public HideousPart(HideousMassEntity owner, String name, Vec2f dimensions, boolean deflect)
	{
		super(owner.getType(), owner.getWorld());
		this.owner = owner;
		this.name = name;
		this.dimensions = EntityDimensions.changing(dimensions.x, dimensions.y);
		calculateDimensions();
		this.deflect = deflect;
	}
	
	@Override
	public boolean canHit()
	{
		return enabled;
	}
	
	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt)
	{
	
	}
	
	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt)
	{
	
	}
	
	public boolean damage(DamageSource source, float amount)
	{
		if(!deflect && enabled)
			return owner.damagePart(this, source, amount);
		return false;
	}
	
	public boolean isPartOf(Entity entity)
	{
		return this == entity || owner == entity;
	}
	
	public Packet<ClientPlayPacketListener> createSpawnPacket()
	{
		throw new UnsupportedOperationException();
	}
	
	public EntityDimensions getDimensions(EntityPose pose)
	{
		return dimensions;
	}
	
	public boolean shouldSave()
	{
		return false;
	}
	
	@Override
	public void tick()
	{
	
	}
	
	public Vector4f getBoxColor()
	{
		if(!enabled)
			return new Vector4f(0.4f, 0.4f, 0.4f, 0.5f);
		if(deflect)
			return new Vector4f(0.5f, 0.75f, 1f, 1f);
		if(name.equals("entrails") || name.equals("tail"))
			return new Vector4f(1f, 0f, 0f, 1f);
		return new Vector4f(1f, 1f, 1f, 1f);
	}
	
	@Override
	protected void initDataTracker()
	{
	
	}
	
	@Override
	protected Box calculateBoundingBox()
	{
		if(!enabled)
			return new Box(0f, 0f, 0f, 0f, 0f, 0f);
		return super.calculateBoundingBox();
	}
	
	@Override
	protected Box calculateBoundsForPose(EntityPose pos)
	{
		return calculateBoundingBox();
	}
	
	@Override
	public Text getName()
	{
		return Text.translatable("entity.ultracraft.hideous_part", name);
	}
	
	@Nullable
	@Override
	public ItemStack getPickBlockStack()
	{
		return owner.getPickBlockStack();
	}
	
	public void setTargetDimensions(Vec2f dimensions)
	{
	
	}
}
