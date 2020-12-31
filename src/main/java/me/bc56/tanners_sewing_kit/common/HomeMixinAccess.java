package me.bc56.tanners_sewing_kit.common;

public interface HomeMixinAccess {
    public void setHome(PlayerHome position);
    public PlayerHome getHome();

    public int teleportToHome();
}
