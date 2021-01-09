package me.bc56.tanners_sewing_kit.tpa;

public interface TeleportMixinAccess {
    PlayerTeleportRequest getIncomingRequest();
    void setIncomingRequest(PlayerTeleportRequest request);

    PlayerTeleportRequest getOutgoingRequest();
    void setOutgoingRequest(PlayerTeleportRequest request);
}
