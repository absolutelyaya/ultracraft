package absolutelyaya.ultracraft.client.gui.terminal.elements;

import net.minecraft.util.Identifier;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class Sprite implements Element
{
	final Identifier tex;
	Vector3f pos;
	Vector2i uv, size, texSize;
	int color = 0xffffffff;
	boolean centered;
	
	public Sprite(Identifier tex, Vector2i pos, float z, Vector2i size, Vector2i uv, Vector2i texSize)
	{
		this.tex = tex;
		this.pos = new Vector3f(pos.x, pos.y, z);
		this.size = size;
		this.uv = uv;
		this.texSize = texSize;
	}
	
	public Sprite setTint(int color)
	{
		this.color = color;
		return this;
	}
	
	public Sprite setCentered(boolean centered)
	{
		this.centered = centered;
		return this;
	}
	
	public Identifier getTex()
	{
		return tex;
	}
	
	public Vector3f getPos()
	{
		return pos;
	}
	
	public void setPos(Vector3f pos)
	{
		this.pos = pos;
	}
	
	public Vector2i getUv()
	{
		return uv;
	}
	
	public void setUv(Vector2i uv)
	{
		this.uv = uv;
	}
	
	public Vector2i getSize()
	{
		return size;
	}
	
	public void setSize(Vector2i size)
	{
		this.size = size;
	}
	
	public Vector2i getTexSize()
	{
		return texSize;
	}
	
	public int getColor()
	{
		return color;
	}
	
	public boolean isCentered()
	{
		return centered;
	}
}
