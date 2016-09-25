package xyz.saboteur.spigot.practice.arena;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import com.sk89q.intake.parametric.annotation.Text;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.cmd.CommandBase;
import xyz.saboteur.spigot.util.cmd.Sender;

public class CmdArena extends CommandBase {

    private ArenaManager arenaManager;

    @Command(aliases = "create", desc = "Create a new arena", usage = "<name> <builder>")
    @Require("practice.arena.create")
    public void create(@Sender Player sender, String name, @Text String builder) {
        if(getManager().exists(name)) {
            send(sender, "&c&l(!) &cThat arena already exists!");
            return;
        }
        getManager().create(name, builder.trim());
        send(sender, "&dCreated {name} built by {builder}", "name", name, "builder", builder.trim());
    }

    @Command(aliases = "delete", desc = "Delete an existing arena", usage = "<name>")
    @Require("practice.arena.delete")
    public void delete(@Sender Player sender, Arena arena) {
        getManager().delete(arena.getName());
        send(sender, "&dDeleted {name} built by {builder}", "name", arena.getName(), "builder", arena.getBuilder());
    }

    @Command(aliases = "addspawn", desc = "Add a spawn point to an existing arena", usage = "<name> <team>")
    @Require("practice.arena.addspawn")
    public void addSpawn(@Sender Player sender, Arena arena, int team) {
        arena.addSpawn(Math.abs(team - 1), sender.getLocation());
        getManager().saveArenas();
        send(sender, "&dAdded spawn point for team {team} on {name} built by {builder}", "team", team, "name", arena.getName(), "builder", arena.getBuilder());
    }

    @Command(aliases = "list", desc = "List arenas", usage = " ")
    public void addSpawn(@Sender Player sender) {
        FancyMessage msg = new FancyMessage("Arenas (" + getManager().getArenas().size() + "):").color(ChatColor.GOLD);
        getManager().getArenas().forEach(arena -> msg.then("\n " + arena.getName() + " » " + arena.getBuilder()).color(arena.isSetup() ? ChatColor.GREEN : ChatColor.RED));
        msg.send(sender);
    }

    public ArenaManager getManager() {
        return arenaManager == null ? arenaManager = Practice.get().getArenaManager() : arenaManager;
    }
}
