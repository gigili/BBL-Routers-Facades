package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class FacadeModelWrapper extends BakedModelWrapper<BakedModel> {
    private final Function<BlockState, BakedModel> lookup;

    public FacadeModelWrapper(BakedModel original, Function<BlockState, BakedModel> lookup) {
        super(original);
        this.lookup = lookup;
    }

    @Override
    public @NotNull ModelData getModelData(BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData existing) {
        var be = view.getBlockEntity(pos);
        if (be != null) {
            var camo = be.getData(FacadeAttachments.FACADE_STATE.get());
            if (camo != null) {
                var camoModel = lookup.apply(camo);
                var wrappedView = new FacadeLevelWrapper(view);

                // Get the ModelData the camo block would have (this is what neighbors need)
                var camoData = camoModel.getModelData(wrappedView, pos, camo, ModelData.EMPTY);

                // Get the state for vanilla connections
                BlockState connectedState = getConnectedState(view, pos, camo);

                // Start with the camoData, THEN add our own property for rendering
                return camoData.derive()
                        .with(FacadeModelData.FACADE, connectedState)
                        .build();
            }
        }
        return existing;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, ModelData data, @Nullable RenderType layer) {
        var camo = data.get(FacadeModelData.FACADE);

        if (camo != null) {
            var model = lookup.apply(camo);
            // Pass the *entire* model data, which includes Athena's CTM data
            return model.getQuads(camo, side, rand, data, layer);
        }
        return super.getQuads(state, side, rand, data, layer);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, ModelData data) {
        var camo = data.get(FacadeModelData.FACADE);

        if (camo != null) {
            var model = lookup.apply(camo);
            // Pass the *entire* model data, which includes Athena's CTM data
            return model.getRenderTypes(camo, rand, data);
        }
        return super.getRenderTypes(state, rand, data);
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

    private static BakedModel bakedFor(BlockState s) {
        ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(s);
        return Minecraft.getInstance().getModelManager().getModel(mrl);
    }

    public static FacadeModelWrapper wrap(BakedModel original) {
        return new FacadeModelWrapper(original, FacadeModelWrapper::bakedFor);
    }
}
