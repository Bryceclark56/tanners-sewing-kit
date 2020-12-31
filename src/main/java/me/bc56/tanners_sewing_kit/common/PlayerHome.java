package me.bc56.tanners_sewing_kit.common;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PlayerHome {
    public final ServerWorld dimension;
    public final BlockPos pos;

    public PlayerHome(ServerWorld dimension, BlockPos position) {
        this.dimension = dimension;
        this.pos = position;
    }

    // Creates a PlayerHome at the specified player's current location
    public static PlayerHome createAt(ServerPlayerEntity player) {
        return new PlayerHome(player.getServerWorld(), player.getBlockPos());
    }
}
