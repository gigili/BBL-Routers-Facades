package dev.gacbl.bblroutersfacade.item;

import dev.gacbl.bblroutersfacade.RouterFacades;
import dev.gacbl.bblroutersfacade.facade.FacadeOps;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FacadeApplicatorItem extends Item {
    private static final String TAG = "picked_block";

    public FacadeApplicatorItem(Properties props) { super(props); }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            clearPick(stack);
            player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.cleared_picked_facade"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player == null) return InteractionResult.PASS;

        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean sneaking = player.isShiftKeyDown();
        boolean isRouter = isRouterBlock(state);

        // Right-click router (sneak) -> APPLY stored pick
        if (isRouter && sneaking) {
            ResourceLocation picked = getPickedId(stack);
            if (picked == null) {
                if (!level.isClientSide) {
                    FacadeOps.clear(level, pos);
                    player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.cleared_facade"), true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                FacadeOps.apply(level, pos, picked);
                var key = Util.makeDescriptionId("block", picked);
                player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.applied_facade", Component.translatable(key)), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Right-click any non-router block (no sneak) -> PICK source
        if (!sneaking && !isRouter) {
            var id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (id != null) {
                if (!level.isClientSide) {
                    setPickedId(stack, id);
                    var key = Util.makeDescriptionId("block", id);
                    player.displayClientMessage(Component.translatable("item.bblroutersfacade.facade_applicator.actions.picked_facade", Component.translatable(key)), true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }


    private static boolean isRouterBlock(BlockState s) {
        var key = BuiltInRegistries.BLOCK.getKey(s.getBlock());
        return RouterFacades.TARGET_NS.equals(key.getNamespace());
    }

    private static ResourceLocation getPickedId(ItemStack stack) {
        var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        if (!tag.contains(TAG)) return null;
        return ResourceLocation.tryParse(tag.getString(TAG));
    }

    private static void setPickedId(ItemStack stack, ResourceLocation id) {
        CompoundTag t = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        t.putString(TAG, id.toString());
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    private static void clearPick(ItemStack stack) {
        var existing = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (existing == null) return;
        CompoundTag t = existing.copyTag();
        if (t.contains(TAG)) {
            t.remove(TAG);
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(t));
        }
    }
}
