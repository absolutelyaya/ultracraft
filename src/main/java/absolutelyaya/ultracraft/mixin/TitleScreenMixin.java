package absolutelyaya.ultracraft.mixin;

import absolutelyaya.ultracraft.client.rendering.TitleBGRenderer;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin
{
    @Mutable
    @Shadow @Final private RotatingCubeMapRenderer backgroundRenderer;
    
    @Shadow @Final public static CubeMapRenderer PANORAMA_CUBE_MAP;
    
    @Inject(at = @At("TAIL"), method = "init()V")
    private void init(CallbackInfo info)
    {
        backgroundRenderer = new TitleBGRenderer(PANORAMA_CUBE_MAP);
    }
}
