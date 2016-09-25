package xyz.saboteur.spigot.util;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class PracticeUtil {
    public static ItemStack generateItem(ItemStack original, String name, String... lore) {
        return generateItem(original, name, lore == null ? null : Arrays.asList(lore));
    }

    public static ItemStack generateItem(ItemStack original, String name, List<String> lore) {
        original = original.clone();
        ItemMeta im = original.getItemMeta();
        if(name.length() > 0) im.setDisplayName(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', name));
        if(lore != null) im.setLore(lore.stream().filter(s -> s.length() > 0).map(s -> ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
        original.setItemMeta(im);
        return original;
    }

    public static String format(String s, Object... objects) {
        if(objects != null)
            for(int i = 0; i < objects.length; i+= 2)
                s = s.replace("{" + objects[i] + "}", String.valueOf(objects[i + 1]));
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void send(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static String getStringFromLocation(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ", " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ", " + loc.getYaw() + ", " + loc.getPitch();
    }

    public static Location getLocationFromString(String s) {
        String[] data = s.split(", ");
        return new Location(Bukkit.getWorld(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3]), Float.parseFloat(data[4]), Float.parseFloat(data[5]));
    }

    public static ItemStack getDisplay(String itemString) {
        return new ItemStack(Material.getMaterial(Integer.parseInt(itemString.split(":")[0])), 1, Short.parseShort(itemString.split(":")[1]));
    }

    public static String fromDisplay(ItemStack itemStack) {
        return itemStack.getType().getId() + ":" + itemStack.getDurability();
    }

    public static String inventoryToString(Inventory inventory) {
        Map<String, List<Integer>> itemMap = new HashMap<>();
        List<String> items = new ArrayList<>();
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if(itemStack == null || itemStack.getType() == Material.AIR) continue;
            List<Object> attributes = new ArrayList<>();
            attributes.add(itemStack.getAmount());
            attributes.add(itemStack.getType().getId() + ":" + itemStack.getDurability());
            if(itemStack.hasItemMeta()) {
                ItemMeta im = itemStack.getItemMeta();
                if(im.hasDisplayName())
                    attributes.add(im.getDisplayName().replace(" ", "_"));
                else if(im.hasLore())
                    attributes.add(Joiner.on("||").join(im.getLore()).replace(" ", "_"));
            }
            String data = Joiner.on(" ").join(attributes);
            List<Integer> slots = itemMap.getOrDefault(data, new ArrayList<>());
            if(!slots.contains(i))
                slots.add(i);
            itemMap.put(data, slots);
        }
        itemMap.forEach((data, slots) -> items.add(Joiner.on(",").join(slots) + " " + data));
        return Joiner.on(", ").join(items);
    }

    public static ItemStack[] stringToInventory(String stored) {
        Map<Integer, ItemStack> itemsMap = new HashMap<>();
        int highestSlot = 0;
        for(String itemString : stored.split(", ")) {
            String[] attr = itemString.split(" ");
            ItemStack itemStack = new ItemStack(Material.getMaterial(attr[2].split(":")[0]), Integer.parseInt(attr[1]), Short.parseShort(attr[2].split(":")[1]));
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(attr.length >= 4)
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', attr[3].replace("_", " ")));
            if(attr.length >= 5)
                itemMeta.setLore(Arrays.asList(attr[4].replace("_", " ").split("\\|\\|")));
            itemStack.setItemMeta(itemMeta);
            if(attr[0].contains(",")) {
                for(String s : attr[0].split(",")) {
                    int slot = Integer.parseInt(s);
                    if(slot > highestSlot) highestSlot = slot;
                    itemsMap.put(slot, itemStack);
                }
            } else {
                int slot = Integer.parseInt(attr[0]);
                if(slot > highestSlot) highestSlot = slot;
                itemsMap.put(slot, itemStack);
            }
        }
        ItemStack[] contents = new ItemStack[(int) Math.floor((highestSlot + 9/2) / 9) * 9];
        itemsMap.forEach((slot, item) -> contents[slot] = item);
        return contents;
    }

    public static String formatSeconds(int seconds) {
        int minutes = seconds / 60;
        if (minutes == 0) return seconds + "s";
        return minutes + "m" + (seconds % 60) + "s";
    }
}
