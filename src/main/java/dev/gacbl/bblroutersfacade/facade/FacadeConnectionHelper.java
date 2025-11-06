package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FacadeConnectionHelper {

    /**
     * Check if a block should connect to a facade in the given direction
     */
    public static boolean shouldConnectToFacade(BlockGetter level, BlockPos pos, Direction direction, Block block) {
        BlockPos neighborPos = pos.relative(direction);
        var neighborBe = level.getBlockEntity(neighborPos);

        if (neighborBe != null) {
            BlockState neighborFacade = neighborBe.getData(FacadeAttachments.FACADE_STATE.get());
            if (neighborFacade != null) {
                Block facadeBlock = neighborFacade.getBlock();

                // 1. Check for exact block match (handles Chipped and most modded blocks)
                if (block == facadeBlock) {
                    return true;
                }

                // 2. Check for vanilla equivalent types
                if (block instanceof FenceBlock && facadeBlock instanceof FenceBlock) {
                    return true;
                }
                if (block instanceof IronBarsBlock && facadeBlock instanceof IronBarsBlock) {
                    return true;
                }
                if (block instanceof WallBlock && facadeBlock instanceof WallBlock) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get the connection state considering both real blocks and facades
     */
    public static boolean getConnectionState(BlockGetter level, BlockPos pos, Direction direction, Block block) {
        // First check vanilla connection
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        Block neighborBlock = neighborState.getBlock();

        boolean vanillaConnection = false;
        if (block instanceof FenceBlock) {
            vanillaConnection = neighborBlock instanceof FenceBlock ||
                    neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite());
        }
        else if (block instanceof IronBarsBlock) {
            vanillaConnection = neighborBlock instanceof IronBarsBlock ||
                    neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite());
        }
        else if (block instanceof WallBlock) {
            vanillaConnection = neighborBlock instanceof WallBlock ||
                    neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite());
        }

        // Also check facade connection
        boolean facadeConnection = shouldConnectToFacade(level, pos, direction, block);

        return vanillaConnection || facadeConnection;
    }
}
