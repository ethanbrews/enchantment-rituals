package me.ethanbrews.rituals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.ethanbrews.rituals.block.BrainInAJarBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;

public class BrainInAJarBlockEntityRenderer implements BlockEntityRenderer<BrainInAJarBlockEntity> {

    private static final ResourceLocation BRAIN_TEXTURE =
            new ResourceLocation("enchantmentrituals", "textures/item/brain.png");

    public BrainInAJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BrainInAJarBlockEntity jar, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        float fillPercent = jar.getFillPercent(); // 0.0 to 1.0
        renderBrain(poseStack, bufferSource, jar, partialTick, packedLight);
        if (fillPercent > 0) {

            renderLiquid(poseStack, bufferSource, fillPercent, packedLight);
        }
    }

    private void renderBrain(PoseStack poseStack, MultiBufferSource bufferSource,
                             BrainInAJarBlockEntity jar, float partialTick, int packedLight) {
        poseStack.pushPose();

        // Position in center of jar
        poseStack.translate(0.5, 0.55, 0.5);  // Centered, floating in middle

        // Get camera rotation to billboard towards player
        Minecraft mc = Minecraft.getInstance();
        float cameraYaw = mc.gameRenderer.getMainCamera().getYRot();
        float cameraPitch = mc.gameRenderer.getMainCamera().getXRot();

        // Rotate to face camera (billboard effect)
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(cameraPitch));

        // Size of the brain
        float size = 0.45f;
        poseStack.scale(size, size, size);

        // Bobbing animation
        long time = jar.getLevel() != null ? jar.getLevel().getGameTime() : 0;
        float bob = (float) Math.sin((time + partialTick) / 10.0) * 0.1f;
        poseStack.translate(0, bob, 0);

        // Render the brain as a textured quad
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(BRAIN_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        // Full white color (no tint)
        float r = 1.0f, g = 1.0f, b = 1.0f, a = 1.0f;

        // Render quad facing camera
        // Bottom-left
        consumer.vertex(matrix, -0.5f, -0.5f, 0)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();

        // Top-left
        consumer.vertex(matrix, -0.5f, 0.5f, 0)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();

        // Top-right
        consumer.vertex(matrix, 0.5f, 0.5f, 0)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();

        // Bottom-right
        consumer.vertex(matrix, 0.5f, -0.5f, 0)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0, 0, 1)
                .endVertex();

        poseStack.popPose();
    }

    private void renderLiquid(PoseStack poseStack, MultiBufferSource bufferSource,
                              float fillPercent, int packedLight) {
        poseStack.pushPose();

        // Coordinates
        float minX = 4f / 16f;
        float maxX = 12f / 16f;
        float minZ = 4f / 16f;
        float maxZ = 12f / 16f;
        float minY = 1f / 16f;
        float maxHeight = 13f / 16f;
        float maxY = minY + (maxHeight - minY) * fillPercent;

        // Green tint for XP
        float r = 0.5f;
        float g = 1.0f;
        float b = 0.0f;
        float a = 0.8f;

        // Get water texture sprites
        TextureAtlasSprite stillSprite = getWaterStillSprite();
        TextureAtlasSprite flowSprite = getWaterFlowSprite();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        // Top face - still texture
        addQuadTextured(consumer, matrix,
                minX, maxY, minZ,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ,
                0, 1, 0,
                r, g, b, a, packedLight,
                stillSprite,
                1.0f // Full texture repeat
        );

        // Bottom face - still texture
        addQuadTextured(consumer, matrix,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                minX, minY, maxZ,
                0, -1, 0,
                r, g, b, a, packedLight,
                stillSprite,
                1.0f
        );

        // Calculate height for texture scaling (less flow)
        float height = maxY - minY;
        float textureScale = 0.5f; // Scale down to reduce flow visibility

        // Side faces - flowing texture (flow downward)
        addQuadTextured(consumer, matrix,
                minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ,
                0, 0, -1, r, g, b, a, packedLight, flowSprite, height * textureScale);

        addQuadTextured(consumer, matrix,
                maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ,
                0, 0, 1, r, g, b, a, packedLight, flowSprite, height * textureScale);

        addQuadTextured(consumer, matrix,
                minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ,
                -1, 0, 0, r, g, b, a, packedLight, flowSprite, height * textureScale);

        addQuadTextured(consumer, matrix,
                maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ,
                1, 0, 0, r, g, b, a, packedLight, flowSprite, height * textureScale);

        poseStack.popPose();
    }

    private void addQuadTextured(VertexConsumer consumer, Matrix4f matrix,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float x3, float y3, float z3,
                                 float x4, float y4, float z4,
                                 float nx, float ny, float nz,
                                 float r, float g, float b, float a,
                                 int light,
                                 TextureAtlasSprite sprite,
                                 float textureRepeat) {
        // Use sprite's UV coordinates with scaling
        float u0 = sprite.getU0();
        float u1 = sprite.getU(textureRepeat * 16); // Scale texture
        float v0 = sprite.getV0();
        float v1 = sprite.getV(textureRepeat * 16);

        // Vertex order for downward flow (top vertices use v0, bottom use v1)
        consumer.vertex(matrix, x1, y1, z1)
                .color(r, g, b, a)
                .uv(u0, v1)  // Bottom-left (higher V = bottom of texture)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();

        consumer.vertex(matrix, x2, y2, z2)
                .color(r, g, b, a)
                .uv(u0, v0)  // Top-left (lower V = top of texture)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();

        consumer.vertex(matrix, x3, y3, z3)
                .color(r, g, b, a)
                .uv(u1, v0)  // Top-right
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();

        consumer.vertex(matrix, x4, y4, z4)
                .color(r, g, b, a)
                .uv(u1, v1)  // Bottom-right
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();
    }

    private static TextureAtlasSprite getWaterStillSprite() {
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("minecraft", "block/water_still"));
    }

    private static TextureAtlasSprite getWaterFlowSprite() {
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("minecraft", "block/water_flow"));
    }
}
