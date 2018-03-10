package chunkpersist.v3;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This plugin to avoid too many chunk unload request lag the server.
 */
public class $ extends JavaPlugin implements Listener {

    private final Map<ChunkVec, ChunkVec.Expire> all = new HashMap<>();
    private Cache<ChunkVec, ChunkVec.Expire> queued;

    private int chunkgcperiod;

    private int chunkload;
    private int chunkunload;
    private int actuallychunkunload;

    public void onEnable() {
        saveDefaultConfig();
        chunkgcperiod = getConfig().getInt("chunk_gc_period", 300) * 1000;
        queued = CacheBuilder.newBuilder().expireAfterWrite(chunkgcperiod, TimeUnit.MILLISECONDS).build();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::loginfo, 1200, 1200);
    }

    private void loginfo() {
        getLogger().info("chunkload=" + chunkload + ", chunkunload=" + actuallychunkunload + ":" + chunkunload + ", chunkloaded=" + all.size());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        chunkload++;
        ChunkVec vec = new ChunkVec(event.getChunk());
        all.put(vec, queued.asMap().containsKey(vec) ? queued.asMap().remove(vec).calcNextGC() : ChunkVec.Expire.next(chunkgcperiod));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        chunkunload++;
        ChunkVec vec = new ChunkVec(event.getChunk());
        if (all.containsKey(vec) && all.get(vec).getNextGC() > System.currentTimeMillis()) {
            event.setCancelled(true);
            return;
        }
        actuallychunkunload++;
        queued.put(vec, all.remove(vec));
    }

}
