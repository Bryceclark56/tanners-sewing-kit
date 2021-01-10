package me.bc56.tanners_sewing_kit.common;

import net.minecraft.server.world.ServerWorld;

public class PlayerLocation {
    public final double x;
    public final double y;
    public final double z;

    public final ServerWorld dimension;

    public PlayerLocation(ServerWorld dimension, double x, double y, double z) {
        this.dimension = dimension;

        this.x = x;
        this.y = y;
        this.z = z;
    }
}
