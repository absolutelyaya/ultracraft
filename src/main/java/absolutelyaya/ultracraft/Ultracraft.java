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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;

public class Ultracraft implements ModInitializer
{
    public static final String MOD_ID = "ultracraft";
    public static final Logger LOGGER = LogUtils.getLogger();
    static int freezeTicks;
    
    @Override
    public void onInitialize()
    {
        ParticleRegistry.init();
        EntityRegistry.register();
        BlockRegistry.registerBlocks();
        BlockEntityRegistry.register();
        ItemRegistry.register();
        PacketRegistry.registerC2S();
        BlockTagRegistry.register();
        SoundRegistry.register();
        GameruleRegistry.register();
    
        ServerTickEvents.END_SERVER_TICK.register(minecraft -> tickFreeze());
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            ((WingedPlayerEntity)newPlayer).setWingsVisible(((WingedPlayerEntity)oldPlayer).isWingsVisible());
            for (ServerPlayerEntity p : ((ServerWorld)newPlayer.world).getPlayers())
            {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeUuid(newPlayer.getUuid());
                buf.writeBoolean(((WingedPlayerEntity)oldPlayer).isWingsVisible());
                ServerPlayNetworking.send(p, PacketRegistry.RESPAWN_PACKET_ID, buf);
            }
        });
        
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((serverPlayer, lastWorld, newWorld) -> GameruleRegistry.SyncAll(serverPlayer));
        ServerPlayConnectionEvents.JOIN.register((networkHandler, sender, server) -> GameruleRegistry.SyncAll(networkHandler.player));
        
        LOGGER.info("Ultracraft initialized.");
    }
    
    public static boolean isTimeFrozen()
    {
        return freezeTicks > 0;
    }
    
    public static void freeze(ServerWorld world, int ticks)
    {
        if(world.getGameRules().get(GameruleRegistry.TIME_STOP).get().equals(GameruleRegistry.Option.FORCE_OFF))
            return;
        for (ServerPlayerEntity player : world.getPlayers())
        {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(ticks);
            ServerPlayNetworking.send(player, PacketRegistry.FREEZE_PACKET_ID, buf);
        }
        freezeTicks += ticks;
        LOGGER.info("Stopping time for " + ticks + " ticks. (Intentional Visual Effect! Do not report!)");
    }
    
    public static void tickFreeze()
    {
        if(freezeTicks > 0)
            freezeTicks--;
    }
}
