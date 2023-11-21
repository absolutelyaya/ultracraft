package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProceduralTextureManager
{
	static final Map<Identifier, NativeImageBackedTexture> textures = new HashMap<>();
	
	public static NativeImage mapHSV(Identifier mapTexture, Identifier baseTexture, int col)
	{
		ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
		try (ResourceTexture.TextureData mapData = ResourceTexture.TextureData.load(manager, mapTexture))
		{
			NativeImage mapImg = mapData.getImage();
			if(mapImg == null)
				return null;
			try (ResourceTexture.TextureData baseData = ResourceTexture.TextureData.load(manager, baseTexture))
			{
				NativeImage baseImg = baseData.getImage();
				if(baseImg == null)
					return null;
				NativeImage out = new NativeImage(NativeImage.Format.RGBA, mapImg.getWidth(), mapImg.getHeight(), false);
				if(out == null)
					return null;
				for (int x = 0; x < mapImg.getWidth(); x++)
				{
					for (int y = 0; y < mapImg.getHeight(); y++)
					{
						if(mapImg.getOpacity(x, y) == 0)
						{
							out.setColor(x, y, baseImg.getColor(x, y));
							continue;
						}
						out.setColor(x, y, ColorUtil.mapHSV(col, mapImg.getColor(x, y)));
					}
				}
				return out;
			}
			catch (IOException exception)
			{
				Ultracraft.LOGGER.error("Failed to load Base Texture! (id: " + baseTexture + ")");
				exception.printStackTrace();
				return null;
			}
		}
		catch (IOException exception)
		{
			Ultracraft.LOGGER.error("Failed to load HSV-Map Texture! (id: " + mapTexture + ")");
			exception.printStackTrace();
			return null;
		}
	}
	
	public static void createHsvMappedTexture(Identifier mapTexture, Identifier baseTexture, Identifier id, int col)
	{
		NativeImage img = mapHSV(mapTexture, baseTexture, col);
		if(img == null)
			return;
		NativeImageBackedTexture tex = new NativeImageBackedTexture(img);
		textures.put(id, tex);
		MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
	}
	
	static void printAll()
	{
		System.out.println("all textures (" + textures.size() + "): ");
		textures.keySet().forEach(key -> System.out.println(key.toString() + " -> " + textures.get(key).getImage()));
	}
	
	public static void deleteAll()
	{
		textures.keySet().forEach(key -> MinecraftClient.getInstance().getTextureManager().destroyTexture(key));
		textures.clear();
	}
}
