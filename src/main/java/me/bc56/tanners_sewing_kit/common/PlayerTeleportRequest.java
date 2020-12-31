package me.bc56.tanners_sewing_kit.common;

import java.time.Duration;
import java.time.LocalDateTime;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

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
            // TODO: Inform requester of expiration
            return;
        }

        BlockPos toPos = to.getBlockPos();
        from.refreshPositionAfterTeleport(toPos.getX(), toPos.getY(), toPos.getZ());
    }
}
