package me.bc56.tanners_sewing_kit.common;

public interface TeleportMixinAccess {
    PlayerTeleportRequest getIncomingRequest();
    void setIncomingRequest(PlayerTeleportRequest request);

    PlayerTeleportRequest getOutgoingRequest();
    void setOutgoingRequest(PlayerTeleportRequest request);
}
