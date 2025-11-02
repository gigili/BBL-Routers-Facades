package dev.gacbl.bblroutersfacade.facade.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Defines the contract for custom block connection handlers to manage server-side
 * block state updates when a facade neighbor changes.
 */
public interface IConnectionHandler {
    /**
     * Checks if this handler is responsible for managing the given block state.
     * @param state The block state of the neighbor block being updated.
     * @return true if this handler should be used.
     */
    boolean handles(BlockState state);

    /**
     * Attempts to update the neighbor block's state properties (e.g., north, south)
     * based on whether a facade connection exists.
     * @param level The current level.
     * @param neighborPos The position of the block being updated.
     * @param state The current state of the block being updated.
     * @param direction The direction from the block being updated to the facade.
     * @param isConnected The desired connection status (true if the facade is present).
     * @return The updated BlockState, or the original state if no change occurred.
     */
    BlockState update(Level level, BlockPos neighborPos, BlockState state, Direction direction, boolean isConnected);
}
