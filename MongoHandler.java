import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoHandler {


    public static String mongo(String classification, String jsonDocument){

        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);

            MongoDatabase db = mongoClient.getDatabase("boss_classifications");
            System.out.println("Connect to database successfully");


            MongoCollection<Document> collection = db.getCollection(classification);

            /*
            List<Document> documents = new ArrayList<Document>();
            for (int i = 0; i < 100; i++) {
                documents.add(new Document("i", i));
            }
            */

            Document newDoc = new Document();
            collection.insertOne(newDoc.parse(jsonDocument));

            Document myDoc = collection.find().first();
            System.out.println("JSON FROM MONGO: " + myDoc.toJson());

        }catch(Exception e){
            e.getStackTrace();
        }
        return "loginSuccessful";
    }

}
