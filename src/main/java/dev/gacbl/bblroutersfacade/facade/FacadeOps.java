package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.network.FacadePayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FacadeOps {
    public static void apply(Level level, BlockPos pos, BlockState facade) {
        var be = level.getBlockEntity(pos);
        if (be == null || facade == null) return;
        be.setData(FacadeAttachments.FACADE_STATE.get(), facade);
        if (level instanceof ServerLevel sl) {
            var bs = sl.getBlockState(pos);
            sl.sendBlockUpdated(pos, bs, bs, 3);
            sl.getChunkSource().blockChanged(pos);
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(pos), new FacadePayloads.FacadeRefresh(pos, facade));
        }
    }

    public static void clear(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (be == null) return;
        be.removeData(FacadeAttachments.FACADE_STATE.get());
        if (level instanceof ServerLevel sl) {
            var bs = sl.getBlockState(pos);
            sl.sendBlockUpdated(pos, bs, bs, 3);
            sl.getChunkSource().blockChanged(pos);
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(pos), new FacadePayloads.FacadeRefresh(pos, null));
        }
    }

}
