package xyz.saboteur.spigot.practice.arena;

import com.google.common.collect.ImmutableList;
import com.sk89q.intake.argument.ArgumentException;
import com.sk89q.intake.argument.ArgumentParseException;
import com.sk89q.intake.argument.CommandArgs;
import com.sk89q.intake.parametric.Provider;
import com.sk89q.intake.parametric.ProvisionException;
import xyz.saboteur.spigot.Practice;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.List;

public class ArenaProvider implements Provider<Arena> {

    @Override
    public boolean isProvided() {
        return false;
    }

    @Nullable
    @Override
    public Arena get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        String name = arguments.next();
        if(Practice.get().getArenaManager().exists(name)) return Practice.get().getArenaManager().get(name);
        throw new ArgumentParseException("No arena with the name of '" + name + "'!");
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return ImmutableList.of();
    }
}
