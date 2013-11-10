package be.pursuit.witrack.mongo;

import com.mongodb.MongoClient;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import javax.inject.Singleton;

/**
 * @author Jo Voordeckers - jo.voordeckers@pursuit.be
 */
@Singleton
public class Database {

    private MongoClient mongoClient;
    private com.mongodb.DB db;
    private Jongo jongo;
    private MongoCollection scans;
    private MongoCollection positions;
    private MongoCollection scanners;

    public void init() {
        try {
            mongoClient = new MongoClient();
            db = mongoClient.getDB("witrack");
            jongo = new Jongo(db);

            // Define Jongo collections
            scans = jongo.getCollection("scans");
            positions = jongo.getCollection("positions");
            scanners = jongo.getCollection("scanners");

            // Define indices
            scans.ensureIndex("{ lastSeen: 1 }");
            scanners.ensureIndex("{ scannerId: 1 }", "{ unique: true }");
            positions.ensureIndex("{ deviceId: 1 }");
            positions.ensureIndex("{ time: 1 }");
            positions.ensureIndex("{ deviceId: 1, time: 1 }");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    public void stop() {
        mongoClient.close();
    }

    public MongoCollection scans() {
        return scans;
    }

    public MongoCollection positions() {
        return positions;
    }

    public MongoCollection scanners() {
        return scanners;
    }

}
