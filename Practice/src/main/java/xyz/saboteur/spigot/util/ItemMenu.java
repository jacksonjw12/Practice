package xyz.saboteur.spigot.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import xyz.saboteur.spigot.Practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemMenu implements Listener {
    private String name;
    private int size;
    private OptionClickEventHandler handler;
    private Practice plugin;
    private String[] optionNames;
    private ItemStack[] optionIcons;
    List<UUID> open;

    public ItemMenu(String name, int size, OptionClickEventHandler handler, Practice plugin) {
        this.name = name;
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        this.open = new ArrayList<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ItemMenu setOption(int position, Material icon, String name, String ... info) {
        ItemStack item = this.setItemNameAndLore(icon, name, info);
        if (this.optionIcons[position] != null && this.optionIcons[position].getType() == icon) {
            this.updateItem(position, item.getItemMeta());
        }
        this.optionIcons[position] = item;
        this.optionNames[position] = name;
        return this;
    }

    public ItemMenu setOption(int position, ItemStack icon, String name, String ... info) {
        ItemStack item = this.setItemNameAndLore(icon.clone(), name, info);
        if (this.optionIcons[position] != null && this.optionIcons[position].getType().equals(icon.getType()))
            this.updateItem(position, item.getItemMeta());
        this.optionIcons[position] = item;
        this.optionNames[position] = name;
        return this;
    }

    public ItemMenu setOption(int position, ItemStack icon, int amt, String name, String ... info) {
        ItemStack item = this.setItemNameAndLore(icon.clone(), name, info);
        item.setAmount(amt > 64 ? 64 : amt);
        if (this.optionIcons[position] != null && this.optionIcons[position].getType().equals(icon.getType()))
            this.updateItem(position, item.getItemMeta());
        this.optionIcons[position] = item;
        this.optionNames[position] = name;
        return this;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(player, this.size, this.name);
        for (int i = 0; i < this.optionIcons.length; ++i) {
            if (this.optionIcons[i] == null) continue;
            inventory.setItem(i, this.optionIcons[i]);
        }
        this.open.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        this.handler = null;
        this.plugin = null;
        this.optionNames = null;
        this.optionIcons = null;
    }

    @EventHandler(priority= EventPriority.MONITOR)
    void onInventoryClick(InventoryClickEvent event) {
        if(event.getInventory().getTitle().equals(this.name)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if(slot >= 0 && slot < this.size && this.optionNames[slot] != null) {
                Plugin plugin = this.plugin;
                OptionClickEvent e = new OptionClickEvent((Player)event.getWhoClicked(), slot, this.optionNames[slot]);
                this.handler.onOptionClick(e);
                if(e.willClose()) {
                    final Player p = (Player)event.getWhoClicked();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, p::closeInventory, 1);
                }
                if(e.willDestroy()) this.destroy();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!this.open.contains(uuid)) return;
        this.open.remove(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!this.open.contains(uuid)) return;
        this.open.remove(uuid);
    }

    private ItemStack setItemNameAndLore(Material material, String name, String[] lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    public void updateItem(int slot, ItemMeta imm) {
        this.open.stream().map(Bukkit::getPlayer).forEach(p -> {
            p.getOpenInventory().getItem(slot).setItemMeta(imm);
            p.updateInventory();
        });
    }

    public ItemStack getItem(int slot) {
        return this.optionIcons[slot];
    }

    public class OptionClickEvent {
        private Player player;
        private int position;
        private String name;
        private boolean close;
        private boolean destroy;

        public OptionClickEvent(Player player, int position, String name) {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = true;
            this.destroy = false;
        }

        public Player getPlayer() {
            return this.player;
        }

        public int getPosition() {
            return this.position;
        }

        public String getName() {
            return this.name;
        }

        public boolean willClose() {
            return this.close;
        }

        public boolean willDestroy() {
            return this.destroy;
        }

        public void setWillClose(boolean close) {
            this.close = close;
        }

        public void setWillDestroy(boolean destroy) {
            this.destroy = destroy;
        }
    }

    public static interface OptionClickEventHandler {
        public void onOptionClick(OptionClickEvent var1);
    }

}