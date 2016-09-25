package xyz.saboteur.spigot;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import xyz.saboteur.spigot.practice.CmdPractice;
import xyz.saboteur.spigot.practice.arena.ArenaManager;
import xyz.saboteur.spigot.practice.gametype.GameTypeManager;
import xyz.saboteur.spigot.practice.kit.KitEditManager;
import xyz.saboteur.spigot.practice.match.DuelManager;
import xyz.saboteur.spigot.practice.match.MatchManager;
import xyz.saboteur.spigot.practice.player.InventoryManager;
import xyz.saboteur.spigot.practice.player.PlayerDataManager;
import xyz.saboteur.spigot.practice.player.PlayerEvents;
import xyz.saboteur.spigot.scoreboard.PracticeScoreboard;
import xyz.saboteur.spigot.util.EntityHider;
import xyz.saboteur.spigot.util.MongoManager;
import xyz.saboteur.spigot.util.PracticeTL;
import xyz.saboteur.spigot.util.PracticeUtil;
import xyz.saboteur.spigot.util.cmd.PracticeExample;
import xyz.saboteur.spigot.util.placeholders.Placeholders;
import xyz.saboteur.spigot.util.placeholders.PracticePlaceholders;

import java.io.File;

public class Practice extends JavaPlugin implements PluginMessageListener {
    private static Practice instance;
    private PracticeExample example;
    private PracticeScoreboard practiceScoreboard;
    private PracticePlaceholders practicePlaceholders;

    private InventoryManager inventoryManager;
    private ArenaManager arenaManager;
    private MatchManager matchManager;
    private GameTypeManager gameTypeManager;
    private PlayerDataManager playerDataManager;
    private KitEditManager kitEditManager;
    private DuelManager duelManager;

    private Location spawn;
    private EntityHider entityHider;
    private PlayerEvents playerEvents;

    @Override
    public void onEnable() {
        if(!new File(getDataFolder(), "config.yml").exists()) {
            this.saveDefaultConfig();
            PracticeTL.writeToConfig(this.getConfig());
            saveConfig();
        }
        if(getConfig().contains("spawn"))
            spawn = PracticeUtil.getLocationFromString(getConfig().getString("spawn"));
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        this.example = new PracticeExample();
        MongoManager.get();
        this.arenaManager = new ArenaManager(this);
        this.gameTypeManager = new GameTypeManager(this);
        this.matchManager = new MatchManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.kitEditManager = new KitEditManager(this);
        this.entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);
        this.duelManager = new DuelManager(this);
        this.playerEvents = new PlayerEvents(this);
        if (this.getConfig().contains("spawn"))
            this.spawn = PracticeUtil.getLocationFromString((this.getConfig().getString("spawn")));

        new CmdPractice();

        Placeholders.registerPlaceholder(practicePlaceholders = new PracticePlaceholders());
        this.practiceScoreboard = new PracticeScoreboard();

        Bukkit.getOnlinePlayers().forEach(player -> {
            this.inventoryManager.setDefaultInventory(player);
            player.teleport(this.getSpawn());
            player.setFoodLevel(20);
            Bukkit.getOnlinePlayers().stream().filter(other -> !player.canSee(other)).forEach(other -> player.showPlayer(other));
        });
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(!channel.equals("BungeeCord")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if(subchannel.equalsIgnoreCase("GetServer"))
            practicePlaceholders.setServer(in.readUTF());
    }

    @Override
    public void onDisable() {
        this.practiceScoreboard.destroy();
        Placeholders.unregisterPlaceholder(practicePlaceholders);
        MongoManager.get().destroy();
    }

    public static Practice get() {
        return instance == null ? instance = Practice.getPlugin(Practice.class) : instance;
    }

    public PracticeScoreboard getPracticeScoreboard() { return practiceScoreboard; }
    public PracticeExample getExample() { return example; }

    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    public ArenaManager getArenaManager() {
        return this.arenaManager;
    }

    public MatchManager getMatchManager() {
        return this.matchManager;
    }

    public GameTypeManager getGameTypeManager() {
        return this.gameTypeManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return this.playerDataManager;
    }

    public Location getSpawn() {
        return this.spawn;
    }

    public EntityHider getEntityHider() {
        return this.entityHider;
    }

    public KitEditManager getKitEditManager() {
        return this.kitEditManager;
    }

    public DuelManager getDuelManager() {
        return this.duelManager;
    }

    public PlayerEvents getPlayerEvents() {
        return this.playerEvents;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
        getConfig().set("spawn", PracticeUtil.getStringFromLocation(spawn));
        saveConfig();
    }
}
