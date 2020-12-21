package net.arcanumverum.arcanelibraries.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Shamelessly stolen from Stacc: https://github.com/Devan-Kerman/Stacc/
@Mixin(ItemStack.class)
public abstract class ItemStackSerializationMixin {
    @Shadow private int count;

    @Shadow
    public abstract int getCount();

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V")
    void onDeserialization(CompoundTag tag, CallbackInfo callbackInformation) {
        if (tag.contains("countInteger")) {
            this.count = tag.getInt("countInteger");
        }
    }

    @Inject (at = @At ("TAIL"), method = "toTag(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;")
    void onSerialization(CompoundTag tag, CallbackInfoReturnable<CompoundTag> callbackInformationReturnable) {
        if (this.count > Byte.MAX_VALUE) {
            tag.putInt("countInteger", this.count);
            // make downgrading less painful
            tag.putByte("Count", (byte) 127);
        }
    }
}
