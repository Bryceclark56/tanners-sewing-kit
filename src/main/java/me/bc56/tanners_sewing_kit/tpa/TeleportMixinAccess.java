package me.bc56.tanners_sewing_kit.tpa;

import me.bc56.tanners_sewing_kit.common.PlayerLocation;

public interface TeleportMixinAccess {
    PlayerTeleportRequest getIncomingRequest();
    void setIncomingRequest(PlayerTeleportRequest request);

    PlayerTeleportRequest getOutgoingRequest();
    void setOutgoingRequest(PlayerTeleportRequest request);

    PlayerLocation getLastLocation();
    void setLastLocation(PlayerLocation location);
}
