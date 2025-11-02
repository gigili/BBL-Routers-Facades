package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper for BlockAndTintGetter (Level) used exclusively during client-side
 * model baking for connected textures (like XyCraft).
 *
 * It spoofs the BlockState returned for our facade/router block positions,
 * reporting the *camouflaged state* instead of the router block state.
 * This makes client-side connection handlers believe they are next to a matching block.
 */
public class FacadeLevelWrapper implements BlockAndTintGetter {
    private final BlockAndTintGetter delegate;

    /**
     * @param delegate The actual level/world object.
     */
    public FacadeLevelWrapper(BlockAndTintGetter delegate) {
        this.delegate = delegate;
    }

    /**
     * Overrides block state fetching.
     * If the queried position has a facade, we return the camouflaged state.
     */
    @Override
    public BlockState getBlockState(BlockPos pos) {
        // Check if the block is one of our routers/facades
        BlockEntity neighborBe = delegate.getBlockEntity(pos);
        if (neighborBe != null) {
            BlockState neighborFacade = neighborBe.getData(FacadeAttachments.FACADE_STATE.get());
            if (neighborFacade != null) {
                // Return the camouflaged state instead of the actual router state
                return neighborFacade;
            }
        }

        // For all other cases, return the true state from the world
        return delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return delegate.getFluidState(blockPos);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return delegate.getBlockEntity(pos);
    }

    @Override
    public int getLightEmission(BlockPos pos) {
        return delegate.getLightEmission(pos);
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        return delegate.getShade(direction, bl);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return delegate.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public ModelData getModelData(BlockPos pos) {
        return delegate.getModelData(pos);
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
