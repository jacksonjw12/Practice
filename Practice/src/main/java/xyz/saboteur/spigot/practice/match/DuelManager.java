package xyz.saboteur.spigot.practice.match;

import com.sk89q.intake.Command;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.util.ItemMenu;
import xyz.saboteur.spigot.util.PracticeTL;
import xyz.saboteur.spigot.util.cmd.CommandBase;
import xyz.saboteur.spigot.util.cmd.Sender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager extends CommandBase implements Listener {
    private Practice plugin;
    private ItemMenu duelMenu;
    private Map<UUID, UUID> pickingGameMode, awaitingReply;
    private Map<UUID, GameType> waitingForReply;

    public DuelManager(Practice plugin) {
        super(false);
        this.plugin = plugin;
        this.pickingGameMode = new HashMap<>();
        this.awaitingReply = new HashMap<>();
        this.waitingForReply = new HashMap<>();
        this.setupMenu();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        if(event.getInventory().getName().equals("Select PvP Style") && this.pickingGameMode.containsKey(event.getPlayer().getUniqueId())) {
            this.pickingGameMode.remove(event.getPlayer().getUniqueId());
        }
    }

    private void initiateDuel(Player player, Player target, GameType gameType) {
        this.awaitingReply.put(player.getUniqueId(), target.getUniqueId());
        PracticeTL.DUEL__REQUEST_SENT.send(player, "player", target.getName(), "type", gameType.getDisplayName());
        new FancyMessage(PracticeTL.DUEL__REQUEST_RECEIVED.get("name", player.getName(), "type", gameType.getDisplayName())).command("/accept " + player.getName()).send(target);
        this.waitingForReply.put(player.getUniqueId(), gameType);
    }

    private void startDuel(Player player, Player target) {
        PracticeTL.DUEL__STARTING.send(player, "player", target.getName());
        PracticeTL.DUEL__STARTING.send(target, "player", player.getName());
        this.plugin.getMatchManager().startMatch(player, target,
                this.waitingForReply.get(player.getUniqueId()), false);
        this.waitingForReply.remove(player.getUniqueId());
    }

    public void setupMenu() {
        this.duelMenu = new ItemMenu("Select PvP Style", 27, event -> {
            GameType gt = this.plugin.getGameTypeManager().getGameTypes().get(event.getPosition());
            if (gt == null) return;
            Player ply = event.getPlayer();
            Player target = Bukkit.getPlayer(this.pickingGameMode.get(ply.getUniqueId()));
            this.pickingGameMode.remove(ply.getUniqueId());
            this.initiateDuel(ply, target, gt);
        }, this.plugin);
        for (GameType gt : this.plugin.getGameTypeManager().getGameTypes())
            this.duelMenu.setOption(this.plugin.getGameTypeManager().getGameTypes().indexOf(gt), gt.getDisplay(), ChatColor.translateAlternateColorCodes('&', gt.getDisplayName()));
    }

    @Command(aliases = "accept", desc = "Accept a duel request", usage = "<player>")
    public void accept(@Sender Player sender, Player target) {
        if (!this.awaitingReply.containsKey(target.getUniqueId()) || this.awaitingReply.get(target.getUniqueId()) != sender.getUniqueId()) {
            PracticeTL.DUEL__UNAVAILABLE.send(sender);
            return;
        }
        this.awaitingReply.remove(target.getUniqueId());
        this.startDuel(target, sender);
    }

    @Command(aliases = "duel", desc = "Send a duel request", usage = "<player>")
    public void duel(@Sender Player sender, Player target) {
        this.duelMenu.open(sender);
        this.pickingGameMode.put(sender.getUniqueId(), target.getUniqueId());
    }
}
