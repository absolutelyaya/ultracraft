package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.registry.WingPatterns;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RenderLayers extends RenderLayer
{
	public static final ShaderProgram WINGS_COLORED_PROGRAM = new ShaderProgram(UltracraftClient::getWingsColoredShaderProgram);
	
	public RenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, Runnable startAction, Runnable endAction)
	{
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}
	
	public static RenderLayer getShockWave(Identifier texture)
	{
		return SHOCKWAVE.apply(texture);
	}
	public static RenderLayer getWingsPattern(Identifier tex, String id)
	{
		return WINGS_PATTERN.apply(tex, id);
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
	
	private static final BiFunction<Identifier, String, RenderLayer> WINGS_PATTERN = Util.memoize((texture, id) -> {
		RenderLayer.MultiPhaseParameters multiPhaseParameters =
				RenderLayer.MultiPhaseParameters.builder().program(new ShaderProgram(WingPatterns.getProgram(id))).texture(new RenderPhase.Texture(texture, false, false))
						.overlay(RenderPhase.DISABLE_OVERLAY_COLOR).transparency(NO_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP)
						.build(true);
		return of("wings_colored", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 256, true, false, multiPhaseParameters);
	});
}
