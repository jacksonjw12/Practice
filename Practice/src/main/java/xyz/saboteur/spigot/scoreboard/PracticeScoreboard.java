
package xyz.saboteur.spigot.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.saboteur.spigot.Practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PracticeScoreboard implements Listener {
    private Map<UUID, PScoreboard> playerScoreboards;
    //private List<SBGroup> groups;
    private BukkitTask task;
    private Practice plugin;
    private FileConfiguration config;
    private Map<UUID, ScoreboardLayout> layoutMap;
    //private SBGroup defaultGroup;

    public ScoreboardLayout getLayoutFor(Player player) {
        return layoutMap.get(player.getUniqueId());
    }

    public void setLayoutFor(Player player, ScoreboardLayout layout) {
        if(playerScoreboards.containsKey(player.getUniqueId()))
            playerScoreboards.get(player.getUniqueId()).clearBoard();
        layoutMap.put(player.getUniqueId(), layout);
    }

    public PracticeScoreboard() {
        this.plugin = Practice.get();
        this.config = plugin.getConfig();
        this.playerScoreboards = new HashMap<>();
        this.layoutMap = new HashMap<>();
        //this.groups = new ArrayList<>();
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::update, 0L, 20L);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        Bukkit.getOnlinePlayers().forEach(player -> {
            PScoreboard scoreboard = new PScoreboard("temp", "stats");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                playerScoreboards.put(player.getUniqueId(), scoreboard);
                setLayoutFor(player, ScoreboardLayout.IDLE_SOLO);
                update(player, scoreboard);
                scoreboard.setScoreboard(player);
            });
        });
    }

    /*public void enable() {
        this.currentTitle = 0;
        this.lines.addAll(config.getStringList("scoreboard.lines"));
        this.titles.addAll(config.getStringList("scoreboard.titles"));
        config.getConfigurationSection("groups").getKeys(false).forEach(s -> groups.add(new SBGroup(s, config.getString("groups." + s, "&7"))));

        Optional<SBGroup> o = groups.stream().filter(g -> g.getGroup().equalsIgnoreCase("default")).findFirst();
        if(!o.isPresent()) {
            System.err.println("NO DEFAULT GROUP!!!!");
        } else defaultGroup = o.get();

        this.register(new Listener() {

        });


    }*/

    public void destroy() {
        this.task.cancel();
        this.playerScoreboards.values().forEach(PScoreboard::destroy);
        this.playerScoreboards.clear();
        /*this.lines.clear();
        this.titles.clear();
        this.groups.clear();*/
        Bukkit.getOnlinePlayers().forEach(player -> player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
    }

    private void update() {
        Bukkit.getOnlinePlayers().forEach(player -> update(player, playerScoreboards.get(player.getUniqueId())));
    }

    private void update(Player player, PScoreboard scoreboard) {
        if (scoreboard == null) return;
        ScoreboardLayout layout = getLayoutFor(player);
        if(layout == null) return;
        scoreboard.setTitle(layout.getTitle(player));
        List<String> lines = layout.getLines(player);
        int i = lines.size();
        for (String s : lines)
            scoreboard.setScore(s, i--);
    }

    /*public SBGroup getGroupForPlayer(Player player) {
        Optional<SBGroup> o = groups.parallelStream().filter(g -> plugin.getPerms().playerInGroup(player, g.getGroup()))*//*.sorted((g, g2) -> g.getPriority().compareTo(g2.getPriority()))*//*.findFirst();
        return o.isPresent() ? o.get() : defaultGroup == null ? null : defaultGroup;
    }

    public Team getTeamNeeded(Player player, SBGroup group, PScoreboard scoreboard) {
        if(group == null || scoreboard == null) return null;
        if(scoreboard.getTeam(*//*group.getGroup()*//*player.getName()) == null) {
            Team team = scoreboard.createTeam(player.getName(), player);
            team.setNameTagVisibility(NameTagVisibility.ALWAYS);
            team.setPrefix(ChatColor.translateAlternateColorCodes('&', group.getTag()));
            team.setAllowFriendlyFire(true);
            return team;
        }
        return scoreboard.getTeam(player.getName());
    }

    public void setNeededTeam(Player player, SBGroup group, PScoreboard scoreboard, boolean loggingIn) {
        if(group == null || scoreboard == null) return;
        Team team = getTeamNeeded(player, group, scoreboard);
        team.addPlayer(player);
        if(loggingIn) return;
        team.removePlayer(player);
    }*/

    @EventHandler(priority= EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PScoreboard scoreboard = new PScoreboard("temp", "stats");
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            playerScoreboards.put(player.getUniqueId(), scoreboard);
            setLayoutFor(player, ScoreboardLayout.IDLE_SOLO);
            /*SBGroup group = getGroupForPlayer(player);
            if(group == null) return;
            Bukkit.getOnlinePlayers().forEach(p -> {
                if(playerScoreboards.containsKey(p.getUniqueId()))
                    setNeededTeam(player, group, playerScoreboards.get(p.getUniqueId()), true);
                SBGroup group2 = getGroupForPlayer(p);
                if(group2 == null) return;
                setNeededTeam(p, group2, scoreboard, true);
            });*/
            update(player, scoreboard);
            scoreboard.setScoreboard(player);
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = player.getUniqueId();
            if (!playerScoreboards.containsKey(uuid)) return;
            playerScoreboards.get(uuid).destroy();
            playerScoreboards.remove(uuid);
            layoutMap.remove(uuid);

            /*SBGroup group = getGroupForPlayer(player);
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (!playerScoreboards.containsKey(p.getUniqueId())) return;
                setNeededTeam(player, group, playerScoreboards.get(p.getUniqueId()), false);
            });*/
        });
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        //if(!isEnabled()) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            if (!playerScoreboards.containsKey(uuid)) return;
            PScoreboard scoreboard = playerScoreboards.get(uuid);
            update(player, scoreboard);
            scoreboard.setScoreboard(player);
        });
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        //if(!isEnabled()) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            if (!playerScoreboards.containsKey(uuid)) return;
            PScoreboard scoreboard = playerScoreboards.get(uuid);
            update(player, scoreboard);
            scoreboard.setScoreboard(player);
        });
    }
}
