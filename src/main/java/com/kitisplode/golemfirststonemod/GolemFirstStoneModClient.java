package com.kitisplode.golemfirststonemod;

import com.kitisplode.golemfirststonemod.block.ModBlocks;
import com.kitisplode.golemfirststonemod.client.HudDandoriCount;
import com.kitisplode.golemfirststonemod.entity.ModEntities;
import com.kitisplode.golemfirststonemod.item.ModItems;
import com.kitisplode.golemfirststonemod.networking.ModMessages;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class GolemFirstStoneModClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ModItems.registerModItemsClient();
        ModBlocks.registerTransparentBlocks();
        ModEntities.registerModEntitiesRenderers();
        ModMessages.registerS2CPackets();
        HudRenderCallback.EVENT.register(new HudDandoriCount());
    }
}
