package dev.sharedeye.mixin;

import dev.sharedeye.Freecam;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the body still while the camera is detached. */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Shadow public float movementForward;
    @Shadow public float movementSideways;
    @Shadow public boolean jumping;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sharedeye$freezeWhileFreecamming(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        if (!Freecam.isActive()) {
            return;
        }
        this.movementForward = 0.0f;
        this.movementSideways = 0.0f;
        this.jumping = false;
    }
}
