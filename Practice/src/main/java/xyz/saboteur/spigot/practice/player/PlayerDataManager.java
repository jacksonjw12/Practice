package xyz.saboteur.spigot.practice.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.practice.kit.Kit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager implements Listener {
    private Practice plugin;
    private Map<UUID, PlayerData> playerData;

    public PlayerDataManager(Practice plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        Bukkit.getOnlinePlayers().forEach(player -> this.playerData.put(player.getUniqueId(), new PlayerData(player)));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.playerData.put(player.getUniqueId(), new PlayerData(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!this.playerData.containsKey(uuid)) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            this.playerData.get(uuid).save();
            this.playerData.remove(uuid);
        }, 5L);
    }

    public void setKit(Player player, GameType gt, int position, Kit kit) {
        this.getPlayerData(player).setKit(gt, kit, position, true);
    }

    public int getRating(Player player, GameType gt) {
        return this.getPlayerData(player).getRating(gt);
    }

    public Kit[] getKits(Player player, GameType gt) {
        return this.getPlayerData(player).getKits().get(gt);
    }

    public Kit getKit(Player player, GameType gt, int position) {
        return this.getKits(player, gt)[position - 1];
    }

    public void removeKit(Player player, GameType gt, int position) {
        this.getPlayerData(player).removeKit(gt, position, true);
    }

    public void updateElo(Player player, GameType gt, int scoreChange) {
        PlayerData data = this.getPlayerData(player);
        data.setRating(gt, data.getRating(gt) + scoreChange, true);
    }

    public void saveKits(Player player) {
        this.getPlayerData(player).save();
    }

    public PlayerData getPlayerData(Player player) {
        return this.playerData.get(player.getUniqueId());
    }

    public void setupNewGameType(GameType gt) {
        this.playerData.values().forEach(data -> data.setRating(gt, 1000, true));
    }
}
