package me.ethanbrews.rituals.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class RitualConsumeParticleProvider implements ParticleProvider<RitualConsumeParticleOptions> {

    public RitualConsumeParticleProvider(SpriteSet spriteSet) {
        // SpriteSet not needed since we use item texture
    }

    @Override
    public Particle createParticle(RitualConsumeParticleOptions options, ClientLevel level,
                                   double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed) {
        return new RitualConsumeParticle(level, x, y, z,
                options.getTargetX(), options.getTargetY(), options.getTargetZ(),
                options.getItem());
    }
}
