package xyz.saboteur.spigot.util.placeholders;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Placeholders {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");
    private static List<Placeholder> placeholders = new ArrayList<>();

    public static void registerPlaceholder(Placeholder placeholder) {
        placeholders.add(placeholder);
    }

    public static void unregisterPlaceholder(Placeholder placeholder) {
        placeholders.remove(placeholder);
    }

    public static String setPlaceholders(Player player, String text) {
        if(text != null && placeholders != null && !placeholders.isEmpty()) {
            Matcher m = PLACEHOLDER_PATTERN.matcher(text);
            while(m.find()) {
                String identifier = m.group(1);
                for(Placeholder p : placeholders) {
                    String value = p.onPlaceholderRequest(player, identifier);
                    if(value != null)
                        text = text.replace("%" + identifier + "%", Matcher.quoteReplacement(value));
                }
            }
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static boolean containsPlaceholders(String text) {
        return (text != null && placeholders != null && !placeholders.isEmpty()) && PLACEHOLDER_PATTERN.matcher(text).find();
    }

}
