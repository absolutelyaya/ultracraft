package absolutelyaya.ultracraft.client;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

public class ProceduralTextureManager
{
	public static NativeImage mapHSV(Identifier mapTexture, Identifier baseTexture, int col)
	{
		ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
		try (ResourceTexture.TextureData mapData = ResourceTexture.TextureData.load(manager, mapTexture))
		{
			NativeImage mapImg = mapData.getImage();
			try (ResourceTexture.TextureData baseData = ResourceTexture.TextureData.load(manager, baseTexture))
			{
				NativeImage baseImg = baseData.getImage();
				NativeImage out = new NativeImage(NativeImage.Format.RGBA, mapImg.getWidth(), mapImg.getHeight(), false);
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
		MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(img));
	}
}
