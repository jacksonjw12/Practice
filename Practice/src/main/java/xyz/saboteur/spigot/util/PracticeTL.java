package xyz.saboteur.spigot.util;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.saboteur.spigot.Practice;

public enum PracticeTL {
    JOINED_QUEUE("&eYou are now in the matchmaking queue for the &a{ladder} *&eladder."),
    YOUR_ELO(" &eYou have &b{elo} ELO&e."),
    HELP__TOP(" &e---- &6{type} Help &e-- &6Page &c{page}&6/&c{maxpage} &e----"),
    HELP__DESCRIPTION("&e/{group} {cmd} &6» &f{description}"),
    HELP__NEXTPAGE("&6Type &c/{group} help {page} &6to read the next page."),
    NO_PERMISSION("&c&l(!) &cYou do not have permission to execute that command!"),
    KIT__TITLE("Manage {type} kits"),
    KIT__EDITING("&aNow editing kits for {name}"),
    KIT__SAVED("&aSaved kit: &6{name}"),
    KIT__LOADED("&aLoaded kit: &6{name}"),
    KIT__DELETED("&cDeleted kit: &6{name}"),
    KIT__DEFAULT_NAME("Custom {type-colorless} kit {number}"),
    KIT__RENAME__NEW("&aType a new name for &6{name}"),
    KIT__RENAME__CANCEL("&cCanceled rename of &6{name}"),
    KIT__RENAME__SUCCESS("&6{old} &arenamed to: &6{new}"),
    KIT__ITEMS__SAVE("&aSave kit: &6{name}"),
    KIT__ITEMS__SAVE_PREFIX("Save kit: "),
    KIT__ITEMS__LOAD("&eLoad kit: &6{name}"),
    KIT__ITEMS__LOAD_PREFIX("Load kit: "),
    KIT__ITEMS__RENAME("&eRename kit: &6{name}"),
    KIT__ITEMS__RENAME_PREFIX("Rename kit: "),
    KIT__ITEMS__DELETE("&cDelete kit: &6{name}"),
    KIT__ITEMS__DELETE_PREFIX("Delete kit: "),
    ITEMS__RANKED("&aRanked Games"),
    ITEMS__UNRANKED("&cUnranked Games"),
    ITEMS__KIT_EDITOR("&eKit Editor"),
    ITEMS__LEAVE_QUEUE("&cLeave Queue"),
    DUEL__REQUEST_SENT("&6Sent a duel request to {player} ({type})"),
    DUEL__REQUEST_RECEIVED("&6{player} has requested to duel you ({type}). &a[Click to accept]"),
    DUEL__STARTING("&eDuel starting with &a{player}"),
    DUEL__UNAVAILABLE("&cThat duel isn't available!"),
    MATCH__FOUND_RANKED("&eMatch found. You are now fighting &b{name} &ewith kit &b{type}&e, they have &b{elo} ELO&e in this ladder."),
    MATCH__FOUND_UNRANKED("&eMatch found. You are now fighting &b{name} &ewith kit &b{type}&e."),
    MATCH__MAP("&eYou are playing in arena &b{name} &ecreated by &b{player}&e."),
    MATCH__STARTING_TIME("&6&oThe match will begin in &e&o{seconds} &6&osecond{second-s}."),
    MATCH__STARTED("&6&oThe match has started."),
    MATCH__ELO_CHANGES("&eElo Changes: &a{winner} +{winner-change} ({winner-elo}) | &c{loser} -{loser-change} ({loser-elo})"),
    QUEUE__JOINED("&eYou are now in the matchmaking queue for the &a{type} &eladder."),
    QUEUE_LEFT("&cYou left the queue!"),
    QUEUE__SEARCHING_RANKED("&eSearching for a fight.. &b[{elo-min} - {elo-max} ELO]"),
    QUEUE__SEARCHING_UNRANKED("&eSearching for a fight.."),
    QUEUE_UNABLE("&c&l(!) &cCouldn't find a game, removing you from the queue!")
    ;

    private final String message;

    PracticeTL(String message) {
        this.message = Practice.get().getConfig().getString("messages." + name().replaceAll("_", ".").toLowerCase(), message);
    }

    public void send(CommandSender sender, Object... replaced) {
        PracticeUtil.send(sender, PracticeUtil.format(message, replaced));
    }

    public String get(Object... replaced) {
        return PracticeUtil.format(message, replaced);
    }

    @Override
    public String toString() {
        return message;
    }

    public static void writeToConfig(FileConfiguration config) {
        for(PracticeTL tl : values())
            config.set("messages." + tl.name().replaceAll("__", ".").toLowerCase(), tl.toString());
    }
}
