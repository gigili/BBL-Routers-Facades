package dev.gacbl.bblroutersfacadeaddon.facade;


import net.minecraft.world.level.block.state.BlockState;

public final class FacadeData implements IFacadeData {
    private BlockState state;
    @Override public BlockState get() { return state; }
    @Override public void set(BlockState s) { state = s; }
    @Override public boolean has() { return state != null; }
    @Override public void clear() { state = null; }
}
