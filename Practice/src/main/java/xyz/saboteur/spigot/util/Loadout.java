package xyz.saboteur.spigot.util;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static java.util.Arrays.copyOfRange;

public class Loadout {
    private Inventory inventory;

    public Loadout() {
        inventory = Bukkit.createInventory(null, 45);
    }

    public static Loadout fromString(String stored) {
        Loadout loadout = new Loadout();
        for(String itemString : stored.split(", ")) {
            String[] attr = itemString.split(" ");
            List<Integer> slots = new ArrayList<>();
            if(attr[0].contains("|")) {
                for (String s : attr[0].split("\\|"))
                    slots.add(Integer.parseInt(s));
            } else slots.add(Integer.parseInt(attr[0]));
            ItemStack itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(attr[1].split(":")[0])), Integer.parseInt(attr[2]), Short.parseShort(attr[1].split(":")[1]));
            if(attr.length > 2) {
                attr = copyOfRange(attr, 3, attr.length);
                Map<Enchantment, Integer> enchantments = new HashMap<>();
                for(String enchantStr : attr) {
                    String[] enchantAttr = enchantStr.split(":");
                    enchantments.put(Enchantment.getById(Integer.parseInt(enchantAttr[0])), Integer.parseInt(enchantAttr[1]));
                }
                itemStack.addUnsafeEnchantments(enchantments);
            }
            for(int i : slots)
                loadout.getInventory().setItem(i, itemStack);
        }
        return loadout;
    }

    public static Loadout fromPlayer(Player player) {
        Loadout loadout = new Loadout();
        for(int i = 0; i < player.getInventory().getContents().length; i++) {
            int y = (y = (3 - (i / 9))) < 3 ? 2-y : y;
            loadout.getInventory().setItem(9 + (y * 9 + (i % 9)), player.getInventory().getContents()[i]);
        }
        for(int i = 0; i < player.getInventory().getArmorContents().length; i++)
            loadout.getInventory().setItem(3 - i, player.getInventory().getArmorContents()[i]);
        return loadout;
    }

    @Override
    public String toString() {
        Map<String, List<Integer>> itemMap = new HashMap<>();
        List<String> items = new ArrayList<>();
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if(itemStack == null || itemStack.getType() == Material.AIR) continue;
            List<Object> attributes = new ArrayList<>();
            attributes.add(itemStack.getType().getId() + ":" + itemStack.getDurability());
            attributes.add(itemStack.getAmount());
            itemStack.getEnchantments().forEach((enchantment, level) -> attributes.add(enchantment.getId() + ":" + level));
            String data = Joiner.on(" ").join(attributes);
            List<Integer> slots = itemMap.getOrDefault(data, new ArrayList<>());
            if(!slots.contains(i))
                slots.add(i);
            itemMap.put(data, slots);
        }
        itemMap.forEach((data, slots) -> items.add(Joiner.on("|").join(slots) + " " + data));
        return Joiner.on(", ").join(items);
    }

    public ItemStack[] getContents() {
        ItemStack[] content = Arrays.copyOfRange(inventory.getContents(), 9, 45);
        ItemStack[] newContent = new ItemStack[content.length];
        for(int i = 0; i < content.length; i++) {
            int y = (y = ((i / 9) + 1)) == 4 ? 0 : y;
            newContent[y * 9 + (i % 9)] = content[i];
        }
        return newContent;
    }

    public ItemStack[] getArmorContents() {
        List<ItemStack> armor = Arrays.asList(Arrays.copyOfRange(inventory.getContents(), 0, 4));
        Collections.reverse(armor);
        return armor.toArray(new ItemStack[armor.size()]);
    }

    public void giveTo(Player player) {
        player.getInventory().setArmorContents(getArmorContents());
        player.getInventory().setContents(getContents());
        player.updateInventory();
    }

    public Inventory getInventory() {
        return inventory;
    }

}
