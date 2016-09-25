package xyz.saboteur.spigot.util.cmd;

import com.sk89q.intake.parametric.AbstractModule;
import org.bukkit.entity.Player;
import xyz.saboteur.spigot.practice.arena.Arena;
import xyz.saboteur.spigot.practice.arena.ArenaProvider;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.practice.gametype.GameTypeProvider;

public class PlayerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Player.class).annotatedWith(Sender.class).toProvider(new SenderProvider());
        bind(Player.class).toProvider(new PlayerProvider());
        bind(Arena.class).toProvider(new ArenaProvider());
        bind(GameType.class).toProvider(new GameTypeProvider());
    }
}
