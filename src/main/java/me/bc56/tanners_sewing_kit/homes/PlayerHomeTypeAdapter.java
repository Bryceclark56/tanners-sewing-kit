package me.bc56.tanners_sewing_kit.homes;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import me.bc56.tanners_sewing_kit.TannersSewingKit;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class PlayerHomeTypeAdapter extends TypeAdapter<PlayerHome> {

    @Override
    public void write(JsonWriter out, PlayerHome home) throws IOException {
        out.beginObject();
            out.name("dimension").value(home.dimension.getRegistryKey().getValue().toString());
            out.name("xyz").beginArray();
                out.value(home.x);
                out.value(home.y);
                out.value(home.z);
            out.endArray();
            out.name("yaw").value(home.yaw);
            out.name("pitch").value(home.pitch);
        out.endObject();
    }

    @Override
    public PlayerHome read(JsonReader in) throws IOException {
        ServerWorld dimension = null;
        double x = 0.0, y = 0.0, z = 0.0;
        float yaw = 0.0F, pitch = 0.0F;

        in.beginObject();
        in.nextName();
        RegistryKey<World> dimKey = RegistryKey.of(Registry.DIMENSION, new Identifier(in.nextString()));
        dimension = TannersSewingKit.server.getWorld(dimKey);

        in.nextName();
        in.beginArray();
            x = in.nextDouble();
            y = in.nextDouble();
            z = in.nextDouble();
        in.endArray();

        in.nextName();
        yaw = (float)in.nextDouble();
        
        in.nextName();
        pitch = (float)in.nextDouble();
        in.endObject();

        /*in.beginObject();
        while (in.hasNext()) {
            switch(in.nextName()) {
                case "dimension": {
                    RegistryKey<World> dimKey = RegistryKey.of(Registry.DIMENSION, new Identifier(in.nextString()));
                    dimension = HomeManager.server.getWorld(dimKey);
                }; break;
                case "xyz": {
                    in.beginArray();
                    x = in.nextDouble();
                    y = in.nextDouble();
                    z = in.nextDouble();
                    in.endArray();
                }; break;
                case "yaw": {
                    yaw = (float)in.nextDouble();
                }; break;
                case "pitch": {
                    pitch = (float)in.nextDouble();
                }; break;
            }
        }*/

        return new PlayerHome(dimension, x, y, z, yaw, pitch);
    }
    
}
