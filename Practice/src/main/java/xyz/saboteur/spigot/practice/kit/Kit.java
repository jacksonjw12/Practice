package xyz.saboteur.spigot.practice.kit;

import xyz.saboteur.spigot.util.Loadout;

public class Kit {
    private String name;
    private Loadout loadout;

    public Kit(String name, Loadout loadout) {
        this.name = name;
        this.loadout = loadout;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Loadout getLoadout() {
        return loadout;
    }

    public void setLoadout(Loadout loadout) {
        this.loadout = loadout;
    }

    public static Kit fromString(String s) {
        if(!s.contains("\\|")) return null;
        return new Kit(s.split("\\|")[0], Loadout.fromString(s.split("\\|")[1]));
    }

    public String toString() {
        return getName() + "|" + getLoadout().toString();
    }
}
