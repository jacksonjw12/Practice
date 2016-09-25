package xyz.saboteur.spigot.practice.kit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.practice.player.PlayerDataManager;
import xyz.saboteur.spigot.util.ItemMenu;
import xyz.saboteur.spigot.util.Loadout;
import xyz.saboteur.spigot.util.PracticeTL;

import java.util.*;

public class KitEditManager implements Listener {
    private Practice plugin;
    private Map<UUID, GameType> editing;
    private Map<UUID, Kit> renaming;
    private PlayerDataManager playerDataManager;
    private Map<UUID, ItemMenu> menus;
    private List<UUID> clickCooldown;

    public KitEditManager(Practice plugin) {
        this.plugin = plugin;
        this.editing = new HashMap<>();
        this.renaming = new HashMap<>();
        this.menus = new HashMap<>();
        this.playerDataManager = this.plugin.getPlayerDataManager();
        this.clickCooldown = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void beginEditing(Player player, GameType gt) {
        player.teleport(gt.getEditor());
        player.getInventory().clear();
        this.editing.keySet().forEach(eUid -> {
            Player ePlayer = Bukkit.getPlayer(eUid);
            ePlayer.hidePlayer(player);
            player.hidePlayer(ePlayer);
        });
        this.editing.put(player.getUniqueId(), gt);
        PracticeTL.KIT__EDITING.send(player, "name", gt.getDisplayName());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || !editing.containsKey(uuid) || event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType().equals(Material.WALL_SIGN) || event.getClickedBlock().getType().equals(Material.SIGN_POST)) {
            this.editing.remove(uuid);
            player.teleport(this.plugin.getSpawn());
            this.plugin.getInventoryManager().setDefaultInventory(player);
            Bukkit.getOnlinePlayers().stream().filter(p -> p != player).forEach(ePlayer -> {
                player.showPlayer(ePlayer);
                ePlayer.showPlayer(player);
            });
        } else if(event.getClickedBlock().getType().equals(Material.CHEST)) {
            event.setCancelled(true);
            player.openInventory(this.editing.get(uuid).getPossibleGear());
        } else if(event.getClickedBlock().getType().equals(Material.ANVIL)) {
            ItemMenu menu = this.getKitMenu(player, this.editing.get(uuid));
            this.menus.put(uuid, menu);
            menu.open(player);
            event.setCancelled(true);
        }
    }

