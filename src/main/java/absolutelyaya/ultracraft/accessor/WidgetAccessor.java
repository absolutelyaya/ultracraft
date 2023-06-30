package absolutelyaya.ultracraft.accessor;

import org.joml.Vector2i;

public interface WidgetAccessor
{
	void setOffset(Vector2i pos);
	
	Vector2i getOffset();
	
	void setActive(boolean b);
	
	void setAlpha(float alpha);
}
