package dev.gacbl.bblroutersfacadeaddon.facade;

import dev.gacbl.bblroutersfacadeaddon.network.FacadePayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FacadeOps {
    public static void apply(Level level, BlockPos pos, ResourceLocation blockId) {
        if (blockId == null) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return;

        be.setData(FacadeAttachments.FACADE_ID.get(), blockId);
        triggerRefresh(level, pos);
    }

    public static void clear(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return;
        be.removeData(FacadeAttachments.FACADE_ID.get());
        triggerRefresh(level, pos);
    }

    public static void triggerRefresh(Level level, BlockPos pos) {
        if (level instanceof ServerLevel sl) {
            BlockState bs = sl.getBlockState(pos);
            sl.sendBlockUpdated(pos, bs, bs, 3);
            sl.getChunkSource().blockChanged(pos);
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(pos), new FacadePayloads.FacadeRefresh(pos));
        }
    }
}
