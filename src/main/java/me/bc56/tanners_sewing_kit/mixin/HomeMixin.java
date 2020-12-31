package me.bc56.tanners_sewing_kit.mixin;

import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.bc56.tanners_sewing_kit.common.HomeMixinAccess;
import me.bc56.tanners_sewing_kit.common.PlayerHome;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public class HomeMixin implements HomeMixinAccess {
    @Unique
    private PlayerHome home;

    @Override
    @Unique
	public void setHome(PlayerHome home) {
        this.home = home;
	}

    @Override
    @Unique
	public PlayerHome getHome() {
		return home;
	}

    @Override
    @Unique
    @Intrinsic
    public int teleportToHome() {
        if (home == null) {
            return 1; // Home not set
        }

        BlockPos pos = home.pos;
        ServerPlayerEntity player = ((ServerPlayerEntity)(Object)this);

        player.teleport(home.dimension, pos.getX(), pos.getY(), pos.getZ(), player.yaw, player.pitch); //TODO: Save player yaw and pitch
        player.networkHandler.syncWithPlayerPosition();

        return 0;
    }

    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Home", 10)) {
            CompoundTag homeTag = tag.getCompound("Home");
            RegistryKey<World> dimKey = RegistryKey.of(Registry.DIMENSION, new Identifier(homeTag.getString("Dimension")));
            ServerWorld dimension = ((ServerPlayerEntity)(Object)this).getServer().getWorld(dimKey);

            if (dimension != null) {
                BlockPos pos = new BlockPos(homeTag.getInt("X"), homeTag.getInt("Y"), homeTag.getInt("Z"));
                
                home = new PlayerHome(dimension, pos);
            }
        }
    }

    @Inject(method = "writeCustomDataToTag", at = @At("RETURN"))
    private void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        if (home != null) {
            CompoundTag homeTag = new CompoundTag();
            homeTag.putString("Dimension", home.dimension.getRegistryKey().getValue().toString());
            homeTag.putInt("X", home.pos.getX());
            homeTag.putInt("Y", home.pos.getY());
            homeTag.putInt("Z", home.pos.getZ());

            tag.put("Home", homeTag);
        }
    }
}
