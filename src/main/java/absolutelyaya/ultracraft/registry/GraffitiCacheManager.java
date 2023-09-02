package absolutelyaya.ultracraft.registry;

import absolutelyaya.ultracraft.Ultracraft;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class GraffitiCacheManager
{
	static final String CACHE_PATH = "Ultracraft/cache/graffiti";
	static final Path cacheDir;
	
	public static Graffiti fetchGrafitti(UUID terminalID)
	{
		try(Stream<Path> stream = Files.list(cacheDir))
		{
			for (Path f : stream.toList())
			{
				if(!Files.isDirectory(f) && f.getFileName().toString().split("\\.")[0].equals(terminalID.toString()))
				{
					String json = Files.readString(f);
					return deserialize(JsonHelper.deserialize(json));
				}
			}
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Failed to search Grafitti Cache", e);
			return null;
		}
		return null;
	}
	
	public static boolean hasNewest(UUID terminalID, int offeredRevision)
	{
		try(Stream<Path> stream = Files.list(cacheDir))
		{
			for (Path f : stream.toList())
			{
				String[] segments = f.getFileName().toString().split("\\.");
				if(!Files.isDirectory(f) && segments.length == 2 && segments[1].equals(".json") && segments[0].equals(terminalID.toString()))
				{
					String json = Files.readString(f);
					Graffiti g = deserialize(JsonHelper.deserialize(json));
					return offeredRevision < g.revision;
				}
			}
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Failed to search Grafitti Cache", e);
		}
		return false;
	}
	
	public static void cacheGraffiti(UUID id, List<Integer> palette, List<Byte> pixels, int revision)
	{
		JsonObject json = new JsonObject();
		JsonArray paletteProperty = new JsonArray();
		for (int color : palette)
			paletteProperty.add(color);
		json.add("palette", paletteProperty);
		json.addProperty("pixels", serializePixels(pixels));
		json.addProperty("revision", revision);
		
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		Path gameDir = FabricLoader.getInstance().getGameDir();
		try
		{
			Files.writeString(Path.of(gameDir.toString(), CACHE_PATH, id.toString() + ".json"), JsonHelper.toSortedString(json));
			//Ultracraft.LOGGER.info("Successfully Cached Grafitti from Terminal {}", id);
		}
		catch (IllegalArgumentException | JsonParseException | IOException e)
		{
			Ultracraft.LOGGER.error("Failed to cache Grafitti from Terminal {}", id, e);
		}
	}
	
	public static void savePng(UUID id, NativeImage image)
	{
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		Path gameDir = FabricLoader.getInstance().getGameDir();
		try
		{
			image.writeTo(Path.of(gameDir.toString(), CACHE_PATH, id.toString() + ".png"));
		}
		catch (IllegalArgumentException | JsonParseException | IOException e)
		{
			Ultracraft.LOGGER.error("Failed to cache Grafitti from Terminal {}", id, e);
		}
	}
	
	public static String serializePixels(List<Byte> pixelPairs)
	{
		StringBuilder out = new StringBuilder();
		for (byte b : pixelPairs)
			out.append(Integer.toHexString(b));
		return out.toString();
	}
	
	public static Graffiti deserialize(JsonObject object)
	{
		JsonArray palette = JsonHelper.getArray(object, "palette");
		List<Integer> paletteOut = new ArrayList<>();
		for (JsonElement i : palette)
			paletteOut.add(i.getAsInt());
		
		String pixelString = object.get("pixels").getAsString();
		List<Byte> pixels = new ArrayList<>();
		for (int i = 0; i < pixelString.length() - 1; i++)
			pixels.add(Byte.valueOf(pixelString.substring(i, i + 1), 16));
		int revision = object.get("revision").getAsInt();
		return new Graffiti(paletteOut, pixels, revision);
	}
	
	public record Graffiti(List<Integer> palette, List<Byte> pixels, int revision) {}
	
	static {
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isPresent())
		{
			Path gameDir = FabricLoader.getInstance().getGameDir();
			cacheDir = Path.of(gameDir.toString(), CACHE_PATH);
			try
			{
				Files.createDirectories(cacheDir);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
			cacheDir = null;
	}
}
