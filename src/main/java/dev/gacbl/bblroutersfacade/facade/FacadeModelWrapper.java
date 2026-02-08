package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class FacadeModelWrapper extends DelegateBlockStateModel {
    private final Function<BlockState, BlockStateModel> lookup;

    public FacadeModelWrapper(BlockStateModel original, Function<BlockState, BlockStateModel> lookup) {
        super(original);
        this.lookup = lookup;
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<BlockModelPart> parts) {
        var data = view.getModelData(pos);
        var camo = data.get(FacadeConnectionHelper.FACADE_STATE_PROPERTY);

        // Fallback to BE data if ModelData is empty (shouldn't happen with our wrapper, but good for safety)
        if (camo == null) {
            var be = view.getBlockEntity(pos);
            if (be != null) {
                camo = be.getData(FacadeAttachments.FACADE_STATE.get());
            }
        }

        if (camo != null) {
            BlockState connectedState = getConnectedState(view, pos, camo);
            var camoModel = lookup.apply(connectedState);
            var wrappedView = view instanceof FacadeLevelWrapper ? view : new FacadeLevelWrapper(view);

            List<BlockModelPart> camoParts = new ArrayList<>();
            camoModel.collectParts(wrappedView, pos, connectedState, random, camoParts);
            for (BlockModelPart part : camoParts) {
                parts.add(new FacadeModelPart(part, connectedState));
            }
            return;
        }
        super.collectParts(view, pos, state, random, parts);
    }

    private static class FacadeModelPart implements BlockModelPart {
        private final BlockModelPart parent;
        private final BlockState camoState;

        public FacadeModelPart(BlockModelPart parent, BlockState camoState) {
            this.parent = parent;
            this.camoState = camoState;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable Direction direction) {
            return parent.getQuads(direction);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return parent.useAmbientOcclusion();
        }

        @Override
        public TextureAtlasSprite particleIcon() {
            return parent.particleIcon();
        }

        @Override
        public ChunkSectionLayer getRenderType(BlockState state) {
            // Redirect to use the camo state instead of the host state (Router)
            return parent.getRenderType(camoState);
        }

        @Override
        public TriState ambientOcclusion() {
            return parent.ambientOcclusion();
        }
    }

    private BlockState getConnectedState(BlockAndTintGetter level, BlockPos pos, BlockState camoState) {
        Block block = camoState.getBlock();
        BlockState connectedState = camoState;

        if (block instanceof FenceBlock) {
            connectedState = updateFenceConnections(level, pos, connectedState);
        } else if (block instanceof IronBarsBlock) {
            connectedState = updatePaneConnections(level, pos, connectedState);
        } else if (block instanceof WallBlock) {
            connectedState = updateWallConnections(level, pos, connectedState);
        }
        return connectedState;
    }

    private BlockState updateFenceConnections(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        boolean north = canConnectToBlock(level, pos, Direction.NORTH, state.getBlock());
        boolean south = canConnectToBlock(level, pos, Direction.SOUTH, state.getBlock());
        boolean east = canConnectToBlock(level, pos, Direction.EAST, state.getBlock());
        boolean west = canConnectToBlock(level, pos, Direction.WEST, state.getBlock());

        return state
                .setValue(FenceBlock.NORTH, north)
                .setValue(FenceBlock.SOUTH, south)
                .setValue(FenceBlock.EAST, east)
                .setValue(FenceBlock.WEST, west);
    }

    private BlockState updatePaneConnections(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        boolean north = canConnectToBlock(level, pos, Direction.NORTH, state.getBlock());
        boolean south = canConnectToBlock(level, pos, Direction.SOUTH, state.getBlock());
        boolean east = canConnectToBlock(level, pos, Direction.EAST, state.getBlock());
        boolean west = canConnectToBlock(level, pos, Direction.WEST, state.getBlock());

        return state
                .setValue(IronBarsBlock.NORTH, north)
                .setValue(IronBarsBlock.SOUTH, south)
                .setValue(IronBarsBlock.EAST, east)
                .setValue(IronBarsBlock.WEST, west);
    }

    private BlockState updateWallConnections(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        boolean north = canConnectToBlock(level, pos, Direction.NORTH, state.getBlock());
        boolean south = canConnectToBlock(level, pos, Direction.SOUTH, state.getBlock());
        boolean east = canConnectToBlock(level, pos, Direction.EAST, state.getBlock());
        boolean west = canConnectToBlock(level, pos, Direction.WEST, state.getBlock());

        // Update wall connections
        for (var property : state.getProperties()) {
            if (property instanceof EnumProperty && property.getName().contains("north")) {
                String baseName = property.getName().replace("north", "");

                EnumProperty<WallSide> northProp = findWallProperty(state, baseName + "north");
                EnumProperty<WallSide> southProp = findWallProperty(state, baseName + "south");
                EnumProperty<WallSide> eastProp = findWallProperty(state, baseName + "east");
                EnumProperty<WallSide> westProp = findWallProperty(state, baseName + "west");

                if (northProp != null) state = state.setValue(northProp, north ? WallSide.LOW : WallSide.NONE);
                if (southProp != null) state = state.setValue(southProp, south ? WallSide.LOW : WallSide.NONE);
                if (eastProp != null) state = state.setValue(eastProp, east ? WallSide.LOW : WallSide.NONE);
                if (westProp != null) state = state.setValue(westProp, west ? WallSide.LOW : WallSide.NONE);
                break;
            }
        }
        return state;
    }

    private EnumProperty<WallSide> findWallProperty(BlockState state, String propertyName) {
        for (var property : state.getProperties()) {
            if (property instanceof EnumProperty && property.getName().equals(propertyName)) {
                //noinspection unchecked
                return (EnumProperty<WallSide>) property;
            }
        }
        return null;
    }

    private boolean canConnectToBlock(BlockAndTintGetter level, BlockPos pos, Direction direction, Block block) {
        return FacadeConnectionHelper.getConnectionState(level, pos, direction, block);
    }

    private static BlockStateModel bakedFor(BlockState s) {
        return Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(s);
    }

    public static FacadeModelWrapper wrap(BlockStateModel original) {
        return new FacadeModelWrapper(original, FacadeModelWrapper::bakedFor);
    }
}
