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
	static final String PresetFolderName = "UltraWingPresets";
	
	public static void restoreDefaults()
	{
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		Path gameDir = FabricLoader.getInstance().getGameDir();
		Path presetDir = Path.of(gameDir.toString(), PresetFolderName);
		Path resourcepackDir = Path.of(gameDir.toString(), "resourcepacks");
		ModContainer container = optionalContainer.get();
		Optional<Path> internalPresetsDir = container.findPath("wing_presets");
		if(internalPresetsDir.isEmpty())
		{
			Ultracraft.LOGGER.info("Internal Wing Color Preset data not found!");
			return;
		}
		List<Path> files = new ArrayList<>();
		Map<Path, String> resourceFiles = new HashMap<>();
		Queue<Path> paths = new ArrayDeque<>();
		//fetch jar internal presets
		paths.add(internalPresetsDir.get());
		while(paths.size() > 0)
		{
			Path p = paths.remove();
			try(Stream<Path> stream = Files.list(p))
			{
				for (Path f : stream.toList())
				{
					if(Files.isDirectory(f))
						paths.add(f);
					else
						files.add(f);
				}
			}
			catch (IOException e)
			{
				Ultracraft.LOGGER.error("Failed to load default Wing Color Presets", e);
				return;
			}
		}
		//fetch resourcepack presets
		try(Stream<Path> stream = Files.list(resourcepackDir))
		{
			for (Path f : stream.toList())
			{
				if(Files.isDirectory(f))
					paths.add(f);
			}
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Error while getting Resourcepacks", e);
			return;
		}
		while(paths.size() > 0)
		{
			Path p = paths.remove();
			String[] pathSegments = p.toString().split("\\\\");
			String name = pathSegments[pathSegments.length - 1];
			p = Path.of(p.toString(), "ultracraft", "wing_presets");
			if(Files.notExists(p))
				continue;
			try(Stream<Path> stream = Files.list(p))
			{
				for (Path f : stream.toList())
				{
					if(!Files.isDirectory(f))
						resourceFiles.put(f, name);
				}
			}
			catch (IOException e)
			{
				Ultracraft.LOGGER.error("Error while loading Color Presets from Resourcepacks", e);
				return;
			}
		}
		int resourcepackImports = 0;
		try
		{
			Files.createDirectories(presetDir);
			for (Path internalFile : files)
			{
				String[] s = internalFile.toString().split("/");
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
					Ultracraft.LOGGER.error("Failed to save default Wing Color Preset '{}'", id, e);
				}
			}
			for (Path resourceFile : resourceFiles.keySet())
			{
				String source = resourceFiles.get(resourceFile);
				String[] s = resourceFile.toString().split("/");
				String id = s[s.length - 1];
				Path filePath = Path.of(presetDir.toString(), source, id);
				Files.createDirectories(Path.of(presetDir.toString(), source));
				if(Files.exists(filePath))
					continue;
				try
				{
					JsonObject json = JsonHelper.deserialize(Files.readString(resourceFile));
					json.addProperty("resourcepack", source);
					Files.createFile(filePath);
					Files.writeString(filePath, JsonHelper.toSortedString(json));
					resourcepackImports++;
				}
				catch (IllegalArgumentException | JsonParseException e)
				{
					Ultracraft.LOGGER.error("Failed to save Wing Color Preset '{}' from Resourcepack '{}'", id, source, e);
				}
			}
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Failed to save Wing Color Presets", e);
			return;
		}
		Ultracraft.LOGGER.info("Loaded Default Wing Color Presets. ({} newly imported from resourcepacks)", resourcepackImports);
	}
	
	public static void loadPresets()
	{
		presets.clear();
		Path gameDir = FabricLoader.getInstance().getGameDir();
		Path presetDir = Path.of(gameDir.toString(), PresetFolderName);
		if(!Files.exists(presetDir))
			restoreDefaults();
		Queue<Path> paths = new ArrayDeque<>();
		//fetch presets
		paths.add(presetDir);
		while(paths.size() > 0)
		{
			Path p = paths.remove();
			try(Stream<Path> stream = Files.list(p))
			{
				for (Path f : stream.toList())
				{
					if(Files.isDirectory(f))
					{
						paths.add(f);
						continue;
					}
					String[] s = f.toString().split("\\\\");
					String id = s[s.length - 1];
					Path filePath = Path.of(f.toString(), id);
					if(Files.exists(filePath))
						continue;
					try
					{
						String json = Files.readString(f);
						presets.put(id, deserialize(JsonHelper.deserialize(json)));
					}
					catch (IllegalArgumentException | JsonParseException e)
					{
						Ultracraft.LOGGER.error("Failed to load Wing Color Preset '{}'", id, e);
					}
				}
			}
			catch (IOException e)
			{
				Ultracraft.LOGGER.error("Failed to load Wing Color Presets", e);
				return;
			}
		}
		Ultracraft.LOGGER.info("Loaded {} Wing Color Presets.", presets.size());
	}
	
	public static void unloadPresets()
	{
		presets.clear();
		Ultracraft.LOGGER.info("Unloaded all Wing Color Presets.");
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
		String author = JsonHelper.hasString(json, "author") ? JsonHelper.getString(json, "author") : null;
		String source = JsonHelper.hasString(json, "resourcepack") ? JsonHelper.getString(json, "resourcepack") : null;
		
		return new WingColorPreset(wing, metal, text, JsonHelper.getString(json, "name"), author, source);
	}
	
	public static WingColorPreset getPreset(String id)
	{
		return presets.get(id);
	}
	
	public static List<String> getAllIDs()
	{
		return new ArrayList<>(presets.keySet());
	}
	
	public record WingColorPreset(Vec3d wings, Vec3d metal, Vec3d text, String name, String author, String source) {}
}
