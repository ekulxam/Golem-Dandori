package com.kitisplode.golemfirststonemod.entity.entity.golem.first;

import com.kitisplode.golemfirststonemod.entity.ModEntities;
import com.kitisplode.golemfirststonemod.entity.entity.golem.AbstractGolemDandoriFollower;
import com.kitisplode.golemfirststonemod.entity.entity.interfaces.IEntityDandoriFollower;
import com.kitisplode.golemfirststonemod.entity.entity.projectile.EntityProjectileAoEOwnerAware;
import com.kitisplode.golemfirststonemod.entity.entity.interfaces.IEntityWithDelayedMeleeAttack;
import com.kitisplode.golemfirststonemod.entity.goal.action.DandoriFollowHardGoal;
import com.kitisplode.golemfirststonemod.entity.goal.action.DandoriFollowSoftGoal;
import com.kitisplode.golemfirststonemod.entity.goal.action.DandoriMoveToDeployPositionGoal;
import com.kitisplode.golemfirststonemod.entity.goal.action.MultiStageAttackGoalRanged;
import com.kitisplode.golemfirststonemod.entity.goal.target.SharedTargetGoal;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

public class EntityGolemFirstOak extends AbstractGolemDandoriFollower implements GeoEntity, IEntityWithDelayedMeleeAttack, IEntityDandoriFollower
{
	protected static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(EntityGolemFirstOak.class, TrackedDataHandlerRegistry.INTEGER);
	private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
	private static final float attackAOERange = 4.0f;
	private static final float projectileSpeed = 5.0f;

	public EntityGolemFirstOak(EntityType<? extends IronGolemEntity> pEntityType, World pLevel)
	{
		super(pEntityType, pLevel);
	}

	public static DefaultAttributeContainer.Builder setAttributes()
	{
		return GolemEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0f)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25f)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.5f)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32);
	}

	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
		if (!this.dataTracker.containsKey(ATTACK_STATE))
			this.dataTracker.startTracking(ATTACK_STATE, 0);
	}

	public int getAttackState()
	{
		return this.dataTracker.get(ATTACK_STATE);
	}
	public void setAttackState(int pInt)
	{
		this.dataTracker.set(ATTACK_STATE, pInt);
	}

	private float getAttackDamage() {
		return (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
	}

	@Override
	public double getEyeY()
	{
		return getY() + 2.2f;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new DandoriFollowHardGoal(this, 1.4, dandoriMoveRange, dandoriSeeRange));
		this.goalSelector.add(1, new DandoriFollowSoftGoal(this, 1.4, dandoriMoveRange, dandoriSeeRange));

		this.goalSelector.add(2, new MultiStageAttackGoalRanged(this, 1.0, true, 1024.0, new int[]{40, 18, 13}, 0));
		this.goalSelector.add(3, new DandoriMoveToDeployPositionGoal(this, 2.0f, 1.0f));

		this.goalSelector.add(4, new DandoriFollowSoftGoal(this, 1.4, dandoriMoveRange, 0));

		this.goalSelector.add(5, new WanderNearTargetGoal(this, 0.8, 48.0F));
		this.goalSelector.add(6, new IronGolemWanderAroundGoal(this, 0.8));
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.add(7, new LookAtEntityGoal(this, MerchantEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));
		this.targetSelector.add(2, new RevengeGoal(this));
		this.targetSelector.add(3, new SharedTargetGoal<>(this, GolemEntity.class, MobEntity.class, 5, true, false, entity -> entity instanceof Monster && !(entity instanceof CreeperEntity), 24));
	}

	@Override
	public boolean tryAttack()
	{
		if (getAttackState() != 2) return false;

		// If we still don't have a target, maybe we shouldn't do anything? lol
		if (this.getTarget() == null) return false;

		this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
		this.playSound(SoundEvents.ITEM_CROSSBOW_SHOOT, 1.0f, 1.0f);
		attack();
		return true;
	}

	private void attack()
	{
		LivingEntity target = this.getTarget();
		if (target == null || !target.isAlive()) return;

		// Spawn the projectile
		if (!this.getWorld().isClient())
		{
			EntityProjectileAoEOwnerAware arrow = ModEntities.ENTITY_PROJECTILE_FIRST_OAK.create(this.getWorld());

			if (arrow == null) return;

			Vec3d shootingVelocity = target.getEyePos().subtract(this.getEyePos()).normalize().multiply(projectileSpeed);
			arrow.setPosition(this.getEyePos());
			arrow.setOwner(this);
			arrow.setVelocity(shootingVelocity);
			arrow.setDamage(getAttackDamage());
			arrow.setAoERange(attackAOERange);
			arrow.setNoGravity(true);
			arrow.setPierceLevel((byte)4);
			arrow.setHasAoE(false);
			this.getWorld().spawnEntity(arrow);
		}
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		return ActionResult.PASS;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "controller", 0, event ->
		{
			EntityGolemFirstOak pGolem = event.getAnimatable();
			if (pGolem.getAttackState() > 0)
			{
				switch (pGolem.getAttackState())
				{
					case 1:
						event.getController().setAnimationSpeed(1.00);
						return event.setAndContinue(RawAnimation.begin().then("animation.first_oak.attack_windup", Animation.LoopType.HOLD_ON_LAST_FRAME));
					case 2:
						event.getController().setAnimationSpeed(1.00);
						return event.setAndContinue(RawAnimation.begin().then("animation.first_oak.attack", Animation.LoopType.HOLD_ON_LAST_FRAME));
					default:
						event.getController().setAnimationSpeed(1.00);
						return event.setAndContinue(RawAnimation.begin().then("animation.first_oak.attack_end", Animation.LoopType.HOLD_ON_LAST_FRAME));
				}
			}
			else
			{
				event.getController().setAnimationSpeed(1.00);
				pGolem.setAttackState(0);
				if (getVelocity().horizontalLengthSquared() > 0.001D || event.isMoving())
					return event.setAndContinue(RawAnimation.begin().thenLoop("animation.first_oak.walk"));
			}
			return event.setAndContinue(RawAnimation.begin().thenLoop("animation.first_oak.idle"));
		}));
	}

	@Override public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
