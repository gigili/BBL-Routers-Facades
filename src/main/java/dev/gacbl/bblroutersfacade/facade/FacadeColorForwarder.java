package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = RouterFacades.MOD_ID, value = Dist.CLIENT)
public final class FacadeColorForwarder {
    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.BlockTintSources e) {
        BlockColors colors = e.getBlockColors();

        List<Block> routerBlocks = BuiltInRegistries.BLOCK.stream()
                .filter(b -> {
                    String ns = BuiltInRegistries.BLOCK.getKey(b).getNamespace();
                    return ns.equals("routers") || ns.equals("bbl_routers");
                })
                .toList();

        if (routerBlocks.isEmpty()) return;

        List<BlockTintSource> sources = new ArrayList<>();
        // Register for multiple tint indices to be safe
        for (int i = 0; i < 2; i++) {
            final int index = i;
            sources.add(new BlockTintSource() {
                @Override
                public int color(BlockState state) {
                    return -1;
                }

                @Override
                public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
                    if (level == null || pos == null) return -1;
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be == null) return -1;
                    BlockState camo = be.getData(FacadeAttachments.FACADE_STATE.get());
                    if (camo == null) return -1;

                    var camoSources = colors.getTintSources(camo);
                    if (index < camoSources.size()) {
                        return camoSources.get(index).colorInWorld(camo, level, pos);
                    }
                    return -1;
                }
            });
        }

        e.register(sources, routerBlocks.toArray(Block[]::new));
    }
}
