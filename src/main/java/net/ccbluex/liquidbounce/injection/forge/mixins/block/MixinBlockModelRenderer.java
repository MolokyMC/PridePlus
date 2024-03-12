/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.block;

import net.ccbluex.liquidbounce.Pride;
import net.ccbluex.liquidbounce.features.module.modules.render.XRay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

    @Inject(method = "renderModelSmooth", at = @At("HEAD"), cancellable = true)
    public void renderModelSmooth(IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> cir) {
        final XRay xray = (XRay) Pride.moduleManager.getModule(XRay.class);

        if (Objects.requireNonNull(xray).getState() && !xray.getXrayBlocks().contains(stateIn.getBlock()))
            cir.setReturnValue(false);
    }

    @Inject(method = "renderModelFlat", at = @At("HEAD"), cancellable = true)
    private void renderModelStandard(IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, boolean checkSides, long rand, final CallbackInfoReturnable<Boolean> booleanCallbackInfoReturnable) {
        final XRay xray = (XRay) Pride.moduleManager.getModule(XRay.class);

        if (Objects.requireNonNull(xray).getState() && !xray.getXrayBlocks().contains(stateIn.getBlock()))
            booleanCallbackInfoReturnable.setReturnValue(false);
    }
}
