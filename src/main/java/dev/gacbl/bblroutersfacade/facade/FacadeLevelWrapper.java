package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for BlockAndTintGetter (Level) used exclusively during client-side
 * model baking for connected textures (like XyCraft).
 * <p>
 * It spoofs the BlockState returned for our facade/router block positions,
 * reporting the *camouflaged state* instead of the router block state.
 * This makes client-side connection handlers believe they are next to a matching block.
 */
public class FacadeLevelWrapper implements BlockAndTintGetter {
    private final BlockAndTintGetter delegate;

    public FacadeLevelWrapper(BlockAndTintGetter delegate) {
        this.delegate = delegate;
    }

    @Override
    public @NotNull BlockState getBlockState(@NotNull BlockPos pos) {
        BlockState originalState = delegate.getBlockState(pos);
        BlockState spoofedState = getSpoofedBlockState(pos);

        if (!spoofedState.equals(originalState)) {
            //System.out.println("Fusion checking " + pos + ":");
            //System.out.println("  Original: " + originalState);
            //System.out.println("  Spoofed: " + spoofedState);

            // Check if this is a facade block entity
            BlockEntity be = delegate.getBlockEntity(pos);
            if (be != null) {
                //System.out.println("  BlockEntity: " + be.getClass().getName());
                BlockState facadeState = be.getData(FacadeAttachments.FACADE_STATE.get());
                //System.out.println("  Facade state: " + facadeState);
            }
        }

        return spoofedState;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NotNull BlockPos pos) {
        // Also check if this position should be spoofed
        BlockState spoofedState = getSpoofedBlockState(pos);
        if (spoofedState != delegate.getBlockState(pos)) {
            // If we're spoofing this block, return null for BlockEntity to prevent conflicts
            return null;
        }
        return delegate.getBlockEntity(pos);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockPos pos) {
        BlockState spoofedState = getSpoofedBlockState(pos);
        BlockState originalState = delegate.getBlockState(pos);

        if (!spoofedState.equals(originalState)) {
            // For spoofed positions, provide model data that matches the spoofed block
            ModelData originalData = delegate.getModelData(pos);

            // Try to get model data for the spoofed block
            BakedModel spoofedModel = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(spoofedState);
            if (spoofedModel != null) {
                ModelData spoofedData = spoofedModel.getModelData(this, pos, spoofedState, ModelData.EMPTY);
                //System.out.println("Providing spoofed model data for " + pos);
                return spoofedData;
            }

            return originalData;
        }

        return delegate.getModelData(pos);
    }

    private BlockState getSpoofedBlockState(BlockPos pos) {
        BlockEntity be = delegate.getBlockEntity(pos);
        if (be != null) {
            BlockState facadeState = be.getData(FacadeAttachments.FACADE_STATE.get());
            if (facadeState != null) {
                return facadeState;
            }
        }

        // TEMPORARY: For testing, spoof all positions around our facades as Connected Glass
        // This will help determine if the issue is with neighbor detection
        BlockPos[] facadePositions = {new BlockPos(0, 58, -9), new BlockPos(0, 57, -9)}; // Your facade positions
        for (BlockPos facadePos : facadePositions) {
            if (pos.closerThan(facadePos, 2)) { // Within 2 blocks of a facade
                BlockEntity facadeBe = delegate.getBlockEntity(facadePos);
                if (facadeBe != null) {
                    BlockState neighborFacade = facadeBe.getData(FacadeAttachments.FACADE_STATE.get());
                    if (neighborFacade != null && "connectedglass".equals(BuiltInRegistries.BLOCK.getKey(neighborFacade.getBlock()).getNamespace())) {
                        //System.out.println("TEMP: Spoofing neighbor " + pos + " as Connected Glass for testing");
                        return neighborFacade;
                    }
                }
            }
        }

        return delegate.getBlockState(pos);
    }

    @Override
    public @NotNull FluidState getFluidState(@NotNull BlockPos blockPos) {
        return delegate.getFluidState(blockPos);
    }

    @Override
    public int getLightEmission(@NotNull BlockPos pos) {
        return delegate.getLightEmission(pos);
    }

    @Override
    public float getShade(@NotNull Direction direction, boolean bl) {
        return delegate.getShade(direction, bl);
    }

    @Override
    public @NotNull LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getBlockTint(@NotNull BlockPos blockPos, @NotNull ColorResolver colorResolver) {
        return delegate.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return delegate.getMinBuildHeight();
    }
}
