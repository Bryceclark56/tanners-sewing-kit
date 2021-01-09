package me.bc56.tanners_sewing_kit.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import me.bc56.tanners_sewing_kit.tpa.PlayerTeleportRequest;
import me.bc56.tanners_sewing_kit.tpa.TeleportMixinAccess;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class TeleportMixin implements TeleportMixinAccess {
    @Unique
    private PlayerTeleportRequest lastOutgoingRequest;

    @Unique
    private PlayerTeleportRequest lastIncomingRequest;

    @Override
    public PlayerTeleportRequest getIncomingRequest() {
        return lastIncomingRequest;
    }

    @Override
    public void setIncomingRequest(PlayerTeleportRequest request) {
        lastIncomingRequest = request;
    }

    @Override
    public PlayerTeleportRequest getOutgoingRequest() {
        return lastOutgoingRequest;
    }

    @Override
    public void setOutgoingRequest(PlayerTeleportRequest request) {
        lastOutgoingRequest = request;
    }
}
