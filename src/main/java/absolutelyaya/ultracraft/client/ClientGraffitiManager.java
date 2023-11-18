package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.TerminalBlockEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class ClientGraffitiManager
{
	static final String EXPORT_PATH = "Ultracraft/graffiti";
	static final Path cacheDir;
	
	public static Graffiti importGrafitti(String name)
	{
		try(Stream<Path> stream = Files.list(cacheDir))
		{
			for (Path f : stream.toList())
			{
				if(!Files.isDirectory(f) && f.getFileName().toString().split("\\.")[0].equals(name))
				{
					String json = Files.readString(f);
					return deserialize(JsonHelper.deserialize(json));
				}
			}
		}
		catch (IOException e)
		{
			Ultracraft.LOGGER.error("Failed to search Grafitti directory", e);
			return null;
		}
		return null;
	}
	
	public static void exportGraffiti(UUID id, List<Integer> palette, List<Byte> pixels, int revision)
	{
		JsonObject json = new JsonObject();
		JsonArray paletteProperty = new JsonArray();
		for (int color : palette)
			paletteProperty.add(color);
		json.add("palette", paletteProperty);
		//json.addProperty("pixels", serializePixels(pixels));
		json.addProperty("revision", revision);
		
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		Path gameDir = FabricLoader.getInstance().getGameDir();
		try
		{
			Files.writeString(Path.of(gameDir.toString(), EXPORT_PATH, id.toString() + ".json"), JsonHelper.toSortedString(json));
			Ultracraft.LOGGER.info("Successfully Exported Grafitti from Terminal {}", id);
		}
		catch (IllegalArgumentException | JsonParseException | IOException e)
		{
			Ultracraft.LOGGER.error("Failed to cache Grafitti from Terminal {}", id, e);
		}
	}
	
	public static void savePng(String name, NativeImage image)
	{
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isEmpty())
			return;
		Path gameDir = FabricLoader.getInstance().getGameDir();
		Path path = Path.of(gameDir.toString(), EXPORT_PATH, name + ".png");
		try
		{
			image.writeTo(path);
			MinecraftClient.getInstance().player.sendMessage(Text.translatable("screen.ultracraft.terminal.graffiti.export_success")
							.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toString()))));
		}
		catch (IllegalArgumentException | JsonParseException | IOException e)
		{
			Ultracraft.LOGGER.error("Failed to export Grafitti {}.png", name, e);
			MinecraftClient.getInstance().player.sendMessage(Text.translatable("screen.ultracraft.terminal.graffiti.export_fail"));
		}
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
	
	public static void refreshGraffitiTexture(TerminalBlockEntity terminal)
	{
		NativeImage image = makeGraffitiImage(terminal);
		AbstractTexture texture = new NativeImageBackedTexture(image);
		if(terminal.getGraffitiTexture() == null)
			terminal.setGraffitiTexture(new Identifier(Ultracraft.MOD_ID, "procedural/graffiti/" + terminal.getTerminalID().toString()));
		MinecraftClient.getInstance().getTextureManager().registerTexture(terminal.getGraffitiTexture(), texture);
	}
	
	public static void exportGraffitiPng(TerminalBlockEntity terminal, String name)
	{
		try(NativeImage image = makeGraffitiImage(terminal))
		{
			savePng(name, image);
		}
	}
	
	static NativeImage makeGraffitiImage(TerminalBlockEntity terminal)
	{
		NativeImage image = new NativeImage(NativeImage.Format.RGBA, 40, 40, false);
		image.fillRect(0, 0, 40, 40, 0x00000000);
		for (int i = 0; i < 40 * 40; i++)
		{
			if(terminal.getGraffiti().size() <= i)
				break;
			int x = i % 40;
			int y = i / 40;
			int color = terminal.getPaletteColor(terminal.getGraffiti().get(i));
			int a = (color >> 24) & 0xff;
			int r = (color >> 16) & 0xff;
			int g = (color >> 8) & 0xff;
			int b = color & 0xff;
			color = (a << 8) + b;
			color = (color << 8) + g;
			color = (color << 8) + r;
			image.setColor(x, y, color); //ABGR
		}
		return image;
	}
	
	public record Graffiti(List<Integer> palette, List<Byte> pixels, int revision) {}
	
	static {
		Optional<ModContainer> optionalContainer = FabricLoader.getInstance().getModContainer(Ultracraft.MOD_ID);
		if(optionalContainer.isPresent())
		{
			Path gameDir = FabricLoader.getInstance().getGameDir();
			cacheDir = Path.of(gameDir.toString(), EXPORT_PATH);
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
