package dev.gacbl.bblroutersfacade.facade;

import dev.gacbl.bblroutersfacade.RouterFacades;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class FacadeAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RouterFacades.MOD_ID);

    // Store the block id; omit serialization when null; enable client sync
    public static final Supplier<AttachmentType<ResourceLocation>> FACADE_ID =
            ATTACHMENT_TYPES.register("facade_id", () ->
                    AttachmentType.<ResourceLocation>builder(() -> null)
                            .serialize(ResourceLocation.CODEC, rl -> rl != null)     // save/load
                            .sync(ResourceLocation.STREAM_CODEC)                      // auto-sync to clients
                            .build()
            );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }

    private FacadeAttachments() {
    }
}
