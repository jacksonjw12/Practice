package xyz.saboteur.spigot.practice.gametype;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.saboteur.spigot.util.Loadout;

import java.util.ArrayList;
import java.util.List;

public class GameType {
    private String name, displayName;
    private Loadout defaultLoadout;
    private List<String> possibleArenas;
    private ItemStack display;
    private boolean editable;
    private Inventory possibleGear;
    private List<Location> signs;
    private Location editor;

    public GameType(String name, String displayName, Loadout defaultLoadout, List<String> possibleArenas, ItemStack display, boolean editable, Inventory possibleGear, List<Location> signs) {
        this.name = name;
        this.displayName = displayName;
        this.possibleArenas = possibleArenas;
        this.defaultLoadout = defaultLoadout;
        this.display = display;
        this.editable = editable;
        this.possibleGear = possibleGear;
        this.signs = signs;
    }

    public GameType(String name) {
        this(name, name, new Loadout(), new ArrayList<>(), new ItemStack(Material.ANVIL, 1), true, Bukkit.createInventory(null, 54, name), new ArrayList<>());
    }

    public boolean isSetup() {
        return this.displayName != null && this.defaultLoadout != null && this.possibleArenas != null && this.display != null && this.editor != null;
    }

    public void setPossibleGear(Inventory inventory) {
        this.possibleGear = Bukkit.createInventory(null, 54, name);// TODO: Change name
        this.possibleGear.setContents(inventory.getContents());
    }

    public Inventory getPossibleGear() {
        Inventory inventory = Bukkit.createInventory(null, possibleGear.getSize(), possibleGear.getName());
        inventory.setContents(possibleGear.getContents());
        return inventory;
    }

    public void setDisplayName(String name) {
        this.name = name;
        this.setPossibleGear(this.getPossibleGear());
    }

    public void addSign(Location location) {
        this.signs.add(location);
    }

    public void removeSign(Location location) {
        this.signs.remove(location);
    }

    public String getName() {
        return this.name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getDisplayNameColorless() { return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.displayName)); }

    public Loadout getDefaultLoadout() {
        return this.defaultLoadout;
    }

    public void setDefaultLoadout(Loadout loadout) {
        this.defaultLoadout = loadout;
    }

    public List<String> getPossibleArenas() {
        return this.possibleArenas;
    }

    public void setPossibleArenas(List<String> possibleArenas) {
        this.possibleArenas = possibleArenas;
    }

    public ItemStack getDisplay() {
        return this.display;
    }

    public void setDisplay(ItemStack display) {
        this.display = display;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public List<Location> getSigns() {
        return this.signs;
    }

    public void setSigns(List<Location> signs) {
        this.signs = signs;
    }

    public Location getEditor() {
        return this.editor;
    }

    public void setEditor(Location editor) {
        this.editor = editor;
    }
}
