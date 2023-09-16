package absolutelyaya.ultracraft.components;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class WingDataComponent implements IWingDataComponent
{
	Vector3f[] wingColors = new Vector3f[] { new Vector3f(247f / 255f, 1f, 154f / 255f), new Vector3f(117f / 255f, 154f / 255f, 1f) };
	String wingPattern = "";
	boolean visible;
	PlayerEntity provider;
	
	public WingDataComponent(PlayerEntity entity)
	{
		provider = entity;
	}
	
	@Override
	public Vector3f[] getColors()
	{
		return wingColors;
	}
	
	@Override
	public void setColor(Vector3f val, int idx)
	{
		wingColors[idx] = val;
	}
	
	@Override
	public String getPattern()
	{
		return wingPattern;
	}
	
	@Override
	public void setPattern(String id)
	{
		wingPattern = id;
	}
	
	@Override
	public boolean isVisible()
	{
		return visible;
	}
	
	@Override
	public void setVisible(boolean b)
	{
		visible = b;
	}
	
	NbtCompound serializeColor(Vector3f color)
	{
		NbtCompound c = new NbtCompound();
		c.putFloat("r", color.x);
		c.putFloat("g", color.y);
		c.putFloat("b", color.z);
		return c;
	}
	
	Vector3f deserializeColor(NbtCompound nbt)
	{
		return new Vector3f(nbt.getFloat("r"), nbt.getFloat("g"), nbt.getFloat("b"));
	}
	
	@Override
	public void readFromNbt(NbtCompound tag)
	{
		NbtCompound colors = tag.getCompound("colors");
		wingColors[0] = deserializeColor(colors.getCompound("wings"));
		wingColors[1] = deserializeColor(colors.getCompound("metal"));
		wingPattern = tag.getString("pattern");
		visible = tag.getBoolean("visible");
	}
	
	@Override
	public void writeToNbt(NbtCompound tag)
	{
		NbtCompound colors = new NbtCompound();
		colors.put("wings", serializeColor(wingColors[0]));
		colors.put("metal", serializeColor(wingColors[1]));
		tag.put("colors", colors);
		tag.putString("pattern", wingPattern);
		tag.putBoolean("visible", visible);
	}
}
