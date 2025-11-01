package dev.gacbl.bblroutersfacadeaddon.network;

import dev.gacbl.bblroutersfacadeaddon.RouterFacades;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public final class FacadePayloads {
    public record FacadeRefresh(BlockPos pos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<FacadeRefresh> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RouterFacades.MOD_ID, "facade_refresh"));

        public static final StreamCodec<FriendlyByteBuf, FacadeRefresh> STREAM_CODEC =
                StreamCodec.composite(
                        BlockPos.STREAM_CODEC, FacadeRefresh::pos,
                        FacadeRefresh::new
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        event.registrar(RouterFacades.MOD_ID)
                .playToClient(FacadeRefresh.TYPE, FacadeRefresh.STREAM_CODEC,
                        (msg, ctx) -> ClientHandlers.handleRefresh(msg));
    }
}
