package xyz.saboteur.spigot.scoreboard;

public class SBGroup {
    private String group, tag;

    public SBGroup(String group, String tag) {
        this.group = group;
        this.tag = tag;
    }

    public String getGroup() {
        return group;
    }
    public String getTag() {
        return tag;
    }
}
