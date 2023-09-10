package absolutelyaya.ultracraft.compat;

import absolutelyaya.ultracraft.client.RenderLayers;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;

public class SodiumWorldRenderStuff
{
	public static final TerrainRenderPass FLESH_PASS = new TerrainRenderPass(RenderLayers.getFlesh(), false, false);
	public static final Material FLESH_MAT = new Material(FLESH_PASS, AlphaCutoffParameter.ZERO, true);
}
