package xyz.saboteur.spigot.util.cmd;

import org.bukkit.command.CommandSender;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.PracticeUtil;

public class CommandBase {

    public CommandBase() {
        this(true);
    }

    public CommandBase(boolean grouped) {
        if(grouped)
            Practice.get().getExample().register(getClass().getSimpleName().replace("Cmd", "").toLowerCase(), this);
        else
            Practice.get().getExample().register(this);
    }

    protected void send(CommandSender sender, String msg, Object... objects) {
        PracticeUtil.send(sender, PracticeUtil.format(msg, objects));
    }
}
