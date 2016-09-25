package xyz.saboteur.spigot.practice.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.saboteur.spigot.Practice;

public class PlayerEvents implements Listener {
    /*static int visibleDistance = Bukkit.getServer().getViewDistance() * 16;
    static int minVisible = 4096;*/
    private Practice plugin;

    public PlayerEvents(Practice plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.getMatchManager().isInMatch(player) && !this.plugin.getKitEditManager().isEditing(player)) {
            event.setCancelled(true);
            return;
        }
        event.getItemDrop().remove();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(this.plugin.getSpawn() == null) return;
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> event.getPlayer().teleport(this.plugin.getSpawn()), 5L);
    }

    @EventHandler(ignoreCancelled=true, priority= EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        refreshPlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        refreshPlayer(event.getPlayer());
    }

    public static void refreshPlayer(Player player) {
        for (Player ply : Bukkit.getOnlinePlayers()) {
            player.showPlayer(ply);
            ply.showPlayer(player);
        }
    }
}
