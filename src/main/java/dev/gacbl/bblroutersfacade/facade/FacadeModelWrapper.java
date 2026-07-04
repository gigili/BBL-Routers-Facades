package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class FacadeModelWrapper extends DelegateBlockStateModel {
    private final Function<BlockState, BlockStateModel> lookup;
    public static final int RENDER_RANGE = 12;
    public static final int RENDER_RANGE_SQ = RENDER_RANGE * RENDER_RANGE;

    public FacadeModelWrapper(BlockStateModel original, Function<BlockState, BlockStateModel> lookup) {
        super(original);
        this.lookup = lookup;
    }

    @Override
    public void collectParts(@NotNull BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random, @NotNull List<BlockStateModelPart> parts) {
        BlockState camo = getCamo(view, pos);

        if (camo != null) {
            Player player = Minecraft.getInstance().player;
            boolean wrench = player != null && (isHoldingWrench(player.getMainHandItem()) || isHoldingWrench(player.getOffhandItem()));
            boolean nearby = wrench && player.blockPosition().distSqr(pos) < RENDER_RANGE_SQ;

            BlockState connectedState = getConnectedState(view, pos, camo);
            var camoModel = lookup.apply(connectedState);
            var wrappedView = view instanceof FacadeLevelWrapper ? view : new FacadeLevelWrapper(view);

            List<BlockStateModelPart> camoParts = new ArrayList<>();
            camoModel.collectParts(wrappedView, pos, connectedState, random, camoParts);
            for (BlockStateModelPart part : camoParts) {
                parts.add(new FacadeModelPart(part, connectedState, nearby));
            }

            if (nearby) {
                super.collectParts(view, pos, state, random, parts);
            }
            return;
        }
        super.collectParts(view, pos, state, random, parts);
    }

    @Override
    public @Nullable Object createGeometryKey(@NotNull BlockAndTintGetter view, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull RandomSource random) {
        BlockState camo = getCamo(view, pos);
        if (camo != null) {
            BlockState connectedState = getConnectedState(view, pos, camo);
            var camoModel = lookup.apply(connectedState);
            return camoModel.createGeometryKey(view, pos, connectedState, random);
        }
        return delegate.createGeometryKey(view, pos, state, random);
    }

    private BlockState getCamo(@NotNull BlockAndTintGetter view, @NotNull BlockPos pos) {
        var data = view.getModelData(pos);
        var camo = data.get(FacadeConnectionHelper.FACADE_STATE_PROPERTY);
        if (camo == null) {
            var be = view.getBlockEntity(pos);
            if (be != null) {
                camo = be.getData(FacadeAttachments.FACADE_STATE.get());
            }
        }
        return camo;
    }

    private static boolean isHoldingWrench(ItemStack stack) {
        return stack.is(ModItems.FACADE_APPLICATOR.get());
    }

    private static class FacadeModelPart implements BlockStateModelPart {
        private final BlockStateModelPart parent;
        private final BlockState camoState;
        private final boolean translucent;

        public FacadeModelPart(BlockStateModelPart parent, BlockState camoState, boolean translucent) {
            this.parent = parent;
            this.camoState = camoState;
            this.translucent = translucent;
        }

        @Override
        public @NonNull List<BakedQuad> getQuads(@Nullable Direction direction) {
            List<BakedQuad> quads = parent.getQuads(direction);
            if (translucent) {
                List<BakedQuad> modified = new ArrayList<>(quads.size());
                for (BakedQuad quad : quads) {
                    modified.add(withAlpha(quad));
                }
                return modified;
            }
            return quads;
        }

        private BakedQuad withAlpha(BakedQuad quad) {
            BakedColors colors = quad.bakedColors();
            BakedColors newColors = BakedColors.of(
                    setAlpha(colors.color(0)),
                    setAlpha(colors.color(1)),
                    setAlpha(colors.color(2)),
                    setAlpha(colors.color(3))
            );

            // In 26.1, we must also update the MaterialInfo to use translucent layer
            BakedQuad.MaterialInfo oldInfo = quad.materialInfo();
            BakedQuad.MaterialInfo newInfo = new BakedQuad.MaterialInfo(
                    oldInfo.sprite(),
                    ChunkSectionLayer.TRANSLUCENT,
                    oldInfo.itemRenderType(),
                    oldInfo.tintIndex(),
                    oldInfo.shade(),
                    oldInfo.lightEmission(),
                    oldInfo.ambientOcclusion()
            );

            return new BakedQuad(
                    quad.position0(), quad.position1(), quad.position2(), quad.position3(),
                    quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(),
                    quad.direction(), newInfo, quad.bakedNormals(), newColors
            );
        }

        private int setAlpha(int color) {
            return (color & 0x00FFFFFF) | (0x7F << 24);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return parent.useAmbientOcclusion();
        }

        @Override
        public Material.@NonNull Baked particleMaterial() {
            return parent.particleMaterial();
        }

        @Override
        public @NonNull TriState ambientOcclusion() {
            return parent.ambientOcclusion();
        }

        @Override
        public @BakedQuad.MaterialFlags int materialFlags() {
            return parent.materialFlags();
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
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(s);
    }

    public static FacadeModelWrapper wrap(BlockStateModel original) {
        return new FacadeModelWrapper(original, FacadeModelWrapper::bakedFor);
    }
}
