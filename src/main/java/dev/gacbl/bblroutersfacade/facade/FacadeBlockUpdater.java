package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import dev.gacbl.bblroutersfacade.facade.compat.ModCompatibilityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public class FacadeBlockUpdater {

    private static final ModCompatibilityManager COMPAT_MANAGER = ModCompatibilityManager.getInstance();

    @SubscribeEvent
    public static void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();

        updateBlockConnections((ServerLevel) level, pos, state, block);
    }

    private static void updateBlockConnections(ServerLevel level, BlockPos pos, BlockState currentState, Block block) {
        BlockState newState = currentState;

        // Iterate through all horizontal directions
        for (Direction direction : Direction.Plane.HORIZONTAL) {

            // Check if the current block at this position should connect to a facade neighbor.
            boolean shouldConnectToFacade = FacadeConnectionHelper.shouldConnectToFacade(level, pos, direction, block);

            if (block instanceof WallBlock) {
                // 1. Handle WallBlock (unique WallSide property)
                Property<WallSide> wallSideProperty = getHorizontalWallProperty(direction);

                if (shouldConnectToFacade) {
                    // If connecting, set to LOW or retain TALL if it was already TALL.
                    WallSide existingSide = newState.getValue(wallSideProperty);
                    newState = newState.setValue(wallSideProperty, existingSide == WallSide.TALL ? WallSide.TALL : WallSide.LOW);
                } else {
                    // If NOT connecting to a facade, check vanilla connection again
                    BlockPos neighborPos = pos.relative(direction);
                    BlockState neighborState = level.getBlockState(neighborPos);

                    boolean isConnectedToVanilla = neighborState.getBlock() instanceof WallBlock ||
                            neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite());

                    if (!isConnectedToVanilla) {
                        newState = newState.setValue(wallSideProperty, WallSide.NONE);
                    }
                }
            } else {
                // 2. Handle Fence, IronBars, and ALL Modded Blocks (Delegated to Compatibility Manager)
                // The manager handles updating the level and returning the final state.
                newState = COMPAT_MANAGER.handleUpdate(level, pos, newState, direction, shouldConnectToFacade);
            }
        }

        // Manually check/set UP connection for WallBlock
        if (block instanceof WallBlock) {
            boolean shouldConnectUp = FacadeConnectionHelper.shouldConnectToFacade(level, pos, Direction.UP, block);
            if (shouldConnectUp) {
                newState = newState.setValue(WallBlock.UP, true);
            }
        }

        // Only apply WallBlock state changes here, as the Manager handles non-Wall blocks internally.
        if (block instanceof WallBlock && !newState.equals(currentState)) {
            level.setBlock(pos, newState, 3);
        }
    }

    private static Property<WallSide> getHorizontalWallProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> WallBlock.NORTH_WALL;
            case SOUTH -> WallBlock.SOUTH_WALL;
            case EAST -> WallBlock.EAST_WALL;
            case WEST -> WallBlock.WEST_WALL;
            default -> throw new IllegalArgumentException("Invalid horizontal direction: " + direction);
        };
    }
}
