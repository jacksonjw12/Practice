package xyz.saboteur.spigot.practice.player;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.entity.Player;
import xyz.saboteur.spigot.Practice;
import xyz.saboteur.spigot.practice.gametype.GameType;
import xyz.saboteur.spigot.practice.gametype.GameTypeManager;
import xyz.saboteur.spigot.practice.kit.Kit;
import xyz.saboteur.spigot.util.MongoManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerData {
    private Map<GameType, Integer> ratings;
    private Map<GameType, Kit[]> kits;
    private Player player;

    public PlayerData(Player player) {
        this.player = player;
        this.ratings = new HashMap<>();
        this.kits = new HashMap<>();
        load();
    }

    public void setRating(GameType gt, int rating, boolean save) {
        this.ratings.put(gt, rating);
        if(save) save();
    }

    public void removeKit(GameType gt, int position, boolean save) {
        Kit[] kits = this.kits.get(gt);
        kits[position - 1] = null;
        this.kits.put(gt, kits);
        if(save) save();
    }

    public void setKit(GameType gt, Kit kit, int position, boolean save) {
        setupKit(gt);
        Kit[] kits = this.kits.get(gt);
        kits[position - 1] = kit;
        this.kits.put(gt, kits);
        if(save) save();
    }

    private void load() {
        MongoDatabase db = MongoManager.get().db();
        MongoCollection<Document> players = db.getCollection("players");
        Document r = new Document("uuid", player.getUniqueId());
        Document found = players.find(r).first();
        if(found == null) {
            load(null);
            save();
            return;
        }
        load(found);
    }

    private void load(Document d) {
        Practice plugin = Practice.get();
        GameTypeManager gtManager = plugin.getGameTypeManager();
        if(d != null) {
            this.ratings.putAll(((Map<String, Integer>) d.get("ratings")).entrySet().stream().collect(Collectors.toMap(e -> gtManager.get(e.getKey()), Map.Entry::getValue)));
            this.kits.putAll(((Map<String, String[]>) d.get("kits")).entrySet().stream().collect(Collectors.toMap(e -> gtManager.get(e.getKey()), e -> Arrays.stream(e.getValue()).map(Kit::fromString).collect(Collectors.toList()).toArray(new Kit[5]))));
        }
        gtManager.getGameTypes().stream().filter(gt -> !ratings.containsKey(gt)).forEach(gt -> setRating(gt, 1000, true));
        gtManager.getGameTypes().stream().filter(gt -> !kits.containsKey(gt)).forEach(this::setupKit);
    }

    public void save() {
        MongoDatabase db = MongoManager.get().db();
        MongoCollection<Document> players = db.getCollection("players");
        Document obj = new Document("uuid", player.getUniqueId())
                        .append("ratings", ratings.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getName(), Map.Entry::getValue)))
                        .append("kits", kits.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getName(), e -> Arrays.stream(e.getValue()).map(Kit::toString).collect(Collectors.toList()).toArray(new String[5]))));
        Document r = new Document("uuid", player.getUniqueId());
        Document found = players.find(r).first();
        if (found == null) players.insertOne(obj);else players.updateOne(found, obj);
    }

    public int getRating(GameType gt) {
        return this.ratings.get(gt);
    }

    public void setupKit(GameType gt) {
        if(this.kits.containsKey(gt)) return;
        this.kits.put(gt, new Kit[5]);
    }

    public Map<GameType, Integer> getRatings() {
        return this.ratings;
    }

    public Map<GameType, Kit[]> getKits() {
        return this.kits;
    }

    public Player getPlayer() {
        return this.player;
    }

    public double getAvgElo() {
        int avg = 0;
        for(int i : getRatings().values()) avg += i;
        return avg / getRatings().size();
    }
}
