package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.network.FacadePayloads;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4), new FacadePayloads.FacadeRefresh(pos, facade));

            // Force model data update
            be.requestModelDataUpdate();

            // Update neighbors for connections - includes REAL blocks and adjacent facades
            updateNearbyBlocks(sl, pos);
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
            PacketDistributor.sendToPlayersTrackingChunk(sl, new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4), new FacadePayloads.FacadeRefresh(pos, null));

            // Force model data update
            be.requestModelDataUpdate();

            // Update neighbors for connections - includes REAL blocks and adjacent facades
            updateNearbyBlocks(sl, pos);
        }
    }

    /**
     * Forces direct neighbors (vanilla blocks) to re-evaluate their block state, and
     * adjacent facade blocks to update their model data (client-side re-render).
     */
    private static void updateNearbyBlocks(ServerLevel level, BlockPos centerPos) {
        BlockState centerState = level.getBlockState(centerPos);
        BlockEntity centerBe = level.getBlockEntity(centerPos);
        BlockState facadeState = centerBe != null ? centerBe.getData(FacadeAttachments.FACADE_STATE.get()) : null;

        // Use facade state if present, otherwise use real block state (for clearing)
        BlockState effectiveState = facadeState != null ? facadeState : centerState;

        // 1. Force state recalculation for direct neighbors (vanilla blocks like fences/walls)
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = centerPos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Trigger updateShape on the neighbor
            BlockState updatedState = neighborState.updateShape(level, level, neighborPos, direction.getOpposite(), centerPos, effectiveState, level.getRandom());
            if (updatedState != neighborState) {
                level.setBlock(neighborPos, updatedState, 3);
            }

            // Notify neighbors of the neighbor as well, to ensure everything updates
            level.neighborChanged(neighborPos, effectiveState.getBlock(), null);
        }

        // 2. Force client-side re-render/model data update for a 3x3x3 area, mainly targeting adjacent facades.
        // This ensures adjacent facades also see the change and re-render their model.
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos updatePos = centerPos.offset(x, y, z);

                    // If it's a block entity (like an adjacent router/facade host), request model data update
                    var be = level.getBlockEntity(updatePos);
                    if (be != null) {
                        be.requestModelDataUpdate();

                        // Send block update packet for client re-render
                        BlockState currentState = level.getBlockState(updatePos);
                        // Flag 2 is used to send block entity update to client
                        level.sendBlockUpdated(updatePos, currentState, currentState, 2);
                    }
                }
            }
        }
    }
}
