package xyz.saboteur.spigot.scoreboard;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.placeholders.Placeholders;

import java.util.List;
import java.util.stream.Collectors;

public enum ScoreboardLayout {
    IDLE_SOLO,
    IDLE_PARTY,
    MATCH_SOLO,
    MATCH_PARTY;

    private String title;
    private boolean titleHasPlaceholders;
    private List<String> lines;

    ScoreboardLayout() {
        FileConfiguration config = Practice.get().getConfig();
        title = config.getString("scoreboard." + name().toLowerCase().replace("_", ".") + ".title");
        lines = config.getStringList("scoreboard." + name().toLowerCase().replace("_", ".") + ".lines");
        titleHasPlaceholders = Placeholders.containsPlaceholders(title);
    }

    public String getTitle(Player player) {
        return titleHasPlaceholders ? Placeholders.setPlaceholders(player, title) : ChatColor.translateAlternateColorCodes('&', title);
    }

    public List<String> getLines(Player player) {
        return this.lines.stream().map(s -> Placeholders.containsPlaceholders(s) ? Placeholders.setPlaceholders(player, s) : ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
    }
}
