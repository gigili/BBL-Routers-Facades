package dev.gacbl.bblroutersfacade.facade.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages and delegates server-side block state updates to registered compatibility handlers.
 */
public class ModCompatibilityManager {
    private static final ModCompatibilityManager INSTANCE = new ModCompatibilityManager();
    private final List<IConnectionHandler> handlers = new ArrayList<>();

    private ModCompatibilityManager() {
        // Register handlers in order of specificity (more specific first)
        handlers.add(new XyCraftConnectionHandler());
        handlers.add(new StandardBooleanHandler()); // Must be the last fallback handler
    }

    public static ModCompatibilityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Finds the correct handler and attempts to update the neighbor block's state properties.
     * @param level The current level.
     * @param neighborPos The position of the block being updated.
     * @param state The current state of the block being updated.
     * @param direction The direction from the block being updated to the facade.
     * @param isConnected The desired connection status (true if the facade is present).
     * @return The updated BlockState, or the original state if no change occurred.
     */
    public BlockState handleUpdate(Level level, BlockPos neighborPos, BlockState state, Direction direction, boolean isConnected) {
        for (IConnectionHandler handler : handlers) {
            if (handler.handles(state)) {
                return handler.update(level, neighborPos, state, direction, isConnected);
            }
        }

        // Fallback to the StandardBooleanHandler's update method directly for non-explicitly handled blocks
        // that might use standard properties without being Fences or IronBars.
        return handlers.stream()
                .filter(h -> h instanceof StandardBooleanHandler)
                .findFirst()
                .map(h -> h.update(level, neighborPos, state, direction, isConnected))
                .orElse(state);
    }
}
