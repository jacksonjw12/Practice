package xyz.saboteur.spigot.practice;

import com.sk89q.intake.Command;
import com.sk89q.intake.Require;
import org.bukkit.entity.Player;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.cmd.CommandBase;
import xyz.saboteur.spigot.util.cmd.Sender;

public class CmdPractice extends CommandBase {

    public CmdPractice() {
        super(false);
    }

    @Command(aliases = "setspawn", desc = "Set spawn", usage = " ")
    @Require("practice.setspawn")
    public void setSpawn(@Sender Player sender) {
        Practice.get().setSpawn(sender.getLocation());
    }
}
