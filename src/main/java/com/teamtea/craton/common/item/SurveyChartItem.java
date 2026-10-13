package com.teamtea.craton.common.item;

import com.teamtea.craton.common.survey.SurveyMaps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** The blank chart becomes an ordinary locked filled map after the survey finishes. */
public class SurveyChartItem extends Item {
    public SurveyChartItem(Properties properties){super(properties.stacksTo(1).component(net.minecraft.core.component.DataComponents.LORE,
            new net.minecraft.world.item.component.ItemLore(java.util.List.of(
                    net.minecraft.network.chat.Component.translatable("survey.craton.blank_hint")))));}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(player instanceof ServerPlayer serverPlayer)
            return SurveyMaps.start(serverPlayer,hand)?InteractionResult.SUCCESS_SERVER:InteractionResult.FAIL;
        return InteractionResult.SUCCESS;
    }
}
