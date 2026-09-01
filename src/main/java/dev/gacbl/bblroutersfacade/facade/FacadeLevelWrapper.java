package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for BlockAndTintGetter used while collecting client-side model
 * geometry for connected textures (like XyCraft).
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
        return getSpoofedBlockState(pos);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NotNull BlockPos pos) {
        // Also check if this position should be spoofed
        BlockState originalState = delegate.getBlockState(pos);
        BlockState spoofedState = getSpoofedBlockState(pos);
        if (!spoofedState.equals(originalState)) {
            // If we're spoofing this block, return null for BlockEntity to prevent conflicts
            return null;
        }
        return delegate.getBlockEntity(pos);
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockPos pos) {
        BlockState originalState = delegate.getBlockState(pos);
        BlockState spoofedState = getSpoofedBlockState(pos);

        if (!spoofedState.equals(originalState)) {
            // Provide model data that matches the spoofed block
            return ModelData.builder()
                    .with(FacadeConnectionHelper.FACADE_STATE_PROPERTY, spoofedState)
                    .build();
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
    public @NotNull CardinalLighting cardinalLighting() {
        return delegate.cardinalLighting();
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
    public int getMinY() {
        return delegate.getMinY();
    }
}
