package me.ethanbrews.rituals.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RitualConsumeParticle extends TextureSheetParticle {
    private final Vec3 target;
    private final double speed;
    private final ItemStack item;

    protected RitualConsumeParticle(ClientLevel level, double x, double y, double z,
                                    double targetX, double targetY, double targetZ,
                                    ItemStack item) {
        super(level, x, y, z);

        this.target = new Vec3(targetX, targetY, targetZ);
        this.speed = 0.1;
        this.item = item;

        this.lifetime = 40;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.quadSize = 0.15F; // Slightly smaller
        this.setAlpha(0.9F);

        // Set sprite from item texture
        setSprite(Minecraft.getInstance().getItemRenderer()
                .getModel(item, level, null, 0)
                .getParticleIcon());

        updateVelocity();
    }

    @Override
    public void tick() {
        super.tick();
        updateVelocity();

        float fadeProgress = 1.0F - ((float) age / (float) lifetime);
        this.setAlpha(0.9F * fadeProgress);

        Vec3 currentPos = new Vec3(x, y, z);
        double distance = currentPos.distanceTo(target);
        this.quadSize = (float) (0.15F * Math.min(1.0, distance / 3.0));
    }

    private void updateVelocity() {
        Vec3 currentPos = new Vec3(x, y, z);
        Vec3 direction = target.subtract(currentPos).normalize();

        double distance = currentPos.distanceTo(target);
        double acceleration = 1.0 + (3.0 / Math.max(distance, 1.0));

        this.xd = direction.x * speed * acceleration;
        this.yd = direction.y * speed * acceleration;
        this.zd = direction.z * speed * acceleration;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }
}