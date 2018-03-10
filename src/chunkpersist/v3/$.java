package chunkpersist.v3;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * This plugin to avoid too many chunk unload request lag the server.
 */
public class $ extends JavaPlugin implements Listener {

    private final Map<ChunkVec, Long> all = new HashMap<>();
    private long chunkgcperiod;

    private int chunkload;
    private int chunkunload;
    private int actuallychunkunload;

    public void onEnable() {
        saveDefaultConfig();
        chunkgcperiod = getConfig().getInt("chunk_gc_period", 300) * 1000L;

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, this::loginfo, 1200, 1200);
    }

    private void loginfo() {
        getLogger().info("chunkload=" + chunkload + ", chunkunload=" + actuallychunkunload + ":" + chunkunload + ", chunkloaded=" + all.size());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        chunkload++;
        all.put(new ChunkVec(event.getChunk()), System.currentTimeMillis());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        chunkunload++;
        ChunkVec vec = new ChunkVec(event.getChunk());
        if (all.containsKey(vec) && all.get(vec) + chunkgcperiod > System.currentTimeMillis()) {
            event.setCancelled(true);
            return;
        }
        actuallychunkunload++;
        all.remove(vec);
    }

}
