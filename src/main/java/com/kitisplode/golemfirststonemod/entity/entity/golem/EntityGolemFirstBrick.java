package com.kitisplode.golemfirststonemod.entity.entity.golem;

import com.kitisplode.golemfirststonemod.GolemFirstStoneMod;
import com.kitisplode.golemfirststonemod.entity.entity.IEntityWithDelayedMeleeAttack;
import com.kitisplode.golemfirststonemod.entity.goal.goal.MultiStageAttackGoalRanged;
import com.kitisplode.golemfirststonemod.entity.goal.target.PassiveTargetGoal;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EntityGolemFirstBrick extends IronGolemEntity implements GeoEntity, IEntityWithDelayedMeleeAttack
{
	private static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(EntityGolemFirstBrick.class, TrackedDataHandlerRegistry.INTEGER);
	private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
	private final int shieldCooldown = 20 * 15;
	private final int shieldHurtTime = 30;
	private final int shieldAbsorptionTime = 20 * 5;
	private final int shieldAbsorptionAmount = 0;
	private final float attackAOERange = 10.0f;
	private final float attackVerticalRange = 5.0f;
	private final ArrayList<StatusEffectInstance> shieldStatusEffects = new ArrayList();

	public EntityGolemFirstBrick(EntityType<? extends IronGolemEntity> pEntityType, World pLevel)
	{
		super(pEntityType, pLevel);
		shieldStatusEffects.add(new StatusEffectInstance(StatusEffects.ABSORPTION, shieldAbsorptionTime, shieldAbsorptionAmount, false, true));
	}

	public static DefaultAttributeContainer.Builder setAttributes()
	{
		return GolemEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 1000.0f)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25f)
			.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 30.0f)
			.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0f)
			.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32);
	}

	@Override
	protected void initDataTracker()
	{
		super.initDataTracker();
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
		this.goalSelector.add(1, new MultiStageAttackGoalRanged(this, 1.0, true, MathHelper.square(attackAOERange), new int[]{70, 30, 25}, 0, shieldCooldown));
		this.goalSelector.add(2, new WanderNearTargetGoal(this, 0.8, 32.0F));
		this.goalSelector.add(2, new WanderAroundPointOfInterestGoal(this, 0.8, false));
		this.goalSelector.add(4, new IronGolemWanderAroundGoal(this, 0.8));
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));
		this.targetSelector
				.add(1, new PassiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 5, false, false, playerTarget()));
		this.targetSelector
			.add(2, new PassiveTargetGoal<MobEntity>(this, MobEntity.class, 5, false, false, golemTarget()));
	}

	private Predicate<LivingEntity> playerTarget()
	{
		return entity ->
		{
			// Check players.
			float thing = MathHelper.abs(entity.getLastAttackedTime() - entity.age);
			if (thing < shieldHurtTime)
				return true;
			return false;
		};
	}

	private Predicate<LivingEntity> golemTarget()
	{
		return entity ->
		{
			// Skip itself.
			if (entity == this) return false;
			// Check other golems, villagers
			if (entity instanceof GolemEntity
					|| entity instanceof MerchantEntity)
			{
				// For golems currently being attacked:
				LivingEntity targetCurrentAttacker = entity.getAttacker();
				if (targetCurrentAttacker != null && targetCurrentAttacker.isAlive())
				{
					return golemTarget_checkTargetAttacker(targetCurrentAttacker);
				}

				// For golems not currently being attacked but attacked recently.
				LivingEntity targetLastAttacker = entity.getLastAttacker();
				if (targetLastAttacker != null)
				{
					if (MathHelper.abs(entity.getLastAttackedTime() - entity.age) < shieldHurtTime)
					{
						GolemFirstStoneMod.LOGGER.info("golem last attacked time: " + entity.getLastAttackedTime() + " | golem age: " + entity.age);
						return golemTarget_checkTargetAttacker(targetLastAttacker);
					}
				}
//				if (entity.getHealth() < entity.getMaxHealth()) return true;
			}
			return false;
		};
	}

	private boolean golemTarget_checkTargetAttacker(LivingEntity targetAttacker)
	{
		// If the golem was player made, skip potential targets that were attacked by the player.
		if (targetAttacker instanceof PlayerEntity && this.isPlayerCreated())
		{
			return false;
		}
		// Skip other potential targets that are being attacked by golems (only happens accidentally or by other cleric golems)
		if (targetAttacker instanceof GolemEntity) return false;
		// Otherwise, this is a good target.
		return true;
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
	}

	@Override
	public boolean tryAttack()
	{
		if (getAttackState() != 3) return false;

		this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_ATTACK_SOUND);
		this.playSound(SoundEvents.ITEM_SHIELD_BLOCK, 1.0f, 1.0f);
		attackDust();
		attackAOE();

		// Check to see if the target is still viable...
		LivingEntity target = this.getTarget();
		if (target != null && target.isAlive())
		{
			boolean targetGood = golemTarget().test(target);
			if (!targetGood) this.setTarget(null);
		}
		else
		{
			this.setTarget(null);
		}

		return true;
	}

	private void attackDust()
	{
		AreaEffectCloudEntity dust = new AreaEffectCloudEntity(getWorld(), getX(),getY(),getZ());
		dust.setParticleType(ParticleTypes.SMOKE);
		dust.setRadius(attackAOERange + 1);
		dust.setDuration(1);
		dust.setPos(getX(),getY(),getZ());
		getWorld().spawnEntity(dust);
	}

	private void attackAOE()
	{
		List<LivingEntity> targetList = getWorld().getNonSpectatingEntities(LivingEntity.class, getBoundingBox().expand(attackAOERange));
		for (LivingEntity target : targetList)
		{
			// Do not shield ourselves.
			if (target == this) continue;
			// Do not shield targets that are NOT villagers, golems, or players.
			if (!(target instanceof MerchantEntity
					|| target instanceof GolemEntity
					|| (target instanceof PlayerEntity && isPlayerCreated())))
				continue;
			// Do not shield targets that are too far on the y axis.
			if (Math.abs(getY() - target.getY()) > attackVerticalRange) continue;

			for (StatusEffectInstance statusEffectInstance : shieldStatusEffects)
			{
				StatusEffect statusEffect = statusEffectInstance.getEffectType();
				int i2 = statusEffectInstance.mapDuration(i -> (int)(1 * (double)i + 0.5));
				StatusEffectInstance statusEffectInstance2 = new StatusEffectInstance(statusEffect, i2, statusEffectInstance.getAmplifier(), statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles());
				if (statusEffectInstance2.isDurationBelow(20)) continue;
				target.addStatusEffect(statusEffectInstance2, this);
			}

			// Apply damage.
//			float forceMultiplier = Math.abs((attackAOERange - this.distanceTo(target)) / attackAOERange);
//			float totalDamage = getAttackDamage() * forceMultiplier;
//			target.damage(getDamageSources().mobAttack(this), totalDamage);
//			// Apply knockback.
//			double knockbackResistance = Math.max(0.0, 1.0 - target.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
//			double knockbackForce = knockbackResistance * attackKnockbackAmount;
//			Vec3d knockbackDirection = target.getPos().subtract(getPos()).normalize().add(0,attackKnockbackAmountVertical,0);
//			target.setVelocity(target.getVelocity().add(knockbackDirection.multiply(knockbackForce)));
//			applyDamageEffects(this, target);
		}
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (!itemStack.isOf(Items.STONE)) {
			return ActionResult.PASS;
		}
		float f = this.getHealth();
		this.heal(25.0f);
		if (this.getHealth() == f) {
			return ActionResult.PASS;
		}
		float g = 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f;
		this.playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0f, g);
		if (!player.getAbilities().creativeMode) {
			itemStack.decrement(1);
		}
		return ActionResult.success(this.getWorld().isClient);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar)
	{
		controllerRegistrar.add(new AnimationController<>(this, "controller", 0, event ->
		{
			EntityGolemFirstBrick pGolem = event.getAnimatable();
			if (pGolem.getAttackState() > 0)
			{
				switch (pGolem.getAttackState())
				{
					case 1:
						event.getController().setAnimationSpeed(0.5);
						return event.setAndContinue(RawAnimation.begin().then("animation.first_brick.attack_windup", Animation.LoopType.HOLD_ON_LAST_FRAME));
					case 2:
						event.getController().setAnimationSpeed(1.00);
						return event.setAndContinue(RawAnimation.begin().then("animation.first_brick.attack", Animation.LoopType.HOLD_ON_LAST_FRAME));
					default:
						event.getController().setAnimationSpeed(1.00);
						return event.setAndContinue(RawAnimation.begin().then("animation.first_brick.attack_end", Animation.LoopType.HOLD_ON_LAST_FRAME));
				}
			}
			else
			{
				event.getController().setAnimationSpeed(1.00);
				pGolem.setAttackState(0);
				if (getVelocity().horizontalLengthSquared() > 0.001D || event.isMoving())
					return event.setAndContinue(RawAnimation.begin().thenLoop("animation.first_brick.walk"));
			}
			return event.setAndContinue(RawAnimation.begin().thenLoop("animation.first_brick.idle"));
		}));
	}

	@Override public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}
}
