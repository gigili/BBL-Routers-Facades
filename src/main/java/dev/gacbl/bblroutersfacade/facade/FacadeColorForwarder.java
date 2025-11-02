package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public final class FacadeColorForwarder {
    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block e) {
        BlockColors colors = e.getBlockColors();

        List<Block> routerBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(b -> BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals(RouterFacades.TARGET_NS))
                .toList();

        if (routerBlocks.isEmpty()) return;

        e.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null) return -1;
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null) return -1;
            BlockState camo = be.getData(FacadeAttachments.FACADE_STATE.get());
            if (camo == null) return -1;
            return colors.getColor(camo, level, pos, tintIndex);
        }, routerBlocks.toArray(Block[]::new));
    }
}
