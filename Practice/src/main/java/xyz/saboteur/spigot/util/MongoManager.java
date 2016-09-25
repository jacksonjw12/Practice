package xyz.saboteur.spigot.util;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import xyz.saboteur.spigot.Practice;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoManager {
    //private ExecutorService pool = Executors.newCachedThreadPool();
    private static MongoManager instance;
    private Practice plugin;
    private MongoClient client;
    private MongoDatabase database;

    public MongoManager() {
        this.plugin = Practice.get();
        Logger.getLogger("com.mongodb").setLevel(Level.OFF);
        MongoCredential credential = MongoCredential.createCredential(plugin.getConfig().getString("mongo.username"), plugin.getConfig().getString("mongo.database"), plugin.getConfig().getString("mongo.password").toCharArray());
        try {
            client = new MongoClient(new ServerAddress(plugin.getConfig().getString("mongo.host") , plugin.getConfig().getInt("mongo.port")), Collections.singletonList(credential));
        } catch (Exception e) {
            System.out.println("Could not connect to database!");
            e.printStackTrace();
            return;
        }
        database = client.getDatabase(plugin.getConfig().getString("mongo.database"));
    }

    public void destroy() {
        client.close();
    }

    public MongoDatabase db() {
        return database;
    }

    public static MongoManager get() {
        return instance == null ? instance = new MongoManager() : instance;
    }
}
