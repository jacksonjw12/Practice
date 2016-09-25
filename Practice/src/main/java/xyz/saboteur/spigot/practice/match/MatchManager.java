package xyz.saboteur.spigot.practice.match;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.gametype.GameType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class MatchManager implements Listener {
    private Practice plugin;
    private List<Match> matches;
    private List<Queue> queues;

    public MatchManager(Practice plugin) {
        this.plugin = plugin;
        this.matches = new ArrayList<>();
        this.queues = new ArrayList<>();
        for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
            this.queues.add(new Queue(gt, true));
            this.queues.add(new Queue(gt, false));
        }
        this.queues.forEach(queue -> Bukkit.getPluginManager().registerEvents(queue, this.plugin));
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        new BukkitRunnable() {
            public void run() {
                MatchManager.this.queues.stream().filter(Queue::hasMatch).forEach(queue -> {
                    for (UUID uuid : queue.getAwaitingMatch().keySet()) {
                        Player player = Bukkit.getPlayer(uuid);
                        GameType gt = queue.getGame();
                        Match match = new Match(plugin.getArenaManager().getLeast(gt.getPossibleArenas()), gt, player, Bukkit.getPlayer(queue.getAwaitingMatch().get(player.getUniqueId())), queue.isRanked());
                        match.startMatch();
                        MatchManager.this.matches.add(match);
                        queue.startMatch(player);
                        MatchManager.this.plugin.getInventoryManager().populateMenus();
                    }
                });
            }
        }.runTaskTimer(this.plugin, 0, 5);
    }

    public void startMatch(Player ply, Player ply2, GameType gt, boolean ranked) {
        Match match = new Match(plugin.getArenaManager().getLeast(gt.getPossibleArenas()), gt, ply, ply2, ranked);
        match.startMatch();
        this.matches.add(match);
    }

    private Queue getQueue(Predicate<Queue> test) {
        for (Queue queue : this.queues) {
            if (!test.test(queue)) continue;
            return queue;
        }
        return null;
    }

    private Queue getQueue(GameType gt, boolean ranked) {
        return this.getQueue(queue -> queue.isRanked() == ranked && queue.getGame() == gt);
    }

    public void addToQueue(Player ply, GameType gt, boolean ranked) {
        this.getQueue(gt, ranked).addToQueue(ply, this.plugin.getPlayerDataManager().getRating(ply, gt));
    }

    public int getAmountInQueue(GameType gt, boolean ranked) {
        return this.getQueue(gt, ranked).getQueue().keySet().size();
    }

    public int getAmountInMatch(GameType gt, boolean ranked) {
        return this.getMatches(match -> match.getGameType() == gt && match.isRanked() == ranked).size() * 2;
    }

    public Match getMatch(Predicate<Match> test) {
        for (Match match : this.matches) {
            if (!test.test(match)) continue;
            return match;
        }
        return null;
    }

    public List<Match> getMatches(Predicate<Match> test) {
        ArrayList<Match> matches = new ArrayList<>();
        for (Match match : this.matches) {
            if (!test.test(match)) continue;
            matches.add(match);
        }
        return matches;
    }

    public List<Match> getMatches() {
        return this.matches;
    }

    public Match getMatch(Player ply) {
        return this.getMatch(match -> match.hasPlayer(ply));
    }

    public GameType getGameType(Player ply) {
        return this.getMatch(ply).getGameType();
    }

    public boolean isInMatch(Player ply) {
        return this.getMatch(ply) != null;
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (!this.isInMatch(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && !this.isInMatch((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!this.isInMatch((Player)event.getEntity())) {
            event.setCancelled(true);
        }
    }

    public void endMatch(Match match) {
        this.matches.remove(match);
        this.plugin.getInventoryManager().populateMenus();
    }
}
