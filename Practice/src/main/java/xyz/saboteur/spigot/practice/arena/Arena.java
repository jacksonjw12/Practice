package xyz.saboteur.spigot.practice.arena;

import org.bukkit.Location;

import java.util.*;

public class Arena {
    private String name, builder;
    private Map<Integer, List<Location>> spawns;

    public Arena(String name, String builder) {
        this.name = name;
        this.builder = builder;
        this.spawns = new HashMap<>();
    }

    public Arena addSpawn(int team, Location... locs) {
        List<Location> locations = spawns.containsKey(team) ? spawns.get(team) : new ArrayList<>();
        locations.addAll(Arrays.asList(locs));
        spawns.put(team, locations);
        return this;
    }

    public String getName() {
        return name;
    }

    public String getBuilder() {
        return builder;
    }

    public Map<Integer, List<Location>> getSpawns() {
        return spawns;
    }

    public boolean isSetup() {
        return spawns.size() >= 2;
    }

    public List<Location> getSpawns(int team) {
        return spawns.get(team);
    }
}
