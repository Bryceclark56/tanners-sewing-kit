package me.bc56.tanners_sewing_kit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public interface ServerWorldSleepMixin {
    @Invoker("resetWeather")
    public void invokeResetWeather();
}
