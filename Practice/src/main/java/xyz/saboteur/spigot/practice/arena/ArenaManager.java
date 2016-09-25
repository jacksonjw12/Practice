package xyz.saboteur.spigot.practice.arena;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.util.PracticeUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ArenaManager {
    private YamlConfiguration config;
    private File configFile;
    private Map<String, Arena> arenaMap;

    public ArenaManager(Practice plugin) {
        this.configFile = new File(plugin.getDataFolder(), "arenas.yml");
        if(!this.configFile.exists()) {
            try {
                this.configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        this.arenaMap = new HashMap<>();
        this.loadArenas();
        new CmdArena();
    }

    public void loadArenas() {
        this.arenaMap.clear();
        try {
            this.config.load(this.configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        if(!this.config.contains("arenas")) return;
        this.config.getConfigurationSection("arenas").getKeys(false).forEach(s -> {
            Arena arena = new Arena(s, this.config.getString("arenas." + s + ".builder"));
            if(this.config.contains("arenas." + s + ".spawns"))
                this.config.getConfigurationSection("arenas." + s + ".spawns").getKeys(false).forEach(s2 -> arena.addSpawn(Integer.parseInt(s2), this.config.getStringList("arenas." + s + ".spawns." + s2).stream().map(PracticeUtil::getLocationFromString).collect(Collectors.toList()).toArray(new Location[0])));
            this.arenaMap.put(arena.getName().toLowerCase(), arena);
        });
    }

    public void saveArenas() {
        this.config.set("arenas", null);
        this.arenaMap.values().forEach(arena -> {
            this.config.set("arenas." + arena.getName() + ".builder", arena.getBuilder());
            for(int i = 0; i < arena.getSpawns().size(); i++)
                this.config.set("arenas." + arena.getName() + ".spawns." + i, arena.getSpawns().get(i).stream().map(PracticeUtil::getStringFromLocation).collect(Collectors.toList()));
        });
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Arena create(String name, String builder) {
        Arena arena = new Arena(name, builder);
        this.arenaMap.put(arena.getName().toLowerCase(), arena);
        saveArenas();
        return arena;
    }

    public void delete(String name) {
        this.arenaMap.remove(name.toLowerCase());
        saveArenas();
    }

    public Arena get(String name) {
        return this.arenaMap.get(name.toLowerCase());
    }

    public Collection<Arena> getArenas() {
        return this.arenaMap.values();
    }

    public boolean exists(String name) {
        return this.arenaMap.containsKey(name.toLowerCase());
    }

    public Arena getLeast(List<String> possible) {
        Map<Arena, Integer> counted = new HashMap<>();
        Practice.get().getMatchManager().getMatches().stream().filter(match -> match.getArena() != null && (possible != null && !possible.isEmpty() && possible.contains(match.getArena().getName()))).forEach(match -> counted.put(match.getArena(), counted.getOrDefault(match.getArena(), 0) + 1));
        Optional<Arena> o = counted.entrySet().stream().sorted((e1, e2) -> e1.getValue().compareTo(e2.getValue())).map(Map.Entry::getKey).limit(1).findFirst();
        return o.isPresent() ? o.get() : new ArrayList<>(arenaMap.values()).get(ThreadLocalRandom.current().nextInt(arenaMap.size()));
    }

}
