package xyz.saboteur.spigot.util.placeholders;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.BasicReflectionUtils;
import xyz.saboteur.spigot.util.PracticeUtil;

import java.text.DecimalFormat;

public class PracticePlaceholders implements Placeholder {
    private Practice plugin;
    private DecimalFormat format = new DecimalFormat("#,###");
    private String server;
    private int attempts;

    public PracticePlaceholders() {
        this.plugin = Practice.get();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if(identifier.equalsIgnoreCase("online"))
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        else if(identifier.equalsIgnoreCase("ping"))
            return String.valueOf(BasicReflectionUtils.getPing(player));
        else if(identifier.equalsIgnoreCase("elo"))
            return format.format(plugin.getPlayerDataManager().getPlayerData(player).getAvgElo());
        /*else if(identifier.equalsIgnoreCase("matches"))
            return format.format(plugin.getPlayerManager().get(player).getRankedMatchesLeft());*/
        else if(identifier.equalsIgnoreCase("server")) {
            if(server == null && attempts <= 50) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("GetServer");
                player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                attempts++;
            }
            return server == null ? "N/A" : server;
        } else if(identifier.equalsIgnoreCase("opponent")) {
            return plugin.getMatchManager().getMatch(player).getOpponent(player).getName();
        } else if(identifier.equalsIgnoreCase("ladder")) {
            return plugin.getMatchManager().getMatch(player).getGameType().getDisplayNameColorless();
        } else if(identifier.equalsIgnoreCase("duration")) {
            return PracticeUtil.formatSeconds(plugin.getMatchManager().getMatch(player).getDuration());
        }
        return null;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