    private ItemMenu getKitMenu(Player player, final GameType gt) {
        UUID uuid = player.getUniqueId();
        ItemMenu menu = new ItemMenu(PracticeTL.KIT__TITLE.get("type", gt.getName()), 36, event -> {
            String itemName = ChatColor.stripColor(event.getName());
            if(itemName.equals("") || this.clickCooldown.contains(uuid)) return;
            this.clickCooldown.add(uuid);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.clickCooldown.remove(uuid), 5L);
            String[] itemNameA = itemName.split(" ");
            String option = itemNameA[0] + " " + itemNameA[1] + " ";
            String kitName = itemName.replaceFirst(option, "");
            if(option.equals(PracticeTL.KIT__ITEMS__SAVE_PREFIX.get())) {
                Kit kit;
                boolean update = false;
                int kitNumber = this.getPosition(event.getPosition());
                if(this.playerDataManager.getKit(player, gt, kitNumber) == null) {
                    kit = new Kit(PracticeTL.KIT__DEFAULT_NAME.get("type", gt.getDisplayName(), "type-colorless", gt.getDisplayNameColorless(), "number", kitNumber), Loadout.fromPlayer(player));
                    update = true;
                } else kit = this.playerDataManager.getKit(player, gt, kitNumber);
                kit.setLoadout(Loadout.fromPlayer(player));
                PracticeTL.KIT__SAVED.send(player, "name", kitName);
                if(update)
                    this.playerDataManager.setKit(player, gt, kitNumber, kit);
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.updateMenu(player, gt), 5L);
            } else if(option.equals(PracticeTL.KIT__ITEMS__LOAD_PREFIX.get())) {
                Kit kit3 = this.playerDataManager.getKit(player, gt, this.getPosition(event.getPosition()));
                if (kit3.getLoadout().getArmorContents() != null)
                    player.getInventory().setArmorContents(kit3.getLoadout().getArmorContents());
                player.getInventory().setContents(kit3.getLoadout().getContents());
                PracticeTL.KIT__LOADED.send(player, "name", kit3.getName());
                player.closeInventory();
            } else if(option.equals(PracticeTL.KIT__ITEMS__RENAME_PREFIX.get())) {
                final Kit kit4 = this.playerDataManager.getKit(player, gt, this.getPosition(event.getPosition()));
                if (this.renaming.containsKey(uuid)) {
                    PracticeTL.KIT__RENAME__CANCEL.send(player, "name", this.renaming.get(uuid).getName());
                    this.renaming.remove(uuid);
                }
                this.renaming.put(uuid, kit4);
                PracticeTL.KIT__RENAME__NEW.send(player, "name", kit4.getName());
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    if (this.renaming.containsKey(uuid) && this.renaming.get(uuid) == kit4) {
                        this.renaming.remove(uuid);
                        PracticeTL.KIT__RENAME__CANCEL.send(player, "name", kit4.getName());
                    }
                }, 300L);
                player.closeInventory();
            } else if(option.equals(PracticeTL.KIT__ITEMS__DELETE_PREFIX.get())) {
                this.playerDataManager.removeKit(player, gt, this.getPosition(event.getPosition()));
                PracticeTL.KIT__DELETED.send(player, "name", kitName);
            }
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.updateMenu(player, gt), 5L);
        }, this.plugin);
        for (int i = 1; i <= 5; ++i) {
            Kit kit = this.playerDataManager.getKit(player, gt, i);
            int slot = (i - 1) * 2;
            if (kit == null) {
                menu.setOption(slot, Material.CHEST, PracticeTL.KIT__ITEMS__SAVE.get("name", i));
                continue;
            }
            String kitName = kit.getName();
            menu.setOption(slot, Material.CHEST, PracticeTL.KIT__ITEMS__SAVE.get("name", kitName));
            menu.setOption(slot + 9, Material.ENCHANTED_BOOK, PracticeTL.KIT__ITEMS__LOAD.get("name", kitName));
            menu.setOption(slot + 18, Material.NAME_TAG, PracticeTL.KIT__ITEMS__RENAME.get("name", kitName));
            menu.setOption(slot + 27, Material.FIRE, PracticeTL.KIT__ITEMS__DELETE.get("name", kitName));
        }
        return menu;
    }

    @EventHandler
    public void onRenameKit(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(!this.renaming.containsKey(uuid)) return;
        Kit kit = this.renaming.get(uuid);
        String newName = event.getMessage();
        PracticeTL.KIT__RENAME__SUCCESS.send(player, "old", kit.getName(), "new", newName);
        this.renaming.get(uuid).setName(newName);
        this.renaming.remove(uuid);
        this.playerDataManager.saveKits(player);
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if(!this.menus.containsKey(uuid)) return;
        this.menus.remove(uuid);
    }

    public void updateMenu(Player player, GameType gt) {
        UUID uuid = player.getUniqueId();
        if(!this.menus.containsKey(uuid)) return;
        ItemMenu menu = this.menus.get(uuid);
        for (int i = 1; i <= 5; ++i) {
            Kit kit = this.playerDataManager.getKit(player, gt, i);
            int slot = (i - 1) * 2;
            if (kit == null) {
                menu.setOption(slot, Material.CHEST, PracticeTL.KIT__ITEMS__SAVE.get("name", i));
                continue;
            }
            String kitName = kit.getName();
            menu.setOption(slot, Material.CHEST, PracticeTL.KIT__ITEMS__SAVE.get("name", kitName));
            menu.setOption(slot + 9, Material.ENCHANTED_BOOK, PracticeTL.KIT__ITEMS__LOAD.get("name", kitName));
            menu.setOption(slot + 18, Material.NAME_TAG, PracticeTL.KIT__ITEMS__RENAME.get("name", kitName));
            menu.setOption(slot + 27, Material.FIRE, PracticeTL.KIT__ITEMS__DELETE.get("name", kitName));
        }
    }

    private int getPosition(int slot) {
        if (slot > 26) {
            slot -= 27;
        } else if (slot > 17) {
            slot -= 18;
        } else if (slot > 8) {
            slot -= 9;
        }
        return slot / 2 + 1;
    }

    public boolean isEditing(Player player) {
        return this.editing.containsKey(player.getUniqueId());
    }
}
