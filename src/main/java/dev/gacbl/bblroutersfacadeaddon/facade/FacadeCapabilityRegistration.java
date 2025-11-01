package dev.gacbl.bblroutersfacadeaddon.facade;

import dev.gacbl.bblroutersfacadeaddon.RouterFacades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.WeakHashMap;

@EventBusSubscriber(modid = RouterFacades.MOD_ID)
public final class FacadeCapabilityRegistration {
    private static final WeakHashMap<BlockEntity, FacadeData> STORE = new WeakHashMap<>();

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            var key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
            if (key != null && RouterFacades.TARGET_NS.equals(key.getNamespace())) {
                event.registerBlockEntity(
                        FacadeCapability.CAP,
                        type,
                        (be, ctx) -> STORE.computeIfAbsent(be, k -> new FacadeData())
                );
            }
        }
    }
}
