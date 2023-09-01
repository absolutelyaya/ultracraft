package absolutelyaya.ultracraft.block;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.BlockEntityRegistry;
import absolutelyaya.ultracraft.registry.BlockRegistry;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.*;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.InstancedAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TerminalBlockEntity extends BlockEntity implements GeoBlockEntity
{
	//Persistent
	Base base = Base.YELLOW;
	UUID owner = UUID.fromString("4a23954b-551c-4e2b-ac52-eb2e1ccbe443");
	List<String> lines = new ArrayList<>();
	int textColor = 0xffffffff;
	boolean locked = false;
	//Don't save all this
	float displayVisibility = 0f, inactivity = 600f;
	AnimatableInstanceCache cache = new InstancedAnimatableInstanceCache(this);
	List<WingedPlayerEntity> focusedPlayers = new ArrayList<>();
	int colorOverride = -1;
	Vector2d cursor = new Vector2d(0f, 0f);
	String lastHovered;
	Tab tab = Tab.MAIN_MENU;
	Vector2f normalWindowSize = new Vector2f(100f, 100f), curWindowSize = new Vector2f(normalWindowSize), sizeOverride = null;
	
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(BlockEntityRegistry.TERMINAL, pos, state);
		lines.add("+--------------+");
		lines.add("   Tip of the Day");
		lines.add("");
		lines.add("     be there or");
		lines.add("      be square");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("");
		lines.add("+--------------+");
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
				case "edit-screensaver" -> setTab(Tab.COMING_SOON);
				case "edit-base" -> setTab(Tab.BASE_SELECT);
				case "graffiti" -> setTab(Tab.COMING_SOON);
				
				case "set-base" -> base = Base.values()[value];
				case "toggle-lock" -> setLocked(!locked);
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
	}
	
	@Override
	protected void writeNbt(NbtCompound nbt)
	{
		super.writeNbt(nbt);
		nbt.putInt("base", base.ordinal());
		nbt.putInt("txt-clr", textColor);
		nbt.putUuid("owner", owner);
		nbt.putBoolean("locked", locked);
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
	
	public void syncCustomization(int c, int base, boolean locked)
	{
		textColor = c;
		if(base >= 0 && base < Base.values().length)
			this.base = Base.values()[base];
		this.locked = locked;
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
			ClientPlayNetworking.send(PacketRegistry.TERMINAL_SYNC_C2S_PACKET_ID, buf);
		}
		this.tab = tab;
		switch(tab)
		{
			case MAIN_MENU ->
			{
				colorOverride = -1;
				sizeOverride = null;
			}
			case COMING_SOON -> colorOverride = 0xffffff00;
			case WEAPONS, BESTIARY -> sizeOverride = new Vector2f(200, 100);
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
	
	public enum Tab
	{
		MAIN_MENU,
		COMING_SOON,
		WEAPONS,
		BESTIARY,
		CUSTOMIZATION,
		BASE_SELECT
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
