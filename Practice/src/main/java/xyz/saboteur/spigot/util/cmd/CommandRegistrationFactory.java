package xyz.saboteur.spigot.util.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * TODO:
 *      Write documentation
 *
 * @author Goblom
 */
public class CommandRegistrationFactory {

    private static CommandMap cmap;

    private String commandLabel;
    private String description;
    private List<String> aliases;
    private String usage;
    private String permission;
    private String permissionMessage;
    private String fromPlugin;
    private CommandExecutor commandExecutor;

    public CommandRegistrationFactory() {
    }

    public CommandRegistrationFactory(String command) {
        this.commandLabel = command;
    }

    public void build() {
        register();
    }

    public CommandRegistrationFactory withCommandExecutor(CommandExecutor exec) {
        this.commandExecutor = exec;
        return this;
    }

    public CommandRegistrationFactory withPlugin(Plugin plugin) {
        this.fromPlugin = plugin.getName();
        return this;
    }

    public CommandRegistrationFactory withPermissionMessage(String message) {
        this.permissionMessage = ChatColor.translateAlternateColorCodes('&', message);
        return this;
    }

    public CommandRegistrationFactory withPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandRegistrationFactory withUsage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandRegistrationFactory withAliases(String... aliases) {
        this.aliases = Arrays.asList(aliases);
        return this;
    }

    public CommandRegistrationFactory withDescription(String description) {
        this.description = description;
        return this;
    }

    public CommandRegistrationFactory withCommandLabel(String label) {
        this.commandLabel = label;
        return this;
    }

    public void register() {
        ReflectCommand command;
        if (this.commandLabel != null && !this.commandLabel.isEmpty()) {
            command = new ReflectCommand(this.commandLabel);
        } else {
            throw new CommandNotPreparedException("Command does not have a name.");
        }

        if (this.commandExecutor == null) {
            throw new CommandNotPreparedException(this.commandLabel + " does not have an executor.");
        }

        if (this.aliases != null) {
            command.setAliases(this.aliases);
        }

        if (this.description != null) {
            command.setDescription(this.description);
        }

        if (this.permission != null) {
            command.setPermission(this.permission);
        }

        if (this.permissionMessage != null) {
            command.setPermissionMessage(this.permissionMessage);
        }

        if (this.usage != null) {
            command.setUsage(this.usage);
        }

        getCommandMap().register((this.fromPlugin != null ? this.fromPlugin : ""), command);
        command.setExecutor(this.commandExecutor);
    }

    private final CommandMap getCommandMap() {
        if (cmap == null) {
            try {
                final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                cmap = (CommandMap) f.get(Bukkit.getServer());
                return getCommandMap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (cmap != null) {
            return cmap;
        }
        return getCommandMap();
    }

    private final class ReflectCommand extends Command {

        private CommandExecutor exe = null;

        protected ReflectCommand(String command) {
            super(command);
        }

        public void setExecutor(CommandExecutor exe) {
            this.exe = exe;
        }

        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (exe != null) {
                exe.onCommand(sender, this, commandLabel, args);
            }
            return false;
        }
    }

    public class CommandNotPreparedException extends RuntimeException {

        public CommandNotPreparedException(String message) {
            super(message);
        }
    }

    public static CommandRegistrationFactory buildCommand(String command) {
        return new CommandRegistrationFactory(command);
    }

    public static CommandRegistrationFactory builder() {
        return new CommandRegistrationFactory();
    }
}
