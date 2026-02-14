package me.ethanbrews.rituals.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.ethanbrews.rituals.Ritual;
import me.ethanbrews.rituals.particle.RitualConsumeParticleOptions;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnchantmentPedestalBlockEntityRenderer implements BlockEntityRenderer<EnchantPedestalBlockEntity> {
    private final ItemRenderer itemRenderer;

    public EnchantmentPedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EnchantPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderItemOnPedestal(blockEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
        spawnEnchantParticles(blockEntity, partialTick);
        spawnConsumeParticles(blockEntity, partialTick);
    }

    private void renderItemOnPedestal(EnchantPedestalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // Position: centered horizontally, floating above the pedestal
        poseStack.translate(0.5, 1.2, 0.5); // Adjust Y value for height

        // Rotation: spin around Y axis based on world time
        var level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        long time = level.getGameTime();
        float rotation = (time + partialTick) * 2.0F; // Speed of rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Bobbing animation (optional)
        float bob = (float) Math.sin((time + partialTick) / 10.0) * 0.05F;
        poseStack.translate(0, bob, 0);

        // Scale (adjust size of item)
        poseStack.scale(0.7F, 0.7F, 0.7F);

        // Render the item
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight,
                packedOverlay, poseStack, bufferSource, level, 0);

        poseStack.popPose();
    }

    private void spawnEnchantParticles(EnchantPedestalBlockEntity blockEntity, float partialTick) {
        if (!blockEntity.isRitualParticipant()) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockPos pos = blockEntity.getBlockPos();

        if (blockEntity.isRitualController()) {
            // Controller: spawn particles in 4 block radius, less frequently
            // Only spawn particles 20% of the time (5x less frequent)
            if (level.random.nextFloat() > 0.2f) return;

            int particleCount = level.random.nextInt(2) + 1; // 1-2 particles per spawn

            for (int i = 0; i < particleCount; i++) {
                // Random position in 4 block radius
                double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 8.0; // 4 block radius = 8 block diameter
                double y = pos.getY() + 0.5 + level.random.nextDouble() * 2.0;
                double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 8.0;

                // Slow upward drift
                double velX = (level.random.nextDouble() - 0.5) * 0.02;
                double velY = 0.05;
                double velZ = (level.random.nextDouble() - 0.5) * 0.02;

                // Mix of enchant and portal particles
                ParticleOptions particleType = level.random.nextBoolean()
                        ? ParticleTypes.ENCHANT
                        : ParticleTypes.PORTAL;

                level.addParticle(particleType, x, y, z, velX, velY, velZ);
            }

        } else {
            // Non-controller pedestals: very rare particles
            // Only spawn 2% of the time (50x less frequent than before)
            if (level.random.nextFloat() > 0.02f) return;

            // Single particle near the pedestal
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 0.5 + level.random.nextDouble() * 1.2;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;

            double velX = (level.random.nextDouble() - 0.5) * 0.02;
            double velY = 0.05;
            double velZ = (level.random.nextDouble() - 0.5) * 0.02;

            // Mostly enchant, occasionally portal
            ParticleOptions particleType = level.random.nextFloat() < 0.8f
                    ? ParticleTypes.ENCHANT
                    : ParticleTypes.PORTAL;

            level.addParticle(particleType, x, y, z, velX, velY, velZ);
        }
    }

    private void spawnConsumeParticles(EnchantPedestalBlockEntity blockEntity, float partialTick) {
        if (!blockEntity.isConsumingItem()) return;

        Level level = blockEntity.getLevel();
        BlockPos controllerPos = blockEntity.getControllerPos();
        if (level == null || controllerPos == null) return;

        ItemStack stack = blockEntity.getItem();
        if (stack.isEmpty()) return;

        if (level.random.nextFloat() > 0.3f) return;

        BlockPos pos = blockEntity.getBlockPos();

        double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
        double y = pos.getY() + 1.2;
        double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;

        // Target position
        double targetX = controllerPos.getX() + 0.5;
        double targetY = controllerPos.getY() + 1.2;
        double targetZ = controllerPos.getZ() + 0.5;

        // Spawn custom homing particle
        level.addParticle(
                new RitualConsumeParticleOptions(
                        targetX, targetY, targetZ,
                        stack
                ),
                x, y, z,
                0, 0, 0
        );

        // Gentle item break particles (slower, subtle)
        if (level.random.nextFloat() < 0.4f) { // Only sometimes
            double velX = (level.random.nextDouble() - 0.5) * 0.03; // Very gentle
            double velY = level.random.nextDouble() * 0.02; // Slight upward
            double velZ = (level.random.nextDouble() - 0.5) * 0.03;

            level.addParticle(
                    new ItemParticleOption(ParticleTypes.ITEM, stack),
                    x, y, z,
                    velX, velY, velZ
            );
        }
    }
}
