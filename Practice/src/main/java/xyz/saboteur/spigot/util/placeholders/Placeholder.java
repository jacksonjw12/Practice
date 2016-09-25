package xyz.saboteur.spigot.util.placeholders;

import org.bukkit.entity.Player;

public interface Placeholder {
    String onPlaceholderRequest(Player player, String request);
}
