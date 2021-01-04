package me.bc56.tanners_sewing_kit.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.bc56.tanners_sewing_kit.TannersSewingKit;
import me.bc56.tanners_sewing_kit.common.HomeManager;
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
public class PlayerHomeMixin implements HomeMixinAccess {
    @Unique
    private Map<String, PlayerHome> homes = new HashMap<>();

    @Override
    @Unique
    public Map<String, PlayerHome> getHomes() {
        return homes;
    }

    @Override
    @Unique
	public void addHome(PlayerHome home, String name) {
        this.homes.put(name, home);
	}

    @Override
    @Unique
	public PlayerHome getHome(String name) {
		return homes.get(name);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    public void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.homes = ((HomeMixinAccess) oldPlayer).getHomes();
    }

    // If an old-style home exists, load it for conversion
    // TODO: Remove this on February 1st, 2021
    @Inject(method = "readCustomDataFromTag", at = @At("RETURN"))
    private void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Home", 10)) {
            TannersSewingKit.LOGGER.info("Player {} has a home in their playerdata .dat file; this will be corrected", ((ServerPlayerEntity)(Object)this).getName().asString());

            CompoundTag homeTag = tag.getCompound("Home");
            RegistryKey<World> dimKey = RegistryKey.of(Registry.DIMENSION, new Identifier(homeTag.getString("Dimension")));
            ServerWorld dimension = ((ServerPlayerEntity)(Object)this).getServer().getWorld(dimKey);

            if (dimension != null) {
                BlockPos pos = new BlockPos(homeTag.getInt("X"), homeTag.getInt("Y"), homeTag.getInt("Z"));
                
                this.addHome(new PlayerHome(dimension, pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F), HomeManager.DEFAULT_HOME_NAME);
            }
        }
    }
}
