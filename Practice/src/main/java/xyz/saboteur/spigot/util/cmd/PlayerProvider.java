package xyz.saboteur.spigot.util.cmd;

import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerProvider implements Provider<Player> {

    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public Player get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        return Bukkit.getPlayer(arguments.next());
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().startsWith(prefix)).map(Player::getName).collect(Collectors.toList());
    }
}
