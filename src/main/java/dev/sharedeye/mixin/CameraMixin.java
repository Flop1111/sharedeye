package dev.sharedeye.mixin;

import dev.sharedeye.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "update", at = @At("TAIL"))
    private void sharedeye$applyFreecam(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                        boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!Freecam.isActive()) {
            return;
        }
        Camera self = (Camera) (Object) this;
        Vec3d pos = Freecam.pos();
        self.setPos(pos.x, pos.y, pos.z);
        self.setRotation(Freecam.yaw(), Freecam.pitch());
    }
}
