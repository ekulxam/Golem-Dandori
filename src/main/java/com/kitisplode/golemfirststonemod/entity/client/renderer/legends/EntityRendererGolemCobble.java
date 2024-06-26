package com.kitisplode.golemfirststonemod.entity.client.renderer.legends;

import com.kitisplode.golemfirststonemod.GolemFirstStoneMod;
import com.kitisplode.golemfirststonemod.entity.client.model.legends.EntityModelGolemCobble;
import com.kitisplode.golemfirststonemod.entity.entity.golem.legends.EntityGolemCobble;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EntityRendererGolemCobble extends GeoEntityRenderer<EntityGolemCobble>
{
	public EntityRendererGolemCobble(EntityRendererFactory.Context renderManager)
	{
		super(renderManager, new EntityModelGolemCobble());
		this.shadowRadius = 0.4f;
	}

	@Override
	public Identifier getTextureLocation(EntityGolemCobble animatable)
	{
		return new Identifier(GolemFirstStoneMod.MOD_ID, "textures/entity/golem/golem_cobble.png");
	}

	@Override
	public void render(EntityGolemCobble entity, float entityYaw, float partialTick, MatrixStack poseStack,
					   VertexConsumerProvider bufferSource, int packedLight)
	{
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
