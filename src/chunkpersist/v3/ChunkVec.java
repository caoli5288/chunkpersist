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

    @Data
    public static class Expire {

        private final long period;
        private long nextGC;
        private int reload;

        public Expire calcNextGC() {
            ++reload;
            nextGC = System.currentTimeMillis() + (long) (period * Math.pow(1.2, reload));
            return this;
        }

        public static Expire next(long period) {
            Expire expire = new Expire(period);
            expire.nextGC = System.currentTimeMillis() + period;
            return expire;
        }
    }
}
