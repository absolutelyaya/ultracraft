package absolutelyaya.ultracraft.client.gui.screen;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.block.HellObserverBlockEntity;
import absolutelyaya.ultracraft.block.HellOperator;
import absolutelyaya.ultracraft.registry.PacketRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Consumer;

public class HellObserverScreen extends Screen
{
	static final Identifier TEXTURE = new Identifier(Ultracraft.MOD_ID, "textures/gui/container/hell_observer.png");
	
	final BlockPos pos;
	int bgWidth = 179, bgHeight = 155, playerCount = 0, enemyCount = 0;
	boolean eyeOpen = true;
	float shake = 0f;
	Random random;
	HellOperator initialPlayerOperator, initialEnemyOperator;
	OperatorButton playerOpButton, enemyOpButton;
	Slider playerSlider, enemySlider;
	
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
	}
	
	@Override
	protected void init()
	{
		super.init();
		int x = (width - bgWidth) / 2, y = (height - bgHeight) / 2;
		ButtonWidget eye = addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
			if(eyeOpen)
			{
				shake = 5f;
				eyeOpen = false;
			}
		}).dimensions(x + 124, y + 8, 14, 10).build());
		playerOpButton = addDrawableChild(new OperatorButton(x + 140, y + 35, b -> {}));
		playerOpButton.operator = initialPlayerOperator;
		enemyOpButton = addDrawableChild(new OperatorButton(x + 140, y + 80, b -> {}));
		enemyOpButton.operator = initialEnemyOperator;
		playerSlider = addDrawableChild(new Slider(x + 12, x + 126, y + 37, s -> playerCount = (int)(s.percent * 64)));
		playerSlider.setPercent(playerCount / 64f);
		enemySlider = addDrawableChild(new Slider(x + 12, x + 126, y + 82, s -> enemyCount = (int)(s.percent * 64)));
		enemySlider.setPercent(enemyCount / 64f);
		eye.setAlpha(0f);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		if(shake > 0)
		{
			context.getMatrices().translate((random.nextFloat() - 0.5f) * 2f * shake, 0, 0);
			shake -= delta;
		}
		int x = (width - bgWidth) / 2, y = (height - bgHeight) / 2;
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
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
		
		context.drawText(textRenderer, Text.of(String.valueOf(playerCount)), 156, 37, 0xffffffff, false);
		context.drawText(textRenderer, Text.of(String.valueOf(enemyCount)), 156, 82, 0xffffffff, false);
		context.getMatrices().pop();
		super.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public void close()
	{
		super.close();
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeBlockPos(pos);
		buf.writeInt(playerCount);
		buf.writeInt(playerOpButton.operator.ordinal());
		buf.writeInt(enemyCount);
		buf.writeInt(enemyOpButton.operator.ordinal());
		ClientPlayNetworking.send(PacketRegistry.HELL_OBSERVER_C2S_PACKET_ID, buf);
		if(!(MinecraftClient.getInstance().player.getWorld().getBlockEntity(pos) instanceof HellObserverBlockEntity observer))
			return;
		observer.sync(playerCount, playerOpButton.operator.ordinal(), enemyCount, enemyOpButton.operator.ordinal());
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
}
