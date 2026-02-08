package dev.gacbl.bblroutersfacade.item;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(RouterFacades.MOD_ID);

    public static final DeferredHolder<Item, FacadeApplicatorItem> FACADE_APPLICATOR = ITEMS.registerItem("facade_applicator", FacadeApplicatorItem::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
