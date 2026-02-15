package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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

import java.util.ArrayList;
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
                // 1. Calculate and retrieve the camo block's ModelData using the wrapped world view (CRITICAL for CTM)
                var camoData = camoModel.getModelData(wrappedView, pos, camo, ModelData.EMPTY);
                BlockState connectedState = getConnectedState(view, pos, camo);

                boolean wrenchNearby = isWrenchNearby(view, pos);

                // Store all necessary data
                return existing.derive()
                        .with(FacadeModelData.FACADE, connectedState)
                        .with(FacadeModelData.CAMO_MODEL_DATA, camoData)
                        .with(FacadeConnectionHelper.ROUTER_POS_PROPERTY, pos)
                        .with(FacadeModelData.WRENCH_NEARBY, wrenchNearby)
                        .build();
            }
        }
        return existing;
    }

    private boolean isWrenchNearby(BlockAndTintGetter level, BlockPos pos) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        if (!isHoldingApplicator(player)) return false;

        double distSq = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return distSq <= 144; // 12 blocks range
    }

    private boolean isHoldingApplicator(LocalPlayer player) {
        return player.getMainHandItem().is(ModItems.FACADE_APPLICATOR.get()) ||
                player.getOffhandItem().is(ModItems.FACADE_APPLICATOR.get());
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, ModelData data, @Nullable RenderType layer) {
        var camo = data.get(FacadeModelData.FACADE);
        var camoData = data.get(FacadeModelData.CAMO_MODEL_DATA);
        Boolean wrenchNearby = data.get(FacadeModelData.WRENCH_NEARBY);

        if (camo != null) {
            var model = lookup.apply(camo);
            // Pass the stored ModelData to the underlying model (CRITICAL for CTM/Fusion)
            var modelData = camoData != null ? camoData : ModelData.EMPTY;

            if (wrenchNearby != null && wrenchNearby) {
                if (layer == RenderType.translucent()) {
                    List<BakedQuad> quads = model.getQuads(camo, side, rand, modelData, layer);
                    if (quads.isEmpty()) {
                        // If it doesn't normally render in translucent, try getting its default quads
                        quads = model.getQuads(camo, side, rand, modelData, null);
                    }
                    if (quads.isEmpty()) return quads;

                    List<BakedQuad> translucentQuads = new ArrayList<>();
                    for (BakedQuad quad : quads) {
                        translucentQuads.add(withAlpha(quad, 0.5f));
                    }
                    return translucentQuads;
                }
                // Also render original router model so it's visible "under" the facade
                return super.getQuads(state, side, rand, data, layer);
            }

            // We rely on the underlying CTM/Fusion model to correctly suppress the quad,
            // now that both models are correctly wrapped and receiving spoofed data.
            return model.getQuads(camo, side, rand, modelData, layer);
        }
        return super.getQuads(state, side, rand, data, layer);
    }

    private BakedQuad withAlpha(BakedQuad quad, float alpha) {
        int[] vertices = quad.getVertices().clone();
        int alphaInt = (int) (alpha * 255) << 24;
        for (int i = 0; i < 4; i++) {
            int offset = i * 8 + 3; // 3 is the color offset in the default vertex format
            vertices[offset] = (vertices[offset] & 0x00FFFFFF) | alphaInt;
        }
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), quad.isTinted());
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, ModelData data) {
        var camo = data.get(FacadeModelData.FACADE);
        var camoData = data.get(FacadeModelData.CAMO_MODEL_DATA);
        Boolean wrenchNearby = data.get(FacadeModelData.WRENCH_NEARBY);

        if (camo != null) {
            var model = lookup.apply(camo);
            var modelData = camoData != null ? camoData : ModelData.EMPTY;

            if (wrenchNearby != null && wrenchNearby) {
                ChunkRenderTypeSet originalTypes = super.getRenderTypes(state, rand, data);
                return ChunkRenderTypeSet.union(originalTypes, ChunkRenderTypeSet.of(RenderType.translucent()));
            }
            
            return model.getRenderTypes(camo, rand, modelData);
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

    private static BakedModel bakedFor(BlockState s) {
        ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(s);
        return Minecraft.getInstance().getModelManager().getModel(mrl);
    }

    public static FacadeModelWrapper wrap(BakedModel original) {
        return new FacadeModelWrapper(original, FacadeModelWrapper::bakedFor);
    }
}
