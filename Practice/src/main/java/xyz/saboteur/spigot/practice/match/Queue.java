package xyz.saboteur.spigot.practice.match;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.util.PracticeTL;
import xyz.saboteur.spigot.util.PracticeUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Queue implements Listener {
    private GameType game;
    private boolean ranked;
    private Map<UUID, Integer> queue;
    private Map<UUID, UUID> awaitingMatch;

    public Queue(GameType game, boolean ranked) {
        this.game = game;
        this.ranked = ranked;
        this.queue = new HashMap<>();
        this.awaitingMatch = new HashMap<>();
    }

    public void addToQueue(final Player ply, final int rating) {
        this.queue.put(ply.getUniqueId(), rating);
        ply.getInventory().clear();
        ply.getInventory().setItem(0, PracticeUtil.generateItem(new ItemStack(Material.REDSTONE, 1), PracticeTL.ITEMS__LEAVE_QUEUE.get()));
        PracticeTL.QUEUE__JOINED.send(ply, "type", this.game.getDisplayName());
        if (this.ranked) {
            PracticeTL.YOUR_ELO.send(ply, "elo", rating);
            PracticeTL.QUEUE__SEARCHING_RANKED.send(ply, "elo-min", (rating-200), "elo-max", (rating+200));
            new BukkitRunnable() {
                int range, i;
                boolean maxRange;
                @Override
                public void run() {
                    if (!Queue.this.queue.containsKey(ply.getUniqueId())) {
                        this.cancel();
                        return;
                    }
                    int r = queue.get(ply.getUniqueId());
                    for (UUID uuid : Queue.this.queue.keySet()) {
                        if (rating - this.range > r || rating + this.range < r || uuid == ply.getUniqueId()) continue;
                        this.cancel();
                        Queue.this.queue.remove(ply.getUniqueId());
                        Queue.this.queue.remove(uuid);
                        Queue.this.awaitingMatch.put(ply.getUniqueId(), uuid);
                        return;
                    }
                    ++this.i;
                    if (!this.maxRange) {
                        if (this.i == 5) {
                            this.range += 50;
                            if (rating - this.range <= 0) {
                                this.maxRange = true;
                            }
                            PracticeTL.QUEUE__SEARCHING_RANKED.send(ply, "elo-min", (rating - this.range), "elo-max", (rating+this.range));
                            this.i = 0;
                        }
                    } else if (this.i % 5 == 0) {
                        PracticeTL.QUEUE_UNABLE.send(ply);
                        Queue.this.queue.remove(ply.getUniqueId());
                        Practice.get().getInventoryManager().setDefaultInventory(ply);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Practice.get(), 0L, 20L);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!queue.containsKey(ply.getUniqueId())) {
                        this.cancel();
                        return;
                    }
                    for (UUID uuid : Queue.this.queue.keySet()) {
                        if (uuid == ply.getUniqueId()) continue;
                        this.cancel();
                        queue.remove(ply.getUniqueId());
                        queue.remove(uuid);
                        awaitingMatch.put(ply.getUniqueId(), uuid);
                        return;
                    }
                }
            }.runTaskTimer(Practice.get(), 0L, 20L);
        }
    }

    public boolean hasMatch() {
        return this.awaitingMatch.size() > 0;
    }

    public void startMatch(Player ply) {
        this.awaitingMatch.remove(ply.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!this.queue.containsKey(uuid)) return;
        this.queue.remove(uuid);
    }

    @EventHandler
    public void onPlayerRightClickLeaveQueue(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().hasItemMeta() && event.getItem().getItemMeta().getDisplayName() != null && ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName()).equals(ChatColor.stripColor(PracticeTL.ITEMS__LEAVE_QUEUE.get())) && this.queue.containsKey(player.getUniqueId())) {
            this.queue.remove(player.getUniqueId());
            Practice.get().getInventoryManager().setDefaultInventory(player);
            PracticeTL.QUEUE_LEFT.send(player);
            Bukkit.getScheduler().runTaskLater(Practice.get(), () -> Practice.get().getInventoryManager().populateMenus(), 5L);
        }
    }

    public GameType getGame() {
        return this.game;
    }

    public boolean isRanked() {
        return this.ranked;
    }

    public Map<UUID, Integer> getQueue() {
        return this.queue;
    }

    public Map<UUID, UUID> getAwaitingMatch() {
        return this.awaitingMatch;
    }
}
