package absolutelyaya.ultracraft;

import absolutelyaya.ultracraft.accessor.WingedPlayerEntity;
import absolutelyaya.ultracraft.registry.*;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;

public class Ultracraft implements ModInitializer
{
    public static final String MOD_ID = "ultracraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static String VERSION;
    static int freezeTicks;
    
    @Override
    public void onInitialize()
    {
        ParticleRegistry.init();
        EntityRegistry.register();
        BlockRegistry.registerBlocks();
        FluidRegistry.register();
        BlockEntityRegistry.register();
        ItemRegistry.register();
        PacketRegistry.registerC2S();
        TagRegistry.register();
        SoundRegistry.register();
        GameruleRegistry.register();
        RecipeSerializers.register();
    
        ServerTickEvents.END_SERVER_TICK.register(minecraft -> tickFreeze());
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            ((WingedPlayerEntity)newPlayer).setWingsVisible(((WingedPlayerEntity)oldPlayer).isWingsActive());
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeUuid(newPlayer.getUuid());
            buf.writeBoolean(((WingedPlayerEntity)oldPlayer).isWingsActive());
            for (ServerPlayerEntity p : ((ServerWorld)newPlayer.world).getPlayers())
                ServerPlayNetworking.send(p, PacketRegistry.SET_HIGH_VELOCITY_S2C_PACKET_ID, buf);
        });
        
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((serverPlayer, lastWorld, newWorld) -> GameruleRegistry.SyncAll(serverPlayer));
        ServerPlayConnectionEvents.JOIN.register((networkHandler, sender, server) -> GameruleRegistry.SyncAll(networkHandler.player));
        
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent(modContainer -> VERSION = modContainer.getMetadata().getVersion().getFriendlyString());
        LOGGER.info("Ultracraft initialized.");
    }
    
    public static boolean isTimeFrozen()
    {
        return freezeTicks > 0;
    }
    
    public static void freeze(ServerWorld world, int ticks)
    {
        if(world != null)
        {
            if(world.getServer().isRemote() && world.getGameRules().get(GameruleRegistry.TIME_STOP).get().equals(GameruleRegistry.Option.FORCE_OFF))
                return;
            for (ServerPlayerEntity player : world.getPlayers())
            {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeInt(ticks);
                ServerPlayNetworking.send(player, PacketRegistry.FREEZE_PACKET_ID, buf);
            }
        }
        freezeTicks += ticks;
        LOGGER.info("Stopping time for " + ticks + " ticks.");
    }
    
    public static void tickFreeze()
    {
        if(freezeTicks > 0)
            freezeTicks--;
    }
}
