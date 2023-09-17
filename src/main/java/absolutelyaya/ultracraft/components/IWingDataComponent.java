package absolutelyaya.ultracraft.components;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import org.joml.Vector3f;

public interface IWingDataComponent extends ComponentV3
{
	Vector3f[] getColors();
	void setColor(Vector3f val, int idx);
	String getPattern();
	void setPattern(String id);
	boolean isVisible();
	void setVisible(boolean b);
	void sync();
}
