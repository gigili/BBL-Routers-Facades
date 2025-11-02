package dev.gacbl.bblroutersfacade.facade.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Compatibility handler for XyCraft blocks.
 *
 * XyCraft uses client-side IConnectedTextureHandler logic, so this server-side
 * handler is a no-op, relying on the client-side model wrapper to spoof the connection.
 * It exists purely to identify the block in the ModCompatibilityManager.
 */
public class XyCraftConnectionHandler implements IConnectionHandler {

    // Known XyCraft block class name (using reflection to avoid hard dependency)
    private static final String XYCRAFT_BLOCK_CLASS = "xyz.phanta.xycraft.block.BlockXy";

    @Override
    public boolean handles(BlockState state) {
        // Check if the block is an instance of the known XyCraft block class via reflection
        try {
            Class<?> xyCraftClass = Class.forName(XYCRAFT_BLOCK_CLASS);
            return xyCraftClass.isInstance(state.getBlock());
        } catch (ClassNotFoundException e) {
            // XyCraft is not loaded, so this handler is irrelevant
            return false;
        } catch (Exception e) {
            // General safety net
            return false;
        }
    }

    @Override
    public BlockState update(Level level, BlockPos neighborPos, BlockState state, Direction direction, boolean isConnected) {
        // NO-OP: XyCraft blocks rely entirely on client-side model data logic.
        return state;
    }
}
