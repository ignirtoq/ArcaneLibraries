package net.arcanumverum.arcanelibraries.mixins;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Shamelessly stolen from Stacc: https://github.com/Devan-Kerman/Stacc/
@Mixin(PacketByteBuf.class)
public abstract class BufStackDesyncMixin {
    @Shadow
    public abstract int readInt();

    @Shadow
    public abstract ByteBuf writeInt(int i);

    @Inject(method = "writeItemStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/network/PacketByteBuf;",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/PacketByteBuf;writeCompoundTag(Lnet/minecraft/nbt/CompoundTag;)"
                             + "Lnet/minecraft/network" + "/PacketByteBuf;"))
    private void write(ItemStack itemStack, CallbackInfoReturnable<PacketByteBuf> cir) {
        this.writeInt(itemStack.getCount());
    }

    @ModifyArg(method = "readItemStack",
            at = @At (value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;<init>(Lnet/minecraft/item/ItemConvertible;I)V"),
            index = 1)
    private int doThing(int amount) {
        return this.readInt();
    }
}
