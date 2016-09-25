package xyz.saboteur.spigot.practice.match;

import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.arena.Arena;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.scoreboard.ScoreboardLayout;
import xyz.saboteur.spigot.util.EntityHider;
import xyz.saboteur.spigot.util.PracticeTL;

public class Match implements Listener {
    private Practice plugin;
    private Arena arena;
    private GameType gameType;
    private Player player1, player2;
    private boolean started, ranked;
    private long startTime;

    public Match(Arena arena, GameType gameType, Player p1, Player p2, boolean ranked) {
        this.plugin = Practice.get();
        this.arena = arena;
        this.gameType = gameType;
        this.player1 = p1;
        this.player2 = p2;
        this.started = false;
        this.ranked = ranked;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void startMatch() {
        this.player1.teleport(this.arena.getSpawns(0).get(0));
        this.player2.teleport(this.arena.getSpawns(1).get(0));
        plugin.getPracticeScoreboard().setLayoutFor(player1, ScoreboardLayout.MATCH_SOLO);
        plugin.getPracticeScoreboard().setLayoutFor(player2, ScoreboardLayout.MATCH_SOLO);
        Bukkit.getOnlinePlayers().stream().filter(player -> !(player.equals(player1) || player.equals(player2))).forEach(player -> {
            player.hidePlayer(player1);
            player.hidePlayer(player2);
            player1.hidePlayer(player);
            player2.hidePlayer(player2);
        });
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            player1.showPlayer(player2);
            player2.showPlayer(player1);
        }, 5);
        if(ranked) {
            PracticeTL.MATCH__FOUND_RANKED.send(player1, "name", player2.getName(), "type", gameType.getDisplayName(), "elo", plugin.getPlayerDataManager().getRating(player2, gameType));
            PracticeTL.MATCH__FOUND_RANKED.send(player2, "name", player1.getName(), "type", gameType.getDisplayName(), "elo", plugin.getPlayerDataManager().getRating(player1, gameType));
        } else {
            PracticeTL.MATCH__FOUND_UNRANKED.send(player1, "name", player2.getName(), "type", gameType.getDisplayName(), "elo", plugin.getPlayerDataManager().getRating(player2, gameType));
            PracticeTL.MATCH__FOUND_UNRANKED.send(player2, "name", player1.getName(), "type", gameType.getDisplayName(), "elo", plugin.getPlayerDataManager().getRating(player1, gameType));
        }
        PracticeTL.MATCH__MAP.send(player1, "name", this.arena.getName(), "player", this.arena.getBuilder());
        PracticeTL.MATCH__MAP.send(player2, "name", this.arena.getName(), "player", this.arena.getBuilder());
        this.player1.getInventory().clear();
        this.player2.getInventory().clear();
        this.plugin.getInventoryManager().showKits(this.player1, this.gameType);
        this.plugin.getInventoryManager().showKits(this.player2, this.gameType);
        new BukkitRunnable() {
            int i = 5;
            public void run() {
                if (this.i == 0) {
                    this.cancel();
                    Match.this.started = true;
                    for (Player ply : new Player[]{Match.this.player1, Match.this.player2}) {
                        if (ply == null) {
                            this.cancel();
                            return;
                        }
                        PracticeTL.MATCH__STARTED.send(ply);
                        ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 2.0f);
                        startTime = System.currentTimeMillis();
                    }
                    return;
                } else {
                    for (Player ply : new Player[]{Match.this.player1, Match.this.player2}) {
                        if (ply == null) {
                            this.cancel();
                            return;
                        }
                        PracticeTL.MATCH__STARTING_TIME.send(ply, "seconds", this.i, "second-s", this.i == 1 ? "" : "s");
                        ply.playSound(ply.getLocation(), Sound.NOTE_PIANO, 10.0f, 1.0f);
                    }
                }
                --this.i;
            }
        }.runTaskTimer(this.plugin, 30L, 20L);
    }

    public void endMatch(Player winner) {
        for (Player ply3 : new Player[]{this.player1, this.player2}) {
            this.plugin.getInventoryManager().setDefaultInventory(ply3);
            ply3.teleport(this.plugin.getSpawn());
            ply3.setHealth(20);
            ply3.setFoodLevel(20);
            ply3.getActivePotionEffects().clear();
        }
        new BukkitRunnable(){
            public void run() {
                for (Player ply : Bukkit.getOnlinePlayers()) {
                    if (ply == null) continue;
                    if (ply != Match.this.player1 && Match.this.player1 != null) {
                        ply.showPlayer(Match.this.player1);
                        Match.this.player1.showPlayer(ply);
                    }
                    if (ply == Match.this.player2 || Match.this.player2 == null) continue;
                    ply.showPlayer(Match.this.player2);
                    Match.this.player2.showPlayer(ply);
                }
            }
        }.runTaskLater(this.plugin, 5);
        for (Player ply : new Player[]{this.player1, this.player2}) {
            ply.sendMessage(ChatColor.YELLOW + "Winner: " + winner.getName());
            new FancyMessage("Inventories (click to view): ").color(ChatColor.GOLD).then(player1.getName()).color(ChatColor.YELLOW).command("/inventory " + player1.getName()).then(", ").then(player2.getName()).color(ChatColor.YELLOW).command("/inventory " + player2.getName()).send(ply);
        }
        if (this.ranked) {
            Player loser;
            double p1 = this.plugin.getPlayerDataManager().getRating(this.player1, this.gameType);
            double p2 = this.plugin.getPlayerDataManager().getRating(this.player2, this.gameType);
            int scoreChange;
            double expectedp1 = 1.0 / (1.0 + Math.pow(10.0, (p1 - p2) / 400.0));
            double expectedp2 = 1.0 / (1.0 + Math.pow(10.0, (p2 - p1) / 400.0));
            if (winner == this.player1) {
                scoreChange = (int)(expectedp1 * 32.0);
                loser = this.player2;
            } else {
                scoreChange = (int)(expectedp2 * 32.0);
                loser = this.player1;
            }
            scoreChange = scoreChange > 25 ? 25 : scoreChange;
            this.plugin.getPlayerDataManager().updateElo(winner, this.gameType, scoreChange);
            this.plugin.getPlayerDataManager().updateElo(loser, this.gameType, -scoreChange);
            PracticeTL.MATCH__ELO_CHANGES.send(this.player1, "winner", winner.getName(), "winner-change", scoreChange, "winner-elo", this.plugin.getPlayerDataManager().getRating(winner, this.gameType), "loser", loser.getName(), "loser-change", scoreChange, this.plugin.getPlayerDataManager().getRating(loser, this.gameType));
            PracticeTL.MATCH__ELO_CHANGES.send(this.player2, "winner", winner.getName(), "winner-change", scoreChange, "winner-elo", this.plugin.getPlayerDataManager().getRating(winner, this.gameType), "loser", loser.getName(), "loser-change", scoreChange, this.plugin.getPlayerDataManager().getRating(loser, this.gameType));
        }
        this.plugin.getMatchManager().endMatch(this);
        plugin.getPracticeScoreboard().setLayoutFor(player1, ScoreboardLayout.IDLE_SOLO);
        plugin.getPracticeScoreboard().setLayoutFor(player2, ScoreboardLayout.IDLE_SOLO);
        this.player1 = null;
        this.player2 = null;
        this.arena = null;
        this.gameType = null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == this.player1) {
            this.endMatch(this.player2);
        } else if (event.getPlayer() == this.player2) {
            this.endMatch(this.player1);
        }
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent event) {
        if (this.player1 == event.getEntity() || this.player2 == event.getEntity()) {
            event.setDeathMessage(null);
            EntityHider hider = this.plugin.getEntityHider();
            for (ItemStack item : event.getDrops()) {
                final Item i = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), item);
                for (Player ply : Bukkit.getOnlinePlayers()) {
                    if (ply == this.player1 || ply == this.player2) continue;
                    hider.hideEntity(ply, i);
                }
                Bukkit.getScheduler().runTaskLater(this.plugin, i::remove, 60);
            }
            event.getDrops().clear();
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                event.getEntity().spigot().respawn();
                if (event.getEntity() == Match.this.player1) {
                    this.endMatch(Match.this.player2);
                } else {
                    this.endMatch(Match.this.player1);
                }
                this.started = false;
            }, 2);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && (this.player1 == event.getEntity() || this.player2 == event.getEntity())) {
            if (!this.started) {
                event.setCancelled(true);
            } else if (event.getDamage() >= ((Player)event.getEntity()).getHealth()) {
                this.plugin.getInventoryManager().storeInv(this.player1, event.getEntity() == this.player1);
                this.plugin.getInventoryManager().storeInv(this.player2, event.getEntity() == this.player2);
            }
        }
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        if (event.getPlayer() == this.player1 || event.getPlayer() == this.player2) {
            EntityHider hider = this.plugin.getEntityHider();
            for (Player ply : Bukkit.getOnlinePlayers()) {
                if (ply == this.player1 || ply == this.player2) continue;
                hider.hideEntity(ply, event.getItemDrop());
            }
            Bukkit.getScheduler().runTaskLater(this.plugin, event.getItemDrop()::remove, 60);
        }
    }

    @EventHandler
    public void onThrowItem(ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.SPLASH_POTION) {
            EntityHider hider = this.plugin.getEntityHider();
            if (event.getEntity().getShooter() == this.player1 || event.getEntity().getShooter() == this.player2) {
                for (Player ply : Bukkit.getOnlinePlayers()) {
                    if (ply == this.player1 || ply == this.player2) continue;
                    hider.hideEntity(ply, event.getEntity());
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplashEvent(PotionSplashEvent event) {
        if (event.getEntity().getShooter() == this.player1 || event.getEntity().getShooter() == this.player2) {
            event.getAffectedEntities().stream().filter(entity -> entity != this.player1 && entity != this.player2).forEach(event.getAffectedEntities()::remove);
            event.setCancelled(true);
            event.getAffectedEntities().stream().filter(entity -> entity == this.player1 || entity == this.player2).forEach(entity -> entity.addPotionEffects(event.getEntity().getEffects()));
        }
    }

    public boolean hasPlayer(Player ply) {
        return this.player1 == ply || this.player2 == ply;
    }

    public GameType getGameType() {
        return this.gameType;
    }

    public Arena getArena() {
        return arena;
    }

    public boolean isRanked() {
        return this.ranked;
    }

    public Player getOpponent(Player player) {
        return player == player1 ? player2 : player1;
    }

    public int getDuration() {
        return started ? 0 : (int)((System.currentTimeMillis() - startTime)/1000);
    }
}
