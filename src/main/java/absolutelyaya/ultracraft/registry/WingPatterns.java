package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.client.UltracraftClient;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class WingPatterns
{
	private static ShaderProgram gamerProgram, sunburstProgram, monochromeProgram, ripplesProgram, ripplesClrProgram;
	private static ShaderProgram gamerPreviewProgram, sunburstPreviewProgram, monochromePreviewProgram, ripplesPreviewProgram, ripplesClrPreviewProgram;
	private static final Map<String, WingPattern> patterns;
	
	public static void init()
	{
		CoreShaderRegistrationCallback.EVENT.register((callback) -> {
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/gamer"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				gamerProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/sunburst"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				sunburstProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/monochrome"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				monochromeProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/ripples"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				ripplesProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/ripples-clr"), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				ripplesClrProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/preview/gamer"), VertexFormats.POSITION_TEXTURE_COLOR, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				gamerPreviewProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/preview/sunburst"), VertexFormats.POSITION_TEXTURE_COLOR, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				sunburstPreviewProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/preview/monochrome"), VertexFormats.POSITION_TEXTURE_COLOR, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				monochromePreviewProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/preview/ripples"), VertexFormats.POSITION_TEXTURE_COLOR, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				ripplesPreviewProgram = program;
			});
			callback.register(new Identifier(Ultracraft.MOD_ID, "wing-patterns/preview/ripples-clr"), VertexFormats.POSITION_TEXTURE_COLOR, (program) -> {
				program.getUniform("MetalColor");
				program.getUniform("WingColor");
				program.markUniformsDirty();
				ripplesClrPreviewProgram = program;
			});
		});
	}
	
	public static WingPattern getPattern(String id)
	{
		return patterns.get(id);
	}
	
	public static List<String> getAllIDs()
	{
		return patterns.keySet().stream().toList();
	}
	
	public static ShaderProgram getGamerShaderProgram()
	{
		return gamerProgram;
	}
	
	public static ShaderProgram getGamerPreviewShaderProgram()
	{
		return gamerPreviewProgram;
	}
	
	public static ShaderProgram getSunburstShaderProgram()
	{
		return sunburstProgram;
	}
	
	public static ShaderProgram getSunburstPreviewShaderProgram()
	{
		return sunburstPreviewProgram;
	}
	
	public static ShaderProgram getMonochromeShaderProgram()
	{
		return monochromeProgram;
	}
	
	public static ShaderProgram getMonochromePreviewShaderProgram()
	{
		return monochromePreviewProgram;
	}
	
	public static ShaderProgram getRipplesShaderProgram()
	{
		return ripplesProgram;
	}
	
	public static ShaderProgram getRipplesPreviewShaderProgram()
	{
		return ripplesPreviewProgram;
	}
	
	public static ShaderProgram getRipplesClrShaderProgram()
	{
		return ripplesClrProgram;
	}
	
	public static ShaderProgram getRipplesClrPreviewShaderProgram()
	{
		return ripplesClrPreviewProgram;
	}
	
	public static Supplier<ShaderProgram> getProgram(String id)
	{
		if(!patterns.containsKey(id))
			return patterns.get("none").program;
		return patterns.get(id).program;
	}
	
	static
	{
		ImmutableMap.Builder<String, WingPattern> builder = ImmutableMap.builder();
		builder.put("none", new WingPattern(UltracraftClient::getWingsColoredShaderProgram, UltracraftClient::getWingsColoredUIShaderProgram,
				new Vec3d(255, 255, 255).multiply(1f / 255f), false));
		builder.put("gamer", new WingPattern(WingPatterns::getGamerShaderProgram, WingPatterns::getGamerPreviewShaderProgram,
				new Vec3d(255, 255, 255).multiply(1f / 255f), true));
		builder.put("sunburst", new WingPattern(WingPatterns::getSunburstShaderProgram, WingPatterns::getSunburstPreviewShaderProgram,
				new Vec3d(133, 36, 46).multiply(1f / 255f), true));
		builder.put("monochrome", new WingPattern(WingPatterns::getMonochromeShaderProgram, WingPatterns::getMonochromePreviewShaderProgram,
				new Vec3d(255, 255, 255).multiply(1f / 255f), true));
		builder.put("ripples", new WingPattern(WingPatterns::getRipplesShaderProgram, WingPatterns::getRipplesPreviewShaderProgram,
				new Vec3d(255, 255, 255).multiply(1f / 255f), true));
		builder.put("ripples-clr", new WingPattern(WingPatterns::getRipplesClrShaderProgram, WingPatterns::getRipplesClrPreviewShaderProgram,
				new Vec3d(255, 255, 255).multiply(1f / 255f), true));
		patterns = builder.build();
	}
	
	public record WingPattern(Supplier<ShaderProgram> program, Supplier<ShaderProgram> previewProgram, Vec3d textColor, boolean hasFlavor) {}
}
