package absolutelyaya.ultracraft.accessor;

import absolutelyaya.ultracraft.entity.demon.HideousPart;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public interface ServerWorldAccessor
{
	Int2ObjectMap<HideousPart> getHideousParts();
}
