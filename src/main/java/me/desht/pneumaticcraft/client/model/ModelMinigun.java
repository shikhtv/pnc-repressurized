// Date: 30-6-2015 15:38:53
// Template version 1.1
// Java generated by Techne
// Keep in mind that you still need to fill in some blanks
// - ZeuX

package me.desht.pneumaticcraft.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.desht.pneumaticcraft.client.util.RenderUtils;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Used in three different places:
 * 1. Drone's minigun (in DroneMinigunLayer)
 * 2. ISTER for the minigun item (RenderItemMinigun)
 * 3. Sentry turrent TER model (RenderSentryTurret)
 */
public class ModelMinigun {
    //fields
    private final ModelRenderer barrel;
    private final ModelRenderer support1;
    private final ModelRenderer support2;
    private final ModelRenderer support3;
    private final ModelRenderer support4;
    private final ModelRenderer support5;
    private final ModelRenderer main;
    private final ModelRenderer magazine;
    private final ModelRenderer mount;
    private final ModelRenderer magazineColor;

    public ModelMinigun() {
        barrel = new ModelRenderer(64, 32, 30, 15);
        barrel.addBox(-0.5F, 1.5F, 0F, 1, 1, 16);
        barrel.setRotationPoint(0F, 20.96667F, -8F);
        barrel.mirror = true;
        support1 = new ModelRenderer(64, 32, 0, 0);
        support1.addBox(-1.5F, -1.5F, 0F, 3, 3, 1);
        support1.setRotationPoint(0F, 21F, -6F);
        support1.mirror = true;
        support2 = new ModelRenderer(64, 32, 0, 4);
        support2.addBox(-1F, 1.5F, 0F, 2, 1, 1);
        support2.setRotationPoint(0F, 21F, -6F);
        support2.mirror = true;
        support3 = new ModelRenderer(64, 32, 0, 6);
        support3.addBox(-1F, -2.5F, 0F, 2, 1, 1);
        support3.setRotationPoint(0F, 21F, -6F);
        support3.mirror = true;
        support4 = new ModelRenderer(64, 32, 0, 8);
        support4.addBox(1.5F, -1F, 0F, 1, 2, 1);
        support4.setRotationPoint(0F, 21F, -6F);
        support4.mirror = true;
        support5 = new ModelRenderer(64, 32, 0, 11);
        support5.addBox(-2.5F, -1F, 0F, 1, 2, 1);
        support5.setRotationPoint(0F, 21F, -6F);
        support5.mirror = true;
        main = new ModelRenderer(64, 32, 36, 0);
        main.addBox(0F, 0F, 0F, 6, 6, 8);
        main.setRotationPoint(-3F, 18F, 8F);
        main.mirror = true;
        magazine = new ModelRenderer(64, 32, 0, 14);
        magazine.addBox(0F, 0F, 0F, 2, 3, 6);
        magazine.setRotationPoint(3F, 22F, 9F);
        magazine.mirror = true;
        mount = new ModelRenderer(64, 32, 0, 23);
        mount.addBox(0F, 0F, 0F, 2, 4, 2);
        mount.setRotationPoint(-1F, 15F, 11F);
        mount.mirror = true;
        magazineColor = new ModelRenderer(64, 32, 8, 0);
        magazineColor.addBox(0F, 0F, 0F, 1, 2, 4);
        magazineColor.setRotationPoint(4.3F, 22.5F, 10F);
        magazineColor.mirror = true;
    }

    public void renderMinigun(MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay, Minigun minigun, float partialTick, boolean renderMount) {
        IVertexBuilder builder = buffer.getBuffer(RenderType.getEntityCutout(Textures.MODEL_DRONE_MINIGUN));
        matrixStack.push();

        if (renderMount) {
            matrixStack.push();
            matrixStack.translate(0, 5 / 16D, -12 / 16D);
            mount.render(matrixStack, builder, combinedLight, combinedOverlay);
            matrixStack.pop();
        }

        float barrelRotation = 0;
        if (minigun != null) {
            barrelRotation = minigun.getOldMinigunRotation() + partialTick * (minigun.getMinigunRotation() - minigun.getOldMinigunRotation());
            float yaw = minigun.oldMinigunYaw + partialTick * (minigun.minigunYaw - minigun.oldMinigunYaw);
            float pitch = minigun.oldMinigunPitch + partialTick * (minigun.minigunPitch - minigun.oldMinigunPitch);

            matrixStack.translate(0, 23 / 16D, 0);
            matrixStack.rotate(Vector3f.YP.rotationDegrees(yaw));
            matrixStack.rotate(Vector3f.XP.rotationDegrees(pitch));
            matrixStack.translate(0, -18 / 16D, -12 / 16D);
        }
        barrel.rotateAngleY = 0;
        barrel.rotateAngleX = 0;
        for (int i = 0; i < 6; i++) {
            barrel.rotateAngleZ = (float) (Math.PI / 3 * i) + barrelRotation;
            barrel.render(matrixStack, builder, combinedLight, combinedOverlay);
        }
        support1.rotateAngleZ = barrelRotation;
        support2.rotateAngleZ = barrelRotation;
        support3.rotateAngleZ = barrelRotation;
        support4.rotateAngleZ = barrelRotation;
        support5.rotateAngleZ = barrelRotation;
        support1.render(matrixStack, builder, combinedLight, combinedOverlay);
        support2.render(matrixStack, builder, combinedLight, combinedOverlay);
        support3.render(matrixStack, builder, combinedLight, combinedOverlay);
        support4.render(matrixStack, builder, combinedLight, combinedOverlay);
        support5.render(matrixStack, builder, combinedLight, combinedOverlay);
        magazine.render(matrixStack, builder, combinedLight, combinedOverlay);
        main.render(matrixStack, builder, combinedLight, combinedOverlay);

        float[] cols = RenderUtils.decomposeColorF(minigun != null ? 0xFF000000 | minigun.getAmmoColor() : 0xFF313131);
        magazineColor.render(matrixStack, builder, combinedLight, combinedOverlay, cols[1], cols[2], cols[3], cols[0]);

        matrixStack.pop();
    }
}
