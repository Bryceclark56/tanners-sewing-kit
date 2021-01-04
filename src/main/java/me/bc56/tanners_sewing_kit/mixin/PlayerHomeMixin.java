package me.bc56.tanners_sewing_kit.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.bc56.tanners_sewing_kit.common.HomeMixinAccess;
import me.bc56.tanners_sewing_kit.common.PlayerHome;
import net.minecraft.server.network.ServerPlayerEntity;

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
}
