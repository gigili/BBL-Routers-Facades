package dev.gacbl.bblroutersfacade.facade.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * Handles blocks that use the standard directional BooleanProperties (north, south, east, west)
 * for connections, like vanilla Fences and Iron Bars, and acts as a generic fallback.
 */
public class StandardBooleanHandler implements IConnectionHandler {

    @Override
    public boolean handles(BlockState state) {
        // Explicitly handle vanilla blocks covered by this logic.
        Block block = state.getBlock();
        return block instanceof FenceBlock || block instanceof IronBarsBlock;
        // All other blocks fall through to the ModCompatibilityManager's generic update logic.
    }

    @Override
    public BlockState update(Level level, BlockPos neighborPos, BlockState state, Direction direction, boolean isConnected) {
        String propName = direction.getName().toLowerCase();

        // Try the standard vanilla directional boolean property (e.g., 'north')
        BlockState newState = updateGenericBooleanProperty(state, propName, isConnected);

        if (newState != state) {
            // Property found and updated. We need to persist the change.
            if (!newState.equals(state)) {
                level.setBlock(neighborPos, newState, 3);
            }
            return newState;
        }

        // If the standard property fails, try common modded names as a last resort
        newState = updateGenericBooleanProperty(state, "c_" + propName, isConnected);
        if (newState != state) {
            if (!newState.equals(state)) {
                level.setBlock(neighborPos, newState, 3);
            }
            return newState;
        }

        newState = updateGenericBooleanProperty(state, "connected_" + propName, isConnected);
        if (newState != state) {
            if (!newState.equals(state)) {
                level.setBlock(neighborPos, newState, 3);
            }
            return newState;
        }

        return state;
    }

    /**
     * Utility method to find and set a BooleanProperty, failing gracefully if not found.
     */
    private BlockState updateGenericBooleanProperty(BlockState state, String propName, boolean value) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty && property.getName().equals(propName)) {
                @SuppressWarnings("unchecked")
                BooleanProperty boolProp = (BooleanProperty) property;
                try {
                    if (state.getValue(boolProp) != value) {
                        return state.setValue(boolProp, value);
                    }
                    return state; // Value already set correctly
                } catch (IllegalArgumentException e) {
                    // Property is present but cannot accept the value for some reason
                    return state;
                }
            }
        }
        return state; // Property not found
    }
}
