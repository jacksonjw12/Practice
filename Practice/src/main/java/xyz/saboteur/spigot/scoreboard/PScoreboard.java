package xyz.saboteur.spigot.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class PScoreboard {
    private Scoreboard scoreboard;
    private Objective objective;
    private Objective buffer;
    private Map<Integer, String> lineMap;

    public PScoreboard(String title, String objective) {
        this.lineMap = new HashMap<>();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(objective, "dummy");
        this.buffer = scoreboard.registerNewObjective(objective + "buffer", "dummy");

        setTitle(title);
    }

    public void setTitle(String title) {
        if(buffer.getDisplayName().equals(title) && objective.getDisplayName().equals(title)) return;
        buffer.setDisplayName(title);
        objective.setDisplayName(title);
    }

    public void setScore(String string, int score) {
        if (lineMap.containsKey(score)) {
            if (lineMap.get(score).equals(string)) return;
            scoreboard.resetScores(lineMap.get(score));
        }
        lineMap.put(score, string);
        buffer.getScore(string).setScore(score);
        swapBuffer();
        buffer.getScore(string).setScore(score);
    }

    public void clearBoard() {
        lineMap.values().forEach(s -> scoreboard.resetScores(s));
        lineMap.clear();
    }

    /*public Team createTeam(String name*//*, Player... members*//*) {
        scoreboard.getTeams().stream().filter(s -> s.getName().equalsIgnoreCase(name)).forEach(Team::unregister);

        scoreboard.registerNewTeam(name);
        Team team = scoreboard.getTeam(name);
        *//*for(Player player : members)
            team.addPlayer(player);*//*
        return team;
    }

    public Team getTeam(String name) {
        Optional<Team> o = scoreboard.getTeams().parallelStream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst();
        return o.isPresent() ? o.get() : null;
    }*/

    public void destroyTeam(String name) {
        scoreboard.getTeams().stream().filter(s -> s.getName().equalsIgnoreCase(name)).forEach(Team::unregister);
    }

    public void setScoreboard(Player p) {
        p.setScoreboard(scoreboard);
    }

    public void destroy() {
        clearBoard();
        objective.unregister();
        buffer.unregister();
        scoreboard.getTeams().forEach(team -> this.destroyTeam(team.getName()));
    }

    private void swapBuffer() {
        buffer.setDisplaySlot(DisplaySlot.SIDEBAR);
        Objective b = objective;
        objective = buffer;
        buffer = b;
    }

}
