package dev.gacbl.bblroutersfacade.network;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public final class FacadePayloads {
    public record FacadeRefresh(BlockPos pos, @Nullable BlockState state) implements CustomPacketPayload {

        public static final Type<FacadeRefresh> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RouterFacades.MOD_ID, "facade_refresh"));

        public static final StreamCodec<FriendlyByteBuf, FacadeRefresh> STREAM_CODEC =
                StreamCodec.of(
                        (buf, m) -> {
                            buf.writeBlockPos(m.pos());
                            buf.writeBoolean(m.state() != null);
                            if (m.state() != null) {
                                var tag = (CompoundTag) BlockState.CODEC.encodeStart(NbtOps.INSTANCE, m.state()).getOrThrow();
                                buf.writeNbt(tag);
                            }
                        },
                        buf -> {
                            var p = buf.readBlockPos();
                            BlockState st = null;
                            if (buf.readBoolean()) {
                                var t = buf.readNbt();
                                st = BlockState.CODEC.parse(NbtOps.INSTANCE, t).result().orElse(null);
                            }
                            return new FacadeRefresh(p, st);
                        }
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent e) {
        e.registrar(RouterFacades.MOD_ID).playToClient(FacadeRefresh.TYPE, FacadeRefresh.STREAM_CODEC, (msg, ctx) -> ClientHandlers.handleRefresh(msg));
    }
}
