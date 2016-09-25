package xyz.saboteur.spigot.practice.player;

import com.sk89q.intake.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.practice.kit.Kit;
import xyz.saboteur.spigot.util.ItemMenu;
import xyz.saboteur.spigot.util.Loadout;
import xyz.saboteur.spigot.util.PracticeTL;
import xyz.saboteur.spigot.util.PracticeUtil;
import xyz.saboteur.spigot.util.cmd.CommandBase;
import xyz.saboteur.spigot.util.cmd.Sender;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryManager extends CommandBase implements Listener {
    private Practice plugin;
    private ItemStack[] defaultInventory;
    private ItemMenu rankedGameSelector, unrankedGameSelector, kitEditor;
    private Map<String, Inventory> invs;
    private List<UUID> checkingInvs;

    public InventoryManager(Practice plugin) {
        super(false);
        this.plugin = plugin;
        this.invs = new HashMap<>();
        this.checkingInvs = new ArrayList<>();
        this.setupGameTypeMenus();
        this.setupDefaultInventory();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    private int getSize(GameType gt) {
        return (int) (Math.ceil(gt.getPossibleArenas().size() / 9D) * 9);
    }

    private int getSize() {
        return (int) (Math.ceil(this.plugin.getGameTypeManager().getGameTypes().size() / 9D) * 9);
    }

    private void setupDefaultInventory() {
        this.defaultInventory = new ItemStack[9];
        this.defaultInventory[8] = PracticeUtil.generateItem(new ItemStack(Material.DIAMOND_SWORD, 1), PracticeTL.ITEMS__RANKED.get());
        this.defaultInventory[4] = PracticeUtil.generateItem(new ItemStack(Material.IRON_SWORD, 1), PracticeTL.ITEMS__UNRANKED.get());
        this.defaultInventory[0] = PracticeUtil.generateItem(new ItemStack(Material.BOOK, 1), PracticeTL.ITEMS__KIT_EDITOR.get());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.setDefaultInventory(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) || event.getItem() == null) return;
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();
        if(itemStack.isSimilar(this.defaultInventory[0]))
            this.kitEditor.open(player);
        else if(itemStack.isSimilar(this.defaultInventory[8]))
            this.rankedGameSelector.open(player);
        else if(itemStack.isSimilar(this.defaultInventory[4]))
            this.unrankedGameSelector.open(player);
            else if(event.getItem().getType().equals(Material.ENCHANTED_BOOK)) {
            String kitName = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            Loadout loadout;
            if(kitName.startsWith("Default")) loadout = this.plugin.getMatchManager().getGameType(player).getDefaultLoadout();
            else loadout = this.plugin.getPlayerDataManager().getKit(player, this.plugin.getMatchManager().getGameType(player), player.getInventory().getHeldItemSlot() - 1).getLoadout();
            loadout.giveTo(player);
        }
    }

    private void setupGameTypeMenus() {
        this.kitEditor = new ItemMenu(ChatColor.translateAlternateColorCodes('&', PracticeTL.ITEMS__KIT_EDITOR.get()), this.getSize(), event -> {
            this.plugin.getKitEditManager().beginEditing(event.getPlayer(), this.plugin.getGameTypeManager().getFromDisplay(event.getName()));
            event.getPlayer().closeInventory();
        }, this.plugin);
        this.rankedGameSelector = new ItemMenu(ChatColor.translateAlternateColorCodes('&', PracticeTL.ITEMS__RANKED.get()), this.getSize(), event -> {
            if(event.getName() == null) return;
            Player player = event.getPlayer();
            player.closeInventory();
            this.plugin.getMatchManager().addToQueue(player, this.plugin.getGameTypeManager().getFromDisplay(event.getName()), true);
            this.populateMenus();
        }, this.plugin);
        this.unrankedGameSelector = new ItemMenu(ChatColor.translateAlternateColorCodes('&', PracticeTL.ITEMS__UNRANKED.get()), this.getSize(), event -> {
            if(event.getName() == null) return;
            Player player = event.getPlayer();
            player.closeInventory();
            this.plugin.getMatchManager().addToQueue(player, this.plugin.getGameTypeManager().getFromDisplay(event.getName()), false);
            this.populateMenus();
        }, this.plugin);
        this.populateMenus();
    }

    public void populateMenus() {
        int i = 0;
        for (GameType gt : this.plugin.getGameTypeManager().getGameTypes()) {
            if (!gt.isSetup()) continue;
            this.unrankedGameSelector.setOption(i, gt.getDisplay(), this.plugin.getMatchManager().getAmountInQueue(gt, true) + this.plugin.getMatchManager().getAmountInMatch(gt, false), ChatColor.translateAlternateColorCodes('&', gt.getDisplayName()), ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInQueue(gt, false), ChatColor.YELLOW + "In match: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInMatch(gt, false));
            this.rankedGameSelector.setOption(i, gt.getDisplay(), this.plugin.getMatchManager().getAmountInQueue(gt, true) + this.plugin.getMatchManager().getAmountInMatch(gt, false), ChatColor.translateAlternateColorCodes('&', gt.getDisplayName()), ChatColor.YELLOW + "In queue: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInQueue(gt, true), ChatColor.YELLOW + "In match: " + ChatColor.GREEN + this.plugin.getMatchManager().getAmountInMatch(gt, false));
            if (gt.isEditable())
                this.kitEditor.setOption(i, gt.getDisplay(), ChatColor.translateAlternateColorCodes('&', gt.getDisplayName()));
            ++i;
        }
    }

    public void showKits(Player player, GameType gameType) {
        PlayerInventory inv = player.getInventory();
        inv.setItem(0, PracticeUtil.generateItem(new ItemStack(Material.ENCHANTED_BOOK, 1), ChatColor.GOLD + "Default " + ChatColor.stripColor(gameType.getDisplayName()) + " Kit"));
        int i = 2;
        for (Kit kit : this.plugin.getPlayerDataManager().getKits(player, gameType)) {
            if (kit == null) continue;
            inv.setItem(i, PracticeUtil.generateItem(new ItemStack(Material.ENCHANTED_BOOK, 1), ChatColor.BLUE + kit.getName()));
            ++i;
        }
    }

    public void setDefaultInventory(Player ply) {
        ply.getInventory().setContents(this.defaultInventory);
        ply.getInventory().setArmorContents(null);
        ply.updateInventory();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        this.setDefaultInventory(event.getPlayer());
    }

    public String storeInv(final Player ply, boolean dead) {
        int i;
        Inventory inv = Bukkit.createInventory(null, 54, ply.getName());
        PlayerInventory pinv = ply.getInventory();
        for (i = 9; i <= 35; ++i)
            inv.setItem(i - 9, pinv.getContents()[i]);
        for (i = 0; i <= 8; ++i)
            inv.setItem(i + 27, pinv.getContents()[i]);
        inv.setItem(36, pinv.getHelmet());
        inv.setItem(37, pinv.getChestplate());
        inv.setItem(38, pinv.getLeggings());
        inv.setItem(39, pinv.getBoots());
        inv.setItem(48, PracticeUtil.generateItem(new ItemStack(dead ? Material.SKULL_ITEM : Material.SPECKLED_MELON, dead ? 1 : (int)ply.getHealth()), dead ? "&cPlayer Died" : "&aPlayer Health"));
        inv.setItem(49, PracticeUtil.generateItem(new ItemStack(Material.COOKED_BEEF, ply.getFoodLevel()), "&aPlayer Food"));
        inv.setItem(50, PracticeUtil.generateItem(new ItemStack(Material.POTION, ply.getActivePotionEffects().size()), "&aPotion Effects:", ply.getActivePotionEffects().stream().map(effect -> effect.getType().getName() + " " + (effect.getAmplifier() + 1) + " - " + PracticeUtil.formatSeconds(effect.getDuration() / 20) + "!").collect(Collectors.toList())));
        this.invs.put(ply.getName(), inv);
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.invs.remove(ply.getName()), 2400L);
        return PracticeUtil.inventoryToString(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!this.checkingInvs.contains(event.getWhoClicked().getUniqueId())) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!this.checkingInvs.contains(uuid)) return;
        this.checkingInvs.remove(uuid);
    }

    @Command(aliases = "inventory", desc = "View a player's inventory", usage = "[name]")
    public void viewInventory(@Sender Player sender, String name) {
        if(!this.invs.containsKey(name)) {
            send(sender, "&cThat inventory no longer exists!");
            return;
        }
        sender.openInventory(this.invs.get(name));
        this.checkingInvs.add(sender.getUniqueId());
    }
}
