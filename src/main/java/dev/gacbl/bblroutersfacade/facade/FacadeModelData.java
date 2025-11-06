package dev.gacbl.bblroutersfacade.facade;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class FacadeModelData {
    public static final ModelProperty<BlockState> FACADE = new ModelProperty<>();
    public static final ModelProperty<ModelData> CAMO_MODEL_DATA = new ModelProperty<>();
}
