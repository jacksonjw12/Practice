package xyz.saboteur.spigot.practice.gametype;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.Loadout;
import xyz.saboteur.spigot.util.cmd.CommandBase;
import xyz.saboteur.spigot.util.cmd.Sender;

public class CmdGameTypes extends CommandBase {

    private GameTypeManager gameTypeManager;

    @Command(aliases = "create", desc = "Create a new game type", usage = "<name>")
    @Require("practice.gametype.create")
    public void create(@Sender Player sender, String name) {
        if(getManager().get(name) != null) {
            send(sender, "&c&l(!) &cThat arena already exists!");
            return;
        }
        getManager().create(name);
        send(sender, "&dCreated GameType {name}", "name", name);
    }

    @Command(aliases = "delete", desc = "Delete an existing game type", usage = "<name>")
    @Require("practice.gametype.delete")
    public void delete(@Sender Player sender, GameType type) {
        getManager().delete(type.getName());
        send(sender, "&dDeleted GameType {name}", "name", type.getName());
    }

    @Command(aliases = "setdisplay", desc = "Set a game type's display item (in hand)", usage = "<name>")
    @Require("practice.gametype.setdisplay")
    public void setDisplay(@Sender Player sender, GameType type) {
        ItemStack hand = sender.getItemInHand();
        if(hand == null || hand.getType().equals(Material.AIR)) {
            send(sender, "&c&l(!) &cYou can't set a display to nothing!");
            return;
        }
        type.setDisplay(hand);
        send(sender, "&dGame type {name} display updated.", "name", type.getName());
        getManager().saveGameTypes();
    }

    @Command(aliases = {"setloadout", "setdefaultloadout"}, desc = "Set default loadout for a game type", usage = "<name>")
    @Require("practice.gametype.setloadout")
    public void setDefaultLoadout(@Sender Player sender, GameType type) {
        type.setDefaultLoadout(Loadout.fromPlayer(sender));
        send(sender, "&dGame type {name} default loadout updated.", "name", type.getName());
        getManager().saveGameTypes();
    }

    @Command(aliases = {"loadloadout", "loaddefaultloadout"}, desc = "Load default loadout for a game type", usage = "<name>")
    @Require("practice.gametype.loadloadout")
    public void loadDefaultLoadout(@Sender Player sender, GameType type) {
        if(type.getDefaultLoadout() == null) {
            send(sender, "&c&l(!) &cThere is no loadout for {name}", "name", type.getName());
            return;
        }
        type.getDefaultLoadout().giveTo(sender);
    }

    @Command(aliases = "list", desc = "List available game types", usage = " ")
    public void list(@Sender Player sender) {
        FancyMessage msg = new FancyMessage("Game Types (" + getManager().getGameTypes().size() + "):").color(ChatColor.GOLD);
        getManager().getGameTypes().forEach(gt -> msg.then("\n " + gt.getDisplayName()).color(gt.isSetup() ? ChatColor.GREEN : ChatColor.RED));
        msg.send(sender);
    }

    @Command(aliases = "seteditor", desc = "Set game type editor", usage = "<name>")
    @Require("practice.arena.seteditor")
    public void setEditor(@Sender Player sender, GameType type) {
        type.setEditor(sender.getLocation());
        getManager().saveGameTypes();
    }

    @Command(aliases = "seteditable", desc = "Allow players to edit or not", usage = "<name>")
    @Require("practice.gametype.seteditable")
    public void setEditable(@Sender Player sender, GameType type) {
        type.setEditable(!type.isEditable());
        getManager().saveGameTypes();
    }

    @Command(aliases = "editinv", desc = "Edit allowed gear", usage = "<name>")
    @Require("practice.gametype.edit")
    public void editInv(@Sender Player sender, GameType type) {
        sender.openInventory(type.getPossibleGear());
        getManager().addEditing(sender);
    }

    public GameTypeManager getManager() {
        return gameTypeManager == null ? gameTypeManager = Practice.get().getGameTypeManager() : gameTypeManager;
    }
}
