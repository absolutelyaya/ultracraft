package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.HellObserverBlockEntity;
import absolutelyaya.ultracraft.block.HellOperator;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class HellObserverScreen extends Screen
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/container/hell_observer.png");
	
	final BlockPos pos;
	int bgWidth = 179, bgHeight = 150, playerCount = 0, enemyCount = 0;
	int areaSizeX, areaSizeY, areaSizeZ, areaOffsetX, areaOffsetY, areaOffsetZ;
	boolean eyeOpen = true, requireBoth = false, editArea;
	float shake = 0f, mainScreenOffset = 0f, darken = 1f, cameraRot = 0f;
	Random random;
	HellOperator initialPlayerOperator, initialEnemyOperator;
	OperatorButton playerOpButton, enemyOpButton;
	Slider playerSlider, enemySlider;
	ButtonWidget interactionButton, editAreaButton, finishEditingButton;
	NumberField sizeFieldX, sizeFieldY, sizeFieldZ, offsetFieldX, offsetFieldY, offsetFieldZ;
	float signSpeed, signOffset = 50, areaPanelOffset = 120;
	List<Drawable> mainWidgets = new ArrayList<>(), areaPanelWidgets = new ArrayList<>();
	
	public HellObserverScreen(BlockPos pos)
	{
		super(Text.translatable("screen.ultracraft.hell_observer.title"));
		random = new Random();
		this.pos = pos;
		if(!(MinecraftClient.getInstance().player.getWorld().getBlockEntity(pos) instanceof HellObserverBlockEntity observer))
			return;
		playerCount = observer.getPlayerCount();
		initialPlayerOperator = observer.getPlayerOperator();
		enemyCount = observer.getEnemyCount();
		initialEnemyOperator = observer.getEnemyOperator();
		requireBoth = observer.getRequireBoth();
		Vec3i offset = observer.getCheckOffset();
		areaOffsetX = offset.getX();
		areaOffsetY = offset.getY();
		areaOffsetZ = offset.getZ();
		Vec3i size = observer.getCheckDimensions();
		areaSizeX = size.getX();
		areaSizeY = size.getY();
		areaSizeZ = size.getZ();
		eyeOpen = observer.shouldPreviewArea();
	}
	
	@Override
	protected void init()
	{
		super.init();
		int x = (width - bgWidth) / 2, y = (height - bgHeight) / 2;
		ButtonWidget eye = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
			if(eyeOpen)
				shake = 5f;
			eyeOpen = !eyeOpen;
		}).dimensions(x + 124, y + 8, 14, 10).build());
		playerOpButton = addDrawableChild(new OperatorButton(x + 140, y + 35, b -> {}));
		playerOpButton.operator = initialPlayerOperator;
		enemyOpButton = addDrawableChild(new OperatorButton(x + 140, y + 80, b -> {}));
		enemyOpButton.operator = initialEnemyOperator;
		playerSlider = addDrawableChild(new Slider(x + 12, x + 126, y + 37, s -> playerCount = (int)(s.percent * 64)));
		playerSlider.setPercent(playerCount / 64f);
		enemySlider = addDrawableChild(new Slider(x + 12, x + 126, y + 82, s -> enemyCount = (int)(s.percent * 64)));
		enemySlider.setPercent(enemyCount / 64f);
		interactionButton = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> requireBoth = !requireBoth)
													 .dimensions(x + 132, y + 58, 29, 11).build());
		interactionButton.setAlpha(0f);
		editAreaButton = addDrawableChild(ButtonWidget.builder(Text.translatable("screen.ultracraft.hell_observer.edit_area"), b -> setEditingArea(true))
													 .dimensions(x + 34, y + 99, 100, 20).build());
		eye.setAlpha(0f);
		//areaPanel
		if(!(MinecraftClient.getInstance().player.getWorld().getBlockEntity(pos) instanceof HellObserverBlockEntity observer))
			return;
		x = width - 115; y = (int)(height * 0.666f) - 50;
		sizeFieldX = addDrawableChild(new NumberField(textRenderer, x + 5, y + 16, 36, 14, Text.empty(), 32));
		sizeFieldX.setText(String.valueOf(areaSizeX));
		sizeFieldX.setChangedListener(s -> {
			areaSizeX = stringToInt(s);
			observer.setCheckDimensions(new Vec3i(areaSizeX, areaSizeY, areaSizeZ));
		});
		sizeFieldY = addDrawableChild(new NumberField(textRenderer, x + 41, y + 16, 36, 14, Text.empty(), 32));
		sizeFieldY.setText(String.valueOf(areaSizeY));
		sizeFieldY.setChangedListener(s -> {
			areaSizeY = stringToInt(s);
			observer.setCheckDimensions(new Vec3i(areaSizeX, areaSizeY, areaSizeZ));
		});
		sizeFieldZ = addDrawableChild(new NumberField(textRenderer, x + 77, y + 16, 36, 14, Text.empty(), 32));
		sizeFieldZ.setText(String.valueOf(areaSizeZ));
		sizeFieldZ.setChangedListener(s -> {
			areaSizeZ = stringToInt(s);
			observer.setCheckDimensions(new Vec3i(areaSizeX, areaSizeY, areaSizeZ));
		});
		offsetFieldX = addDrawableChild(new NumberField(textRenderer, x + 5, y + 45, 36, 14, Text.empty(), 16));
		offsetFieldX.setText(String.valueOf(areaOffsetX));
		offsetFieldX.setChangedListener(s -> {
			areaOffsetX = stringToInt(s);
			observer.setCheckOffset(new Vec3i(areaOffsetX, areaOffsetY, areaOffsetZ));
		});
		offsetFieldY = addDrawableChild(new NumberField(textRenderer, x + 41, y + 45, 36, 14, Text.empty(), 16));
		offsetFieldY.setText(String.valueOf(areaOffsetY));
		offsetFieldY.setChangedListener(s -> {
			areaOffsetY = stringToInt(s);
			observer.setCheckOffset(new Vec3i(areaOffsetX, areaOffsetY, areaOffsetZ));
		});
		offsetFieldZ = addDrawableChild(new NumberField(textRenderer, x + 77, y + 45, 36, 14, Text.empty(), 16));
		offsetFieldZ.setText(String.valueOf(areaOffsetZ));
		offsetFieldZ.setChangedListener(s -> {
			areaOffsetZ = stringToInt(s);
			observer.setCheckOffset(new Vec3i(areaOffsetX, areaOffsetY, areaOffsetZ));
		});
		offsetFieldX.setAllowNegative(true);
		offsetFieldY.setAllowNegative(true);
		offsetFieldZ.setAllowNegative(true);
		finishEditingButton = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> setEditingArea(false))
													   .dimensions(width / 2 - 83 / 2, 29, 83, 17).build());
		finishEditingButton.setAlpha(0f);
		setEditingArea(editArea);
		mainWidgets.clear();
		mainWidgets.addAll(List.of(interactionButton, editAreaButton, playerOpButton, enemyOpButton, playerSlider, enemySlider));
		areaPanelWidgets.clear();
		areaPanelWidgets.addAll(List.of(offsetFieldX, offsetFieldY, offsetFieldZ, sizeFieldX, sizeFieldY, sizeFieldZ, finishEditingButton));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		MatrixStack matrices = context.getMatrices();
		context.fill(0, 0, width, height, ColorHelper.Argb.getArgb((int)(darken * 127), 0, 0, 0));
		if(isEditingArea())
		{
			if(mainScreenOffset < height)
				mainScreenOffset = Math.min(mainScreenOffset + delta * 30, height);
			if(darken > 0f)
				darken = Math.max(darken - delta / 10f, 0f);
		}
		else
		{
			if(mainScreenOffset > 0)
				mainScreenOffset = Math.max(mainScreenOffset - delta * 30, 0);
			if(darken < 1f)
				darken = Math.min(darken + delta / 10f, 1f);
		}
		if(shake > 0)
		{
			matrices.translate((random.nextFloat() - 0.5f) * 2f * shake, 0, 0);
			shake -= delta;
		}
		int x = (width - bgWidth) / 2, y = (height - bgHeight) / 2;
		matrices.push();
		matrices.translate(x, y - mainScreenOffset, 0);
		if(eyeOpen)
		{
			int eyeOffsetX = MathHelper.clamp((mouseX - x - 125 * 2) / 30, -7, -1),
					eyeOffsetY = MathHelper.clamp((mouseY - y - 100) / 30, -5, -1);
			context.drawTexture(TEXTURE, 125 + eyeOffsetX, 7 + eyeOffsetY, 179, 61, 20, 18);
		}
		context.drawTexture(TEXTURE, -3, 0, 0, 0, bgWidth, bgHeight);
		if(!eyeOpen)
			context.drawTexture(TEXTURE, 120, 4, 179, 79, 25, 19);
		context.drawText(textRenderer, title, 12, 5, 0, false);
		context.drawText(textRenderer, Text.translatable("screen.ultracraft.hell_observer.players"), 21, 21, 0, false);
		context.drawText(textRenderer, Text.translatable("screen.ultracraft.hell_observer.enemies"), 21, 66, 0, false);
		String s = Text.translatable("screen.ultracraft.hell_observer." + (requireBoth ? "both" : "either")).getString();
		context.drawText(textRenderer, s, 147 - textRenderer.getWidth(s) / 2, 60, interactionButton.isHovered() ? 0xffffffff : 0, false);
		
		context.drawText(textRenderer, Text.of(String.valueOf(playerCount)), 156, 37, 0xffffffff, false);
		context.drawText(textRenderer, Text.of(String.valueOf(enemyCount)), 156, 82, 0xffffffff, false);
		matrices.pop();
		if(mainScreenOffset == 0)
			for (Drawable w : mainWidgets)
				w.render(context, mouseX, mouseY, delta);
		
		if(isEditingArea())
		{
			if(areaPanelOffset > 0)
				areaPanelOffset = Math.max(areaPanelOffset - delta * 10, 0);
			if(Math.abs(signSpeed) > 0f)
			{
				signOffset -= signSpeed * delta;
				if(signOffset <= 0)
					signSpeed = -signSpeed * 0.33f;
				signSpeed += 0.5f;
				signOffset = Math.max(signOffset, 0);
			}
		}
		else
		{
			if(areaPanelOffset < 120)
				areaPanelOffset = Math.min(areaPanelOffset + delta * 10, 120);
			if(signOffset < 50)
				signOffset = Math.min(signOffset + delta * 15, 50);
		}
		
		matrices.push();
		matrices.translate(width / 2f, -signOffset, 0);
		matrices.push();
		context.drawTexture(TEXTURE, -83 / 2 + 2, -3, 2, 150, 12, 33);
		matrices.pop();
		matrices.push();
		context.drawTexture(TEXTURE, 83 / 2 - 14, -3, 67, 150, 12, 34);
		matrices.pop();
		//sign
		matrices.push();
		context.drawTexture(TEXTURE, -83 / 2, 29, 0, 183, 83, 17);
		Text t = Text.translatable("screen.ultracraft.hell_observer.finish_editing");
		context.drawText(textRenderer, t, -textRenderer.getWidth(t) / 2, 32, finishEditingButton.isHovered() ? 0xffffffff : 0, false);
		matrices.pop();
		matrices.pop();
		//Area Panel
		matrices.push();
		matrices.translate(width - 115 + areaPanelOffset, height * 0.666f - 50, 0);
		context.drawTexture(TEXTURE, 0, -2, 84, 151, 116, 102);
		context.drawText(textRenderer, Text.translatable("screen.ultracraft.hell_observer.size"), 7, 6, 0, false);
		context.drawText(textRenderer, Text.translatable("screen.ultracraft.hell_observer.offset"), 7, 35, 0, false);
		context.drawText(textRenderer, Text.translatable("screen.ultracraft.hell_observer.move_hint"), 5, 64, 0, false);
		matrices.pop();
		matrices.push();
		matrices.translate(0f, 0.5f, 0f);
		if(areaPanelOffset == 0)
			for (Drawable w : areaPanelWidgets)
				w.render(context, mouseX, mouseY, delta);
		matrices.pop();
	}
	
	public BlockPos getObserverPos()
	{
		return pos;
	}
	
	void setEditingArea(boolean b)
	{
		editArea = b;
		interactionButton.visible = editAreaButton.visible = playerOpButton.visible = enemyOpButton.visible = playerSlider.visible = enemySlider.visible = !editArea;
		offsetFieldX.visible = offsetFieldY.visible = offsetFieldZ.visible = sizeFieldX.visible = sizeFieldY.visible = sizeFieldZ.visible =
																															   finishEditingButton.visible = editArea;
		signSpeed = 2f;
		MinecraftClient.getInstance().options.hudHidden = b;
	}
	
	public boolean isEditingArea()
	{
		return editArea;
	}
	
	public float getCamRot()
	{
		return cameraRot;
	}
	
	public int stringToInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException ignored)
		{
			return 0;
		}
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(editArea && (keyCode == 68 || keyCode == 65 || keyCode == 256))
		{
			switch (keyCode)
			{
				case 68 -> cameraRot += (modifiers == 2 ? -15 : -7.5f); //right
				case 65 -> cameraRot += (modifiers == 2 ? 15 : 7.5f); //left
				case 256 -> close(); // ESC
			}
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public void close()
	{
		setEditingArea(false);
		super.close();
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeInt(playerCount);
		buf.writeInt(playerOpButton.operator.ordinal());
		buf.writeInt(enemyCount);
		buf.writeInt(enemyOpButton.operator.ordinal());
		buf.writeBoolean(requireBoth);
		buf.writeVector3f(new Vector3f(areaOffsetX, areaOffsetY, areaOffsetZ));
		buf.writeVector3f(new Vector3f(areaSizeX, areaSizeY, areaSizeZ));
		buf.writeBoolean(eyeOpen);
		ClientPlayNetworking.send(PacketRegistry.HELL_OBSERVER_C2S_PACKET_ID, buf);
		if(!(MinecraftClient.getInstance().player.getWorld().getBlockEntity(pos) instanceof HellObserverBlockEntity observer))
			return;
		observer.sync(playerCount, playerOpButton.operator.ordinal(), enemyCount, enemyOpButton.operator.ordinal(), requireBoth,
				new Vec3i(areaOffsetX, areaOffsetY, areaOffsetZ), new Vec3i(areaSizeX, areaSizeY, areaSizeZ), eyeOpen);
	}
	
	static class OperatorButton extends ButtonWidget
	{
		HellOperator operator = HellOperator.IGNORE;
		
		protected OperatorButton(int x, int y, PressAction onPress)
		{
			super(x, y, 13, 13, Text.empty(), onPress, DEFAULT_NARRATION_SUPPLIER);
		}
		
		@Override
		public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
		{
			int u = operator.u + (isHovered() ? 13 : 0);
			context.drawTexture(HellObserverScreen.TEXTURE, getX(), getY(), u, operator.v, width, height);
			if(isHovered())
				context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.translatable(operator.translation), mouseX, mouseY);
		}
		
		@Override
		protected boolean clicked(double mouseX, double mouseY)
		{
			boolean b = super.clicked(mouseX, mouseY);
			if(b)
			{
				operator = switch(operator)
				{
					case IGNORE -> HellOperator.MORE;
					case MORE -> HellOperator.LESS;
					case LESS -> HellOperator.EQUALS;
					case EQUALS -> HellOperator.IGNORE;
				};
			}
			return b;
		}
		
		@Override
		protected MutableText getNarrationMessage()
		{
			return Text.translatable(operator.translation);
		}
		
		@Nullable
		@Override
		public Tooltip getTooltip()
		{
			return Tooltip.of(getNarrationMessage());
		}
	}
	
	static class Slider extends ButtonWidget
	{
		final Consumer<Slider> onValueChanged;
		final int minX, maxX;
		float x, percent;
		
		protected Slider(int minX, int maxX, int y, Consumer<Slider> onValueChanged)
		{
			super(minX, y, 6, 16, Text.empty(), b -> {}, DEFAULT_NARRATION_SUPPLIER);
			this.minX = minX;
			this.maxX = maxX;
			this.onValueChanged = onValueChanged;
		}
		
		@Override
		protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY)
		{
			if(isSelected())
			{
				x = (float)MathHelper.clamp(x + deltaX, minX, maxX);
				setX((int)x);
				percent = (x - minX) / (maxX - minX);
				onValueChanged.accept(this);
			}
			super.onDrag(mouseX, mouseY, deltaX, deltaY);
		}
		
		public void setPercent(float f)
		{
			percent = f;
			x = MathHelper.lerp(f, minX, maxX);
			setX((int)x);
			onValueChanged.accept(this);
		}
	}
	
	static class NumberField extends TextFieldWidget
	{
		boolean allowNegative = false;
		int max;
		
		public NumberField(TextRenderer textRenderer, int x, int y, int width, int height, Text text, int max)
		{
			super(textRenderer, x, y, width, height, text);
			this.max = max;
		}
		
		@Override
		public boolean charTyped(char chr, int modifiers)
		{
			if(!isValidChar(chr))
				return false;
			return super.charTyped(chr, modifiers);
		}
		
		boolean isValidChar(char c)
		{
			if(c == '-' && allowNegative)
				return true;
			return Character.isDigit(c);
		}
		
		public void setAllowNegative(boolean b)
		{
			allowNegative = b;
		}
		
		@Override
		public void write(String text)
		{
			super.write(text);
			try
			{
				int v = Integer.parseInt(getText());
				if(v > max)
					setText(String.valueOf(max));
				else if(!allowNegative && v < 0)
					setText("0");
				else if(v < -max)
					setText(String.valueOf(-max));
			}
			catch (NumberFormatException ignored)
			{
			
			}
		}
	}
}
