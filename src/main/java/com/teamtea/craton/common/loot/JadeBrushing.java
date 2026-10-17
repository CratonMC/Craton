package com.teamtea.craton.common.loot;

import com.teamtea.craton.common.block.OreBlock;
import com.teamtea.craton.common.registry.CratonBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.ArrayList;

/** Brush extraction consumes the mineralized block, preventing repeated harvests. */
@EventBusSubscriber
public class JadeBrushing {
    @SubscribeEvent
    public static void brush(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var player = event.getEntity();
        var tool = event.getItemStack();
        var state = level.getBlockState(pos);
        if (!tool.is(Items.BRUSH) || !state.is(CratonBlocks.suspicious_jadeitite.value())) return;
        if (!player.mayBuild() || !level.mayInteract(player, pos)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide()) return;
        var ore = (OreBlock) state.getBlock();
        var products = new ArrayList<ItemStack>();
        OreProductsEntry.roll(ore.oreDrops(), level.getRandom(), products::add);
        if (products.isEmpty() || !level.setBlock(pos, ore.host().defaultBlockState(), Block.UPDATE_ALL)) return;
        for (var product : products) Block.popResource(level, pos, product);
        tool.hurtAndBreak(1, player, event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    private JadeBrushing() {}
}
