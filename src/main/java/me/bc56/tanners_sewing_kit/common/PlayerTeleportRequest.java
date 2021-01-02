package me.bc56.tanners_sewing_kit.common;

import java.time.Duration;
import java.time.LocalDateTime;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerTeleportRequest {
    ServerPlayerEntity from;
    ServerPlayerEntity to;

    LocalDateTime dateRequested;
    Duration timeout;
    LocalDateTime dateExpires;

    public PlayerTeleportRequest(ServerPlayerEntity from, ServerPlayerEntity to, LocalDateTime dateRequested, Duration timeout) {
        this.from = from;
        this.to = to;

        this.dateRequested = dateRequested;
        this.timeout = timeout;

        dateExpires = dateRequested.plus(timeout);
    }

    public static PlayerTeleportRequest create(ServerPlayerEntity from, ServerPlayerEntity to) {
        return new PlayerTeleportRequest(from, to, LocalDateTime.now(), Duration.ofSeconds(20)); // TODO: Get timeout from mod config
    }

    public static PlayerTeleportRequest createAndSet(ServerPlayerEntity from, ServerPlayerEntity to) {
        PlayerTeleportRequest request = create(from, to);

        ((TeleportMixinAccess) from).setOutgoingRequest(request);
        ((TeleportMixinAccess) to).setIncomingRequest(request);

        return request;
    }

    public void execute() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (currentDateTime.isAfter(dateExpires)) {
            // Lazy expiration
            
            TeleportMixinAccess from2 = ((TeleportMixinAccess)from);
            if (from2.getOutgoingRequest() == this) {
                from2.setOutgoingRequest(null);
            }

            TeleportMixinAccess to2 = ((TeleportMixinAccess)to);
            if (to2.getIncomingRequest() == this) {
                to2.setIncomingRequest(null);
            }

            return;
        }

        from.teleport(to.getServerWorld(), to.getX(), to.getY(), to.getZ(), to.getYaw(1.0F), to.getPitch(1.0F));
    }
}
