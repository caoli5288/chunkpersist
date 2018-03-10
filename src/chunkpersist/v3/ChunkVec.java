package chunkpersist.v3;

import lombok.Data;
import org.bukkit.Chunk;

@Data
public class ChunkVec {

    private final String world;
    private final int x;
    private final int z;

    public ChunkVec(Chunk chunk) {
        world = chunk.getWorld().getName();
        x = chunk.getX();
        z = chunk.getZ();
    }
}
