package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class WingColorPresetManager
{
	private static final Map<String, WingColorPreset> presets = new HashMap<>();
	
	public static void restoreDefaults()
	{
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		Path gameDir = FabricLoader.getInstance().getGameDir();
		Path presetDir = Path.of(gameDir.toString(), "HivelWingPresets");
		ModContainer container = optionalContainer.get();
		Optional<Path> op = container.findPath("wing_presets");
		if(op.isEmpty())
		{
			Ultracraft.LOGGER.info("Internal wing color Preset data not found!");
			return;
		}
		Path p = op.get();
		try(Stream<Path> stream = Files.list(p))
		{
			Files.createDirectories(presetDir);
			List<Path> files = stream.filter(file -> file.toFile().isFile()).toList();
			for (Path internalFile : files)
			{
				System.out.println(internalFile);
				String[] s = internalFile.toString().split("\\\\");
				String id = s[s.length - 1];
				Path filePath = Path.of(presetDir.toString(), id);
				if(Files.exists(filePath))
					continue;
				try
				{
					String json = Files.readString(internalFile);
					Files.writeString(filePath, json);
				}
				catch (IllegalArgumentException | JsonParseException e)
				{
					Ultracraft.LOGGER.error("Parsing error loading Wing Color Preset '{}'", id, e);
				}
			}
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Error while loading default Wing Color Presets", e);
			return;
		}
		Ultracraft.LOGGER.info("Loaded Default Wing Color Presets.");
	}
	
	public static void loadPresets()
	{
		presets.clear();
		Path gameDir = FabricLoader.getInstance().getGameDir();
		Path presetDir = Path.of(gameDir.toString(), "HivelWingPresets");
		if(!Files.exists(presetDir))
			restoreDefaults();
		try(Stream<Path> stream = Files.list(presetDir))
		{
			List<Path> files = stream.filter(file -> file.toFile().isFile()).toList();
			for (Path file : files)
			{
				String[] s = file.toString().split("\\\\");
				String id = s[s.length - 1];
				Path filePath = Path.of(file.toString(), id);
				if(Files.exists(filePath))
					continue;
				try
				{
					String json = Files.readString(file);
					presets.put(id, deserialize(JsonHelper.deserialize(json)));
				}
				catch (IllegalArgumentException | JsonParseException e)
				{
					Ultracraft.LOGGER.error("Parsing error loading Wing Color Preset '{}'", id, e);
				}
			}
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Error while loading default Wing Color Presets", e);
			return;
		}
		Ultracraft.LOGGER.info("Loaded {} Wing Color Presets.", presets.size());
	}
	
	public static void unloadPresets()
	{
		presets.clear();
	}
	
	public static WingColorPreset deserialize(JsonObject json)
	{
		JsonArray wingColor = JsonHelper.getArray(json, "wings");
		JsonArray metalColor = JsonHelper.getArray(json, "metal");
		JsonArray textColor = JsonHelper.getArray(json, "text");
		Vec3d wing = new Vec3d(wingColor.get(0).getAsDouble(), wingColor.get(1).getAsDouble(), wingColor.get(2).getAsDouble());
		if(wing.length() > 3f)
			wing = wing.multiply(1f / 255f);
		Vec3d metal = new Vec3d(metalColor.get(0).getAsDouble(), metalColor.get(1).getAsDouble(), metalColor.get(2).getAsDouble());
		if(metal.length() > 3f)
			metal = metal.multiply(1f / 255f);
		Vec3d text = new Vec3d(textColor.get(0).getAsDouble(), textColor.get(1).getAsDouble(), textColor.get(2).getAsDouble());
		if(text.length() > 3f)
			text = text.multiply(1f / 255f);
		return new WingColorPreset(wing, metal, text, JsonHelper.getString(json, "name"));
	}
	
	public static WingColorPreset getPreset(String id)
	{
		return presets.get(id);
	}
	
	public static List<String> getAllIDs()
	{
		return new ArrayList<>(presets.keySet());
	}
	
	public record WingColorPreset(Vec3d wings, Vec3d metal, Vec3d text, String name) {}
}
