package xyz.saboteur.spigot.practice.gametype;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.Loadout;
import xyz.saboteur.spigot.util.PracticeUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GameTypeManager implements Listener {
    private Practice plugin;
    private YamlConfiguration config;
    private File configFile;
    private List<GameType> gameTypes;
    private List<UUID> editing;

    public GameTypeManager(Practice plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "gametypes.yml");
        if(!this.configFile.exists()) {
            try {
                this.configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.gameTypes = new ArrayList<>();
        this.editing = new ArrayList<>();
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        loadGameTypes();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        new CmdGameTypes();
    }

    public void loadGameTypes() {
        try {
            this.config.load(this.configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        if(!this.config.contains("gametypes")) return;
        this.config.getConfigurationSection("gametypes").getKeys(false).forEach(s -> {
            GameType gameType = new GameType(s);
            if(this.config.getString("gametypes." + s + ".loadout", "").length() != 0)
                gameType.setDefaultLoadout(Loadout.fromString(this.config.getString("gametypes." + s + ".loadout")));
            if(this.config.contains("gametypes." + s + ".arenas"))
                gameType.setPossibleArenas(this.config.getStringList("gametypes." + s + ".arenas"));
            if(this.config.contains("gametypes." + s + ".display"))
                gameType.setDisplay(PracticeUtil.getDisplay(this.config.getString("gametypes." + s + ".display")));
            if(this.config.contains("gametypes." + s + ".editor"))
                gameType.setEditor(PracticeUtil.getLocationFromString(this.config.getString("gametypes." + s + ".editor")));
            gameType.setDisplayName(this.config.getString("gametypes." + s + ".displayName", s));
            gameType.setEditable(this.config.getBoolean("gametypes." + s + ".editable", false));
            if(this.config.getString("gametypes." + s + ".possibleGear", "").length() != 0) {
                Inventory inv = Bukkit.createInventory(null, 54, gameType.getName());
                inv.setContents(PracticeUtil.stringToInventory(this.config.getString("gametypes." + s + ".possibleGear")));
                gameType.setPossibleGear(inv);
            }
            if(this.config.contains("gametypes." + s + ".signs"))
                gameType.setSigns(this.config.getStringList("gametypes." + s + ".signs").stream().map(PracticeUtil::getLocationFromString).collect(Collectors.toList()));
            this.gameTypes.add(gameType);
        });
    }

    public GameType getGameType(Predicate<GameType> test) {
        Optional<GameType> o = gameTypes.stream().filter(test).findFirst();
        return o.isPresent() ? o.get() : null;
    }

    public GameType get(String name) {
        return this.getGameType(gt -> gt.getName().equalsIgnoreCase(name));
    }

    public GameType getFromDisplay(String name) {
        return this.getGameType(gt -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', gt.getDisplayName())).equalsIgnoreCase(ChatColor.stripColor(name)));
    }

    public List<GameType> getGameTypes() {
        return gameTypes;
    }

    public GameType create(String name) {
        GameType gameType = new GameType(name);
        this.gameTypes.add(gameType);
        this.saveGameTypes();
        this.plugin.getDuelManager().setupMenu();
        this.plugin.getPlayerDataManager().setupNewGameType(gameType);
        return gameType;
    }

    public void delete(String name) {
        this.gameTypes.remove(get(name));
        this.saveGameTypes();
        this.plugin.getDuelManager().setupMenu();
    }

    public void addSign(GameType gameType, Location location) {
        gameType.addSign(location);
    }

    public void removeSign(GameType gameType, Location location) {
        gameType.removeSign(location);
    }

    public void saveGameTypes() {
        this.config.set("gametypes", null);
        this.gameTypes.forEach(gt -> {
            this.config.set("gametypes." + gt.getName() + ".displayName", gt.getDisplayName());
            this.config.set("gametypes." + gt.getName() + ".editable", gt.isEditable());
            this.config.set("gametypes." + gt.getName() + ".loadout", gt.getDefaultLoadout().toString());
            this.config.set("gametypes." + gt.getName() + ".arenas", gt.getPossibleArenas());
            this.config.set("gametypes." + gt.getName() + ".signs", gt.getSigns().stream().map(PracticeUtil::getStringFromLocation).collect(Collectors.toList()));
            if(gt.getEditor() != null)
                this.config.set("gametypes." + gt.getName() + ".editor", PracticeUtil.getStringFromLocation(gt.getEditor()));
            if(gt.getDisplay() != null)
                this.config.set("gametypes." + gt.getName() + ".display", PracticeUtil.fromDisplay(gt.getDisplay()));
            if(gt.getPossibleGear() != null && gt.getPossibleGear().getContents() != null)
                this.config.set("gametypes." + gt.getName() + ".possibleGear", PracticeUtil.inventoryToString(gt.getPossibleGear()));
        });
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.getInventoryManager().populateMenus();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!this.editing.contains(uuid) || get(event.getInventory().getName()) == null) return;
        GameType gt = get(event.getInventory().getName());
        gt.setPossibleGear(event.getInventory());
        this.saveGameTypes();
        this.editing.remove(uuid);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.WALL_SIGN || event.getBlock().getType() == Material.SIGN_POST) {
            this.getGameTypes().stream().filter(gt -> gt.getSigns().contains(event.getBlock().getLocation())).forEach(gt -> this.removeSign(gt, event.getBlock().getLocation()));
        }
    }

    public void addEditing(Player player) {
        this.editing.add(player.getUniqueId());
    }

    public void updateSigns(GameType gt) {
        ArrayList<Location> signs = new ArrayList<>();
        gt.getSigns().stream().filter(loc -> loc.getBlock().getType() == Material.SIGN || loc.getBlock().getType() == Material.WALL_SIGN || loc.getBlock().getType() == Material.SIGN_POST).forEach(loc -> {
                    Sign sign = (Sign)loc.getBlock().getState();
                    sign.setLine(0, gt.getDisplayName());
                    sign.setLine(1, "");
                    sign.setLine(2, "");
                    sign.update();
                    signs.add(loc);
                }
        );
        gt.setSigns(signs);
    }
}
