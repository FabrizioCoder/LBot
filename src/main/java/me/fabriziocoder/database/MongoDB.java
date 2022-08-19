package me.fabriziocoder.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.LoggerFactory;

public class MongoDB {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MongoDB.class);
    static MongoClient client;

    public static void start(String host) {
        client = MongoClients.create(host);
        logger.info("MongoDB client created");
    }

    private static MongoCollection<Document> getCollection() {
        MongoDatabase database = client.getDatabase("luxanna");
        return database.getCollection("summoners");
    }

    public static void addUserProfile(long userId, String summonerName, String summonerPlatform) {
        Document userProfileDocument = new Document("_id", String.valueOf(userId)).append("summonerName", summonerName).append("summonerPlatform", summonerPlatform);
        getCollection().insertOne(userProfileDocument);
    }

    public static boolean removeUserProfile(long userId) {
        if (getCollection().find(new Document("_id", String.valueOf(userId))).first() != null) {
            getCollection().deleteOne(new Document("_id", String.valueOf(userId)));
            return true;
        }
        return false;
    }

    public static com.mongodb.client.FindIterable<Document> userProfileExists(long userId) {
        return getCollection().find(new Document("_id", String.valueOf(userId)));
    }

    public static Document getUserProfile(long userId) {
        return getCollection().find(new Document("_id", String.valueOf(userId))).first();
    }

    public static void updateUserProfile(long userId, String summonerName, String summonerPlatform) {
        getCollection().updateOne(new Document("_id", String.valueOf(userId)), new Document("$set", new Document("summonerName", summonerName).append("summonerPlatform", summonerPlatform)));
    }

}
