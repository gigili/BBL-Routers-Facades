package dev.gacbl.bblroutersfacade.item;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RouterFacades.MOD_ID);

    public static final Supplier<CreativeModeTab> RM_TAB = CREATIVE_MODE_TAB.register("rm_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.FACADE_APPLICATOR.get()))
            .title(Component.translatable("creative.bblroutersfacade.tab"))
            .displayItems(((itemDisplayParameters, output) -> {
                output.accept(ModItems.FACADE_APPLICATOR.get());
            }))
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
