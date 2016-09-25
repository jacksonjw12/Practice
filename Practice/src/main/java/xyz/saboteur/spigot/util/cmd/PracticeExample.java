package xyz.saboteur.spigot.util.cmd;

import com.google.common.base.Joiner;
import com.sk89q.intake.*;
import com.sk89q.intake.argument.Namespace;
import com.sk89q.intake.dispatcher.Dispatcher;
import com.sk89q.intake.fluent.CommandGraph;
import com.sk89q.intake.fluent.DispatcherNode;
import com.sk89q.intake.parametric.Injector;
import com.sk89q.intake.parametric.ParametricBuilder;
import com.sk89q.intake.parametric.provider.PrimitivesModule;
import com.sk89q.intake.util.auth.AuthorizationException;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.PracticeTL;
import xyz.saboteur.spigot.util.PracticeUtil;

import java.util.*;
import java.util.stream.Collectors;

public class PracticeExample {
    private Injector injector;
    private ParametricBuilder builder;
    private Dispatcher dispatcher;
    private CommandGraph graph;
    private DispatcherNode commands;

    private Map<String, Map<String, List<String>>> helpMessages = new HashMap<>();

    public PracticeExample() {
        injector = Intake.createInjector();
        injector.install(new PrimitivesModule());
        injector.install(new PlayerModule());

        builder = new ParametricBuilder(injector);
        builder.setAuthorizer(new PlayerAuthorizer());

        graph = new CommandGraph().builder(builder);
        commands = graph.commands();
    }

    private void sendHelpTo(CommandSender player, String group, int page) {
        int PAGE_SIZE = 5;
        List<String> help = helpMessages.get(group).entrySet().stream().filter(e -> e.getValue().stream().filter(player::hasPermission).count() != 0 || e.getValue().isEmpty()).map(Map.Entry::getKey).collect(Collectors.toList());
        page -= 1;
        int min = page * PAGE_SIZE;
        if(min > help.size()) min = help.size() - PAGE_SIZE;
        if(min < 0) min = 0;
        page = (min / PAGE_SIZE);
        int max = (page * PAGE_SIZE) + PAGE_SIZE;
        if(max > help.size()) max = help.size();
        page+=1;
        int totalPages = (int)Math.ceil(help.size() / PAGE_SIZE) + 1;
        StringBuilder msg = new StringBuilder(PracticeTL.HELP__TOP.get("type", WordUtils.capitalizeFully(group), "page", page, "maxpage", totalPages));
        help.subList(min, max).forEach(line -> msg.append("\n").append(line));
        if(page != totalPages)
            msg.append("\n").append(PracticeTL.HELP__NEXTPAGE.get("group", group, "page", page + 1));
        player.sendMessage(msg.toString());
    }

    public void register(CommandBase base) {
        Set<CommandMapping> mappings = commands.group(base.getClass().getSimpleName()).registerMethods(base).getDispatcher().getCommands();
        mappings.forEach(mapping -> new CommandRegistrationFactory(mapping.getPrimaryAlias()).withPlugin(Practice.get()).withCommandExecutor((sender, cmd, label, args) -> {
            if(sender instanceof Player) {
                execute((Player) sender, base.getClass().getSimpleName() + " " + label + " " + Joiner.on(' ').join(args));
                return true;
            }
            sender.sendMessage("Only players can send that command!");
            return true;
        }).register());

        graph = commands.graph();
        dispatcher = graph.getDispatcher();
    }

   public void register(String group, CommandBase base) {
        Set<CommandMapping> mappings = commands.group(group).registerMethods(base).getDispatcher().getCommands();
        Map<String, List<String>> descriptions = new HashMap<>();
        mappings.forEach(cmd -> descriptions.put(PracticeTL.HELP__DESCRIPTION.get("group", group, "cmd", Joiner.on(",").join(cmd.getAllAliases()) + (cmd.getDescription().getUsage().trim().length() == 0 ? "" : " " + cmd.getDescription().getUsage()), "description", cmd.getDescription().getShortDescription()), cmd.getDescription().getPermissions()));
        helpMessages.put(group, descriptions);
        new CommandRegistrationFactory(group).withPlugin(Practice.get()).withCommandExecutor((sender, cmd, label, args) -> {
            if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")) || (args.length == 2 && args[0].equalsIgnoreCase("help") && isInt(args[1]))) {
                int page = 1;
                try {
                    page = Integer.parseInt(args[1]);
                } catch(Exception ignored) {}
                sendHelpTo(sender, group, page);
                return true;
            }
            if(sender instanceof Player) {
                execute((Player) sender, label + " " + Joiner.on(' ').join(args));
                return true;
            }
            sender.sendMessage("Only players can send that command!");
            return true;
        }).register();
        graph = commands.graph();
        dispatcher = graph.getDispatcher();
    }

    private void execute(Player player, String command) {
        Namespace namespace = new Namespace();
        namespace.put("sender", player);
        executeCommand(namespace, dispatcher, command);
    }

    private void execute2(Player player, String command) {
        Namespace namespace = new Namespace();
        namespace.put("sender", player);
        try {
            dispatcher.call(command, namespace, Collections.emptyList());
        } catch (CommandException | InvocationCommandException e) {
            ((Player)namespace.get("sender")).sendMessage(ChatColor.RED + e.getMessage());
            //e.printStackTrace();
        } catch (AuthorizationException ignored) {
            PracticeTL.NO_PERMISSION.send(((Player)namespace.get("sender")));
        }
    }

    private void executeCommand(Namespace namespace, CommandCallable callable, String command) {
        try {
            callable.call(command, namespace, Collections.emptyList());
        } catch (CommandException | InvocationCommandException e) {
            if(e instanceof InvalidUsageException) {
                InvalidUsageException iue = (InvalidUsageException) e;
                if(iue.getMessage().equals("Please choose a sub-command.")) {
                    String prefix = command.split(" ")[0];
                    command = command.replaceFirst(prefix + " ", "");
                    if(command.equalsIgnoreCase("") || isInt(command)) {
                        executeCommand(namespace, callable, prefix + " help " + command);
                    } else {
                        PracticeUtil.send((Player) namespace.get("sender"), "&c&l(!) &cCommand not found! Use /" + prefix + " help for help.");
                    }
                    return;
                }
            }
            ((Player)namespace.get("sender")).sendMessage(ChatColor.RED + e.getMessage());
            //e.printStackTrace();
        } catch (AuthorizationException ignored) {
            PracticeTL.NO_PERMISSION.send(((Player)namespace.get("sender")));
        }
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch(Exception ignored) {}
        return false;
    }

    public Injector getInjector() {
        return injector;
    }

    public ParametricBuilder getBuilder() {
        return builder;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }
}
