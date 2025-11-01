package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeData {
    BlockState get();
    void set(BlockState state);
    boolean has();
    void clear();
}
