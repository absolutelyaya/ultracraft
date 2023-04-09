package absolutelyaya.ultracraft.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;
import java.util.function.Supplier;

public class RenderLayers extends RenderLayer
{
	public RenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction)
	{
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}
	
	public static RenderLayer getShockWave(Identifier texture)
	{
		return SHOCKWAVE.apply(texture);
	}
	
	public static RenderLayer getLightTrail()
	{
		return LIGHT_TRAIL.get();
	}
	
	private static final Function<Identifier, RenderLayer> SHOCKWAVE = Util.memoize((texture) -> {
		RenderLayer.MultiPhaseParameters multiPhaseParameters =
				RenderLayer.MultiPhaseParameters.builder().program(BEACON_BEAM_PROGRAM)
						.texture(new Texture(texture, false, false)).transparency(LIGHTNING_TRANSPARENCY)
						.writeMaskState(ALL_MASK).cull(DISABLE_CULLING).build(false);
		return RenderLayer.of("shockwave", VertexFormats.POSITION_COLOR_TEXTURE, VertexFormat.DrawMode.QUADS, 256, false, true, multiPhaseParameters);
	});
	
	private static final Supplier<RenderLayer> LIGHT_TRAIL = (() -> {
		RenderLayer.MultiPhaseParameters multiPhaseParameters =
				RenderLayer.MultiPhaseParameters.builder().program(LIGHTNING_PROGRAM).layering(VIEW_OFFSET_Z_LAYERING)
						.transparency(RenderPhase.LIGHTNING_TRANSPARENCY).writeMaskState(ALL_MASK).cull(DISABLE_CULLING).build(false);
		return RenderLayer.of("light_trail", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, false, true, multiPhaseParameters);
	});
}
