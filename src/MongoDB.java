import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.echonest.api.v4.EchoNestException;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import org.json.simple.JSONObject;

public class MongoDB {
	public static MongoDB mongodb;
	public Mongo mongo;
	public DB db;
	public DBCollection charts;
	public DBCollection songs;
	public MongoDB(){
		try {
			mongo = new Mongo("localhost", 27017);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = mongo.getDB("vglista");

		charts = db.getCollection("charts");
		songs = db.getCollection("songs");

	}

	public void funkerDette(){
		DBCursor cursorDoc = charts.find();
		while (cursorDoc.hasNext()) {
			System.out.println(cursorDoc.next());
		}
	}

	public void insertSong(String json){

		DBObject dbObject = (DBObject) JSON.parse(json);
		songs.insert(dbObject);
	}

	public void insertChart(String json){

		DBObject dbObject = (DBObject) JSON.parse(json);
		charts.insert(dbObject);
	}
	public boolean findSong(String artist, String title){
boolean hit = true;

		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("artist", artist);
		whereQuery.put("title", title);
		DBObject cursor = songs.findOne(whereQuery);
		if(cursor==null){
			hit = false;
			System.out.println(artist + " " + title + " finnes ikke");
		}
		
		return hit;
	}
	public static MongoDB getInstance(){
		if (mongodb == null){
			mongodb = new MongoDB();
			return mongodb;
		}
		else return mongodb;
	}
}
