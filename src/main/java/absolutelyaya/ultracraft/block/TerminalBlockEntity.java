package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.GraffitiCacheManager;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.*;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TerminalBlockEntity extends BlockEntity implements GeoBlockEntity
{
	//Persistent
	Base base = Base.YELLOW;
	UUID owner = null;
	List<String> lines = new ArrayList<>() {
		{
			add("+--------------+");
			add("   Tip of the Day");
			add("");
			add("     be there or");
			add("      be square");
			add("");
			add("");
			add("");
			add("");
			add("");
			add("+--------------+");
		}
	};
	int textColor = 0xffffffff;
	boolean locked = false;
	List<Integer> palette = new ArrayList<>() {
		{
			//0 is reserved for transparency
			add(0xffd9e7ff);
			add(0xffa5b2b9);
			add(0xff5d6569);
			add(0xff250c30);
			add(0xff78434d);
			add(0xffa80300);
			add(0xffd74b0f);
			add(0xffd7c00f);
			add(0xff77c62d);
			add(0xff1e8725);
			add(0xff2b73cb);
			add(0xff3129cb);
			add(0xff8f35cb);
			add(0xffcb0acb);
			add(0xffdf95da);
		}
	};
	List<Byte> graffiti = new ArrayList<>();
	UUID terminalID;
	//Don't save all this
	float displayVisibility = 0f, inactivity = 600f, caretTimer = 0f, graffitiCamRotation = 0f;
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	List<WingedPlayerEntity> focusedPlayers = new ArrayList<>();
	int colorOverride = -1, graffitiRevision = 0;
	Vector2d cursor = new Vector2d();
	Vector2i caret = new Vector2i();
	String lastHovered;
	Tab tab = Tab.MAIN_MENU;
	Vector2f normalWindowSize = new Vector2f(100f, 100f), curWindowSize = new Vector2f(normalWindowSize), sizeOverride = null;
	Identifier graffitiTexture;
	
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.TERMINAL, pos, state);
		terminalID = UUID.randomUUID();
	}
	
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
	
	}
	
	public void onHit()
	{
		inactivity = 0f;
		if(lastHovered != null)
		{
			String[] segments = lastHovered.split("@");
			String action = segments[0];
			int value = 0;
			if(segments.length > 1)
				value = Integer.parseInt(segments[1]);
			
			switch(action)
			{
				case "customize" -> setTab(Tab.CUSTOMIZATION);
				case "bestiary" -> setTab(Tab.COMING_SOON);
				case "weapons" -> setTab(Tab.WEAPONS);
				case "mainmenu" -> setTab(Tab.MAIN_MENU);
				case "edit-screensaver" -> setTab(Tab.EDIT_SCREENSAVER);
				case "edit-base" -> setTab(Tab.BASE_SELECT);
				case "graffiti" -> setTab(Tab.GRAFFITI);
				
				case "set-base" -> base = Base.values()[value];
				case "toggle-lock" -> setLocked(!locked);
				case "force-screensaver" -> setInactivity(60f);
				default -> Ultracraft.LOGGER.error("Undefined Behavior for Terminal button '" + lastHovered + "'");
			}
		}
	}
	
	void onBlockBreak()
	{
		for (WingedPlayerEntity p : focusedPlayers)
			p.setFocusedTerminal(null);
	}
	
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
	
	@Override
	public void readNbt(NbtCompound nbt)
	{
		super.readNbt(nbt);
		if(nbt.contains("base", NbtElement.INT_TYPE))
			base = Base.values()[nbt.getInt("base")];
		if(nbt.contains("txt-clr", NbtElement.INT_TYPE))
			textColor = nbt.getInt("txt-clr");
		if(nbt.containsUuid("owner"))
			owner = nbt.getUuid("owner");
		if(nbt.contains("locked", NbtElement.BYTE_TYPE))
			locked = nbt.getBoolean("locked");
		if(nbt.contains("screensaver", NbtElement.COMPOUND_TYPE))
		{
			NbtCompound screensaver = nbt.getCompound("screensaver");
			applyScreensaver(screensaver);
		}
		if(nbt.containsUuid("id"))
			terminalID = nbt.getUuid("id");
		if(world == null && nbt.contains("graffiti", NbtElement.COMPOUND_TYPE))
			applyGraffiti(nbt.getCompound("graffiti"));
		if(world != null && world.isClient)
		{
			GraffitiCacheManager.Graffiti g = GraffitiCacheManager.fetchGrafitti(terminalID);
			if(g != null)
			{
				setPalette(g.palette());
				setGraffiti(g.pixels());
				setGraffitiRevision(g.revision());
			}
			else if (nbt.contains("graffiti", NbtElement.COMPOUND_TYPE))
				applyGraffiti(nbt.getCompound("graffiti"));
			refreshGraffitiTexture();
		}
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("base", base.ordinal());
		nbt.putInt("txt-clr", textColor);
		if(owner != null)
			nbt.putUuid("owner", owner);
		nbt.putBoolean("locked", locked);
		nbt.put("screensaver", serializeScreensaver());
		nbt.putUuid("id", terminalID);
		nbt.put("graffiti", serializeGraffiti());
	}
	
	void applyScreensaver(NbtCompound screensaver)
	{
		for (int i = 0; i < 11; i++)
		{
			if(screensaver.contains("line" + i, NbtElement.STRING_TYPE))
				lines.set(i, screensaver.getString("line" + i));
			else
				lines.set(i, "");
		}
	}
	
	NbtCompound serializeScreensaver()
	{
		NbtCompound screensaver = new NbtCompound();
		for (int i = 0; i < lines.size(); i++)
			if(lines.get(i).length() > 0)
				screensaver.putString("line" + i, lines.get(i));
		return screensaver;
	}
	
	void applyGraffiti(NbtCompound nbt)
	{
		palette = Arrays.asList(ArrayUtils.toObject(nbt.getIntArray("palette")));
		String pixelString = nbt.getString("pixels");
		List<Byte> pixels = new ArrayList<>();
		for (int i = 0; i < pixelString.length() - 1; i++)
			pixels.add(Byte.valueOf(pixelString.substring(i, i + 1), 16));
		graffiti = pixels;
	}
	
	NbtCompound serializeGraffiti()
	{
		NbtCompound graffiti = new NbtCompound();
		graffiti.putIntArray("palette", ArrayUtils.toPrimitive(getPalette().toArray(new Integer[15])));
		graffiti.putString("pixels", GraffitiCacheManager.serializePixels(getGraffiti()));
		return graffiti;
	}
	
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket()
	{
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	@Override
	public NbtCompound toInitialChunkDataNbt()
	{
		return createNbt();
	}
	
	public void syncCustomization(int c, int base, boolean locked, NbtCompound screensaver)
	{
		textColor = c;
		if(base >= 0 && base < Base.values().length)
			this.base = Base.values()[base];
		this.locked = locked;
		applyScreensaver(screensaver);
		markDirty();
		world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
	}
	
	public float getRotation()
	{
		if(!getWorld().getBlockState(getPos()).isOf(BlockRegistry.TERMINAL_DISPLAY))
			return 0;
		return switch(getWorld().getBlockState(getPos()).get(TerminalDisplayBlock.FACING))
		{
			default -> 0;
			case EAST -> 90;
			case SOUTH -> 180;
			case WEST -> 270;
		};
	}
	
	public void setDisplayVisibility(float f)
	{
		displayVisibility = MathHelper.clamp(f, 0f, 1f);
		if(getWorld().getBlockState(getPos()).isOf(BlockRegistry.TERMINAL_DISPLAY))
			getWorld().setBlockState(getPos(), getWorld().getBlockState(getPos()).with(TerminalDisplayBlock.GLOWS,displayVisibility > 0.25f));
		if(displayVisibility == 0f && !tab.equals(Tab.MAIN_MENU))
			setTab(Tab.MAIN_MENU);
	}
	
	public float getDisplayVisibility()
	{
		return displayVisibility;
	}
	
	public List<String> getLines()
	{
		return lines;
	}
	
	public int getTextColor()
	{
		if(colorOverride != -1)
			return colorOverride;
		return textColor;
	}
	
	public void setTextColor(int i)
	{
		textColor = i;
	}
	
	public float getInactivity()
	{
		return inactivity;
	}
	
	public void setInactivity(float f)
	{
		if(!tab.equals(Tab.MAIN_MENU))
			f = 0f;
		inactivity = MathHelper.clamp(f, 0f, 600f);
		if(inactivity > 30f)
			lastHovered = null;
	}
	
	public boolean isFocused(WingedPlayerEntity p)
	{
		return focusedPlayers.contains(p);
	}
	
	public void unFocus(WingedPlayerEntity p)
	{
		focusedPlayers.remove(p);
	}
	
	public UUID getOwner()
	{
		return owner;
	}
	
	public void setCursor(Vector2d cursor)
	{
		float minX = -curWindowSize.x / 200f + 0.5f;
		this.cursor = new Vector2d(MathHelper.clamp(cursor.x, minX, minX + curWindowSize.x / 100f), MathHelper.clamp(cursor.y, 0, curWindowSize.y / 100f));
	}
	
	public Vector2d getCursor()
	{
		return cursor;
	}
	
	public String getLastHovered()
	{
		return lastHovered;
	}
	
	public void setLastHovered(String lastHovered)
	{
		this.lastHovered = lastHovered;
	}
	
	public void setTab(Tab tab)
	{
		if(this.tab.equals(Tab.CUSTOMIZATION) && tab.equals(Tab.MAIN_MENU))
		{
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBlockPos(pos);
			buf.writeInt(textColor);
			buf.writeInt(base.ordinal());
			buf.writeBoolean(locked);
			buf.writeNbt(serializeScreensaver());
			ClientPlayNetworking.send(PacketRegistry.TERMINAL_SYNC_C2S_PACKET_ID, buf);
			markDirty();
		}
		if(this.tab.equals(Tab.GRAFFITI) && !tab.equals(Tab.GRAFFITI))
		{
			Integer[] palette = getPalette().toArray(new Integer[15]);
			Byte[] pixels = getGraffiti().toArray(new Byte[0]);
			graffitiRevision++;
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeBlockPos(pos);
			buf.writeIntArray(ArrayUtils.toPrimitive(palette));
			buf.writeByteArray(ArrayUtils.toPrimitive(pixels));
			buf.writeInt(getGraffitiRevision());
			ClientPlayNetworking.send(PacketRegistry.GRAFFITI_C2S_PACKET_ID, buf);
			graffitiCamRotation = 0f;
			
			refreshGraffitiTexture();
			markDirty();
		}
		this.tab = tab;
		switch(tab)
		{
			case MAIN_MENU, CUSTOMIZATION ->
			{
				colorOverride = -1;
				sizeOverride = null;
			}
			case COMING_SOON -> colorOverride = 0xffffff00;
			case WEAPONS, BESTIARY -> sizeOverride = new Vector2f(200, 100);
			case EDIT_SCREENSAVER -> {
				caret = new Vector2i();
				sizeOverride = new Vector2f(200, 106);
			}
		}
	}
	
	public Tab getTab()
	{
		return tab;
	}
	
	public Vector2f getCurWindowSize()
	{
		return curWindowSize;
	}
	
	public void setCurWindowSize(Vector2f curWindowSize)
	{
		this.curWindowSize = curWindowSize;
	}
	
	public Vector2f getSizeOverride()
	{
		return sizeOverride;
	}
	
	public Vector2f getNormalWindowSize()
	{
		return normalWindowSize;
	}
	
	public Base getBase()
	{
		return base;
	}
	
	public void setLocked(boolean b)
	{
		locked = b;
	}
	
	public boolean isLocked()
	{
		return locked;
	}
	
	public Vector2i getCaret()
	{
		return caret;
	}
	
	public void setCaretTimer(float f)
	{
		caretTimer = f;
	}
	
	public float getCaretTimer()
	{
		return caretTimer;
	}
	
	public void setCaret(Vector2i v)
	{
		int y = v.y < 0 ? lines.size() - 1 : v.y % lines.size();
		int x;
		int len = lines.get(y).length();
		if(len == 0)
			x = 0;
		else
			x = v.x < 0 ? lines.get(y).length() : v.x % (lines.get(y).length() + 1);
		caret = new Vector2i(x, y);
		caretTimer = 0f;
	}
	
	public Vec3d getCamOffset()
	{
		if(tab.equals(Tab.GRAFFITI))
			return new Vec3d(0f, 0f, -2.5f).rotateY((getRotation() + graffitiCamRotation) * -MathHelper.RADIANS_PER_DEGREE);
		return new Vec3d(0f, 0f, -1f).rotateY(getRotation() * -MathHelper.RADIANS_PER_DEGREE);
	}
	
	public List<Byte> getGraffiti()
	{
		return graffiti;
	}
	
	public void setGraffiti(List<Byte> pixels)
	{
		graffiti = pixels;
	}
	
	public void refreshGraffitiTexture()
	{
		NativeImage image = new NativeImage(NativeImage.Format.RGBA, 32, 32, false);
		image.fillRect(0, 0, 32, 32, 0x00000000);
		for (int i = 0; i < 32 * 32; i++)
		{
			if(graffiti.size() <= i)
				break;
			int x = i % 32;
			int y = i / 32;
			int color = getPaletteColor(graffiti.get(i));
			int a = (color >> 24) & 0xff;
			int r = (color >> 16) & 0xff;
			int g = (color >> 8) & 0xff;
			int b = color & 0xff;
			color = (a << 8) + b;
			color = (color << 8) + g;
			color = (color << 8) + r;
			image.setColor(x, y, color); //ABGR
		}
		AbstractTexture texture = new NativeImageBackedTexture(image);
		graffitiTexture = new Identifier(Ultracraft.MOD_ID, "graffiti/" + terminalID.toString());
		//MinecraftClient.getInstance().getTextureManager().destroyTexture(graffitiTexture);
		MinecraftClient.getInstance().getTextureManager().registerTexture(graffitiTexture, texture);
		if(graffiti.size() > 0 && !GraffitiCacheManager.hasNewest(terminalID, graffitiRevision))
		{
			GraffitiCacheManager.cacheGraffiti(terminalID, palette, graffiti, graffitiRevision);
			GraffitiCacheManager.savePng(terminalID, image);
		}
		//image.close();
		//texture.close();
	}
	
	public void setPalette(List<Integer> colors)
	{
		palette = colors;
	}
	
	public void setPaletteColor(int idx, int color)
	{
		if(idx >= 0)
			palette.set(idx, color);
	}
	
	public List<Integer> getPalette()
	{
		return palette;
	}
	
	public int getPaletteColor(int i)
	{
		if(i == 0)
			return 0x0;
		return palette.get(i - 1);
	}
	
	public UUID getTerminalID()
	{
		return terminalID;
	}
	
	public int getGraffitiRevision()
	{
		return graffitiRevision;
	}
	
	public void setGraffitiRevision(int i)
	{
		graffitiRevision = i;
	}
	
	public Identifier getGraffitiTexture()
	{
		return graffitiTexture;
	}
	
	public boolean isOwner(UUID id)
	{
		if(owner == null)
			return true;
		return owner.equals(id);
	}
	
	public void rotateGrafittiCam(float f)
	{
		graffitiCamRotation += f;
	}
	
	public void setPixel(int x, int y, byte color)
	{
		int i = x + y * 32;
		while(graffiti.size() <= i)
			graffiti.add((byte)0);
		graffiti.set(i, color);
		refreshGraffitiTexture();
	}
	
	public enum Tab
	{
		MAIN_MENU,
		COMING_SOON,
		WEAPONS,
		BESTIARY,
		CUSTOMIZATION,
		BASE_SELECT,
		EDIT_SCREENSAVER,
		GRAFFITI
	}
	
	public enum Base
	{
		WHITE, LIGHT_GRAY, GRAY, BLACK, BROWN, RED, ORANGE, YELLOW,
		LIME, GREEN, CYAN, LIGHT_BLUE, BLUE, PURPLE, MAGENTA, PINK;
		
		public Identifier getTexture()
		{
			return new Identifier(Ultracraft.MOD_ID, String.format("textures/block/terminal/%s.png", name().toLowerCase()));
		}
		
		public String translationKey()
		{
			return "color.minecraft." + name().toLowerCase();
		}
	}
}
