package me.bc56.tanners_sewing_kit.homes;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class PlayerHome {
    public final ServerWorld dimension;
    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public PlayerHome(ServerWorld dimension, double x, double y, double z, float yaw, float pitch) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    // Creates a PlayerHome at the specified player's current location
    public static PlayerHome createAt(ServerPlayerEntity player) {
        return new PlayerHome(player.getServerWorld(), player.getX(), player.getY(), player.getZ(), player.getYaw(1.0F), player.getPitch(1.0F));
    }
}
