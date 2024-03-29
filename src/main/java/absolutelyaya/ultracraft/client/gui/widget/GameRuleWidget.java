package absolutelyaya.ultracraft.client.gui.widget;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.accessor.WidgetAccessor;
import absolutelyaya.ultracraft.util.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.GameRules;
import org.joml.Vector2i;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameRuleWidget<T extends GameRules.Key<?>> extends ClickableWidget implements Element, Drawable, Selectable
{
	static final Identifier ICONS = new Identifier(Ultracraft.MOD_ID, "textures/gui/gamerule_icons.png");
	
	final NbtCompound rules;
	final T rule;
	final ValueType type;
	final int icon;
	TextRenderer renderer;
	String[] cycleValues;
	Drawable valueWidget;
	Identifier BGTexture;
	
	public GameRuleWidget(NbtCompound rules, Vector2i pos, T rule, ValueType type, int idx)
	{
		super(pos.x, pos.y + 38 * idx, 200, 36, Text.empty());
		this.rules = rules;
		this.rule = rule;
		this.type = type;
		renderer = MinecraftClient.getInstance().textRenderer;
		switch(type)
		{
			case BOOL -> valueWidget = new CheckboxWidget(getX() + 178, getY() + 14, 20, 20, Text.empty(), Boolean.parseBoolean(rules.getString(rule.getName())));
			case INT -> {
				valueWidget = new TextFieldWidget(renderer, getX() + 151, getY() + 15, 46, 18, Text.empty());
				((TextFieldWidget)valueWidget).setText(rules.getString(rule.getName()));
			}
		}
		this.icon = idx + 1;
		BGTexture = pickBG();
	}
	
	public GameRuleWidget(NbtCompound rules, Vector2i pos, T rule, String[] values, int idx)
	{
		super(pos.x, pos.y + 38 * idx, 200, 36, Text.empty());
		this.rules = rules;
		this.rule = rule;
		this.cycleValues = values;
		this.type = ValueType.CYCLE;
		renderer = MinecraftClient.getInstance().textRenderer;
		valueWidget = CyclingButtonWidget.builder(o -> Text.of((String)o)).values(values).initially(rules.getString(rule.getName()))
							  .omitKeyText().build(getX() + 130, getY() + 14, 68, 20, Text.empty());
		this.icon = idx + 1;
		BGTexture = pickBG();
	}
	
	public GameRuleWidget(NbtCompound rules, Vector2i pos, T rule, Enum<?>[] values, int idx)
	{
		super(pos.x, pos.y + 38 * idx, 200, 36, Text.empty());
		this.rules = rules;
		this.rule = rule;
		this.cycleValues = Arrays.stream(values).map(Enum::name).toArray(String[]::new);
		this.type = ValueType.CYCLE;
		renderer = MinecraftClient.getInstance().textRenderer;
		valueWidget = CyclingButtonWidget.builder(o -> Text.of((String)o)).values(cycleValues).initially(rules.getString(rule.getName()))
							  .omitKeyText().build(getX() + 130, getY() + 14, 68, 20, Text.empty());
		this.icon = idx + 1;
		BGTexture = pickBG();
	}
	
	Identifier pickBG()
	{
		int bg = new Random().nextInt(100);
		if(bg == 0)
			return new Identifier("textures/block/diamond_ore.png");
		else if(bg < 5)
			return new Identifier("textures/block/lapis_ore.png");
		else if(bg < 12)
			return new Identifier("textures/block/gold_ore.png");
		else if(bg < 25)
			return new Identifier("textures/block/iron_ore.png");
		else if(bg < 50)
			return new Identifier("textures/block/coal_ore.png");
		else
			return new Identifier("textures/block/stone.png");
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta, boolean simplistic)
	{
		alpha = MathHelper.clamp((getY() - 30) / 10f, 0f, 1f);
		RenderSystem.setShaderColor(0.69f, 0.69f, 0.69f, alpha);
		RenderSystem.setShaderTexture(0, simplistic ? new Identifier(Ultracraft.MOD_ID, "textures/gui/simplistic_bg.png") : BGTexture);
		MatrixStack matrices = context.getMatrices();
		RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(getX(), getY(), 200, 36), 0,
				new Vec2f(16, 16), new Vector4f(0f, 0, 100, 16), alpha);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		context.fill(getX(), getY(), getX() + 200, getY() + 1, 0xaaffffff);
		context.fill(getX(), getY(), getX() + 1, getY() + 36, 0xaaffffff);
		context.fill(getX(), getY() + 35, getX() + 200, getY() + 36, 0xaa000000);
		context.fill(getX() + 199, getY(), getX() + 200, getY() + 36, 0xaa000000);
		context.drawBorder(getX() + 2, getY() + 2, 32, 32, 0xffffffff);
		RenderSystem.setShaderTexture(0, ICONS);
		RenderingUtil.drawTexture(matrices.peek().getPositionMatrix(), new Vector4f(getX() + 2, getY() + 2, 32, 32), 1, new Vec2f(320, 320), new Vector4f(32 * (icon % 10), 32 * (int)Math.floor(icon / 10f) + 32, 32, -32), alpha);
		context.drawTextWithShadow(renderer, Text.translatable(rule.getTranslationKey()).getWithStyle(Style.EMPTY.withUnderline(true)).get(0),
				getX() + 36, getY() + 2, 0xffffffff);
		int controlWidth = switch(type)
		{
			case BOOL -> 22;
			case INT -> 52;
			case CYCLE -> 72;
		};
		Text fullText = Text.translatable(rule.getTranslationKey() + ".description");
		List<OrderedText> lines = renderer.wrapLines(fullText.getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)).get(0), 200 - 36 - controlWidth);
		for (int i = 0; i < Math.min(lines.size(), 2); i++)
		{
			OrderedText t;
			if(i == 0 || lines.size() < 3)
				t = lines.get(i);
			else
			{
				StringBuilder sb = new StringBuilder();
				lines.get(i).accept((a, b, c) -> {
					if(a == 0)
						sb.append(Formatting.byName(b.getColor().getName()));
					sb.appendCodePoint(c);
					return c > 0;
				});
				t = Text.of(sb + "...").asOrderedText();
				setTooltip(Tooltip.of(fullText));
			}
			drawOutlinedText(context, renderer, t, getX() + 36, getY() + 13 + renderer.fontHeight * i, alpha);
		}
		((WidgetAccessor)valueWidget).setOffset(((WidgetAccessor)this).getOffset());
		((WidgetAccessor) valueWidget).setAlpha(Math.max(alpha, 0.02f));
		valueWidget.render(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta)
	{
	
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean b = false;
		if(((Element)valueWidget).isMouseOver(mouseX, mouseY))
		{
			b = ((Element)valueWidget).mouseClicked(mouseX, mouseY, button);
			if(valueWidget instanceof TextFieldWidget textField)
				textField.setFocused(true);
		}
		else if(valueWidget instanceof TextFieldWidget textField && textField.isFocused())
			textField.setFocused(false);
		if(b)
		{
			String text = "";
			if(valueWidget instanceof CheckboxWidget checkbox)
				text = String.valueOf(checkbox.isChecked());
			else if(valueWidget instanceof CyclingButtonWidget<?> cycler)
				text = cycler.getValue().toString();
			if(text.length() > 0)
				setRule(text);
		}
		return b || super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		boolean b = ((Element)valueWidget).keyPressed(keyCode, scanCode, modifiers);
		if(valueWidget instanceof TextFieldWidget text && type.equals(ValueType.INT) && keyCode == 259)
			setRule(text.getText());
		return b;
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers)
	{
		boolean b = false;
		if(chr >= 48 && chr <= 57)
			b = ((Element)valueWidget).charTyped(chr, modifiers);
		if(b)
		{
			String text = ((TextFieldWidget)valueWidget).getText();
			if(text.length() > 0 && Integer.parseInt(text) > 0)
				setRule(text);
		}
		return b;
	}
	
	void setRule(String value)
	{
		if(value.length() == 0)
			return;
		rules.putString(rule.getName(), value);
		MinecraftClient.getInstance().player.networkHandler.sendChatCommand(String.format("gamerule %s %s", rule.getName(), value));
	}
	
	public void stateUpdate()
	{
		switch(type)
		{
			case BOOL -> {
				if(((CheckboxWidget)valueWidget).isChecked() != Boolean.parseBoolean(rules.getString(rule.getName())))
					((CheckboxWidget)valueWidget).onPress();
			}
			case INT -> ((TextFieldWidget)valueWidget).setText(rules.getString(rule.getName()));
			case CYCLE -> ((CyclingButtonWidget)valueWidget).setValue(rules.getString(rule.getName()));
		}
	}
	
	@Override
	public void setFocused(boolean focused)
	{
	
	}
	
	@Override
	public boolean isFocused()
	{
		return false;
	}
	
	@Override
	public SelectionType getType()
	{
		return SelectionType.NONE;
	}
	
	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder)
	{
	
	}
	
	void drawOutlinedText(DrawContext context, TextRenderer textRenderer, OrderedText text, int x, int y, float alpha)
	{
		RenderSystem.setShaderColor(0.1f, 0.1f, 0.1f, alpha);
		context.drawText(textRenderer, text, x - 1, y, 0xff444444, false);
		context.drawText(textRenderer, text, x + 1, y, 0xff444444, false);
		context.drawText(textRenderer, text, x, y + 1, 0xff444444, false);
		context.drawText(textRenderer, text, x, y - 1, 0xff444444, false);
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		
		context.drawText(textRenderer, text, x, y, 0xffffffff, false);
	}
	
	public enum ValueType
	{
		BOOL,
		INT,
		CYCLE
	}
}
