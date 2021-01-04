package me.bc56.tanners_sewing_kit.common;

import java.util.Map;

public interface HomeMixinAccess {
    public Map<String, PlayerHome> getHomes();
    public void addHome(PlayerHome home, String name);
    public PlayerHome getHome(String name);
}
