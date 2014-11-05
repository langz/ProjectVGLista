import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.echonest.api.v4.EchoNestException;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MongoDB {
	public static MongoDB mongodb;
	public Mongo mongo;
	public DB db;
	public DBCollection charts;
	public DBCollection songs;
	public DBCollection summaryYear;
	public DBCollection summaryDecade;
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
		summaryYear = db.getCollection("summaryYear");
		summaryDecade = db.getCollection("summaryDecade");
	}

	public void funkerDette(){
		for(int i=1960; i<2015; i++){
			HashMap<String, Long> lyricSummaryMap = new HashMap<String, Long>();
			JSONArray lyricSummaryArray = new JSONArray();
			JSONArray jsonKeyArray = new JSONArray();
			int[] keys = new int[12];
			int antallLister =0;
			int songs =0;
			double danceability =  0.0f;
			double duration =  0.0f;
			double energy =  0.0f;
			double loudness =  0.0f;
			double mode = 0;
			double tempo =  0.0f;

			DBCursor cursorDoc = charts.find();
			while (cursorDoc.hasNext()) {

				DBObject temp = cursorDoc.next();
				int tempint = Integer.parseInt(temp.get("year").toString());
				if(tempint==i){

					BasicDBList dbc = (BasicDBList) temp.get("soundSummary");
					BasicDBObject danceabilityObj =  (BasicDBObject) dbc.get(0);
					danceability += Double.parseDouble(danceabilityObj.get("danceability").toString());

					BasicDBObject durationObj =  (BasicDBObject) dbc.get(1);
					duration += Double.parseDouble(durationObj.get("duration").toString());

					BasicDBObject energyObj =  (BasicDBObject) dbc.get(2);
					energy += Double.parseDouble(energyObj.get("energy").toString());

					BasicDBObject loudnessObj =  (BasicDBObject) dbc.get(3);
					loudness += Double.parseDouble(loudnessObj.get("loudness").toString());

					BasicDBObject modeObj =  (BasicDBObject) dbc.get(4);
					mode += Double.parseDouble(modeObj.get("mode").toString());

					BasicDBObject tempoObj =  (BasicDBObject) dbc.get(5);
					tempo += Double.parseDouble(tempoObj.get("tempo").toString());

					BasicDBList songsList = (BasicDBList) temp.get("list");
					songs+=songsList.size();
					antallLister++;

					BasicDBList lyricSummary = (BasicDBList) temp.get("lyricSummary");

					for(int iter = 0 ; iter < lyricSummary.size() ; iter++ ){

						BasicDBList tempLyric = (BasicDBList) lyricSummary.get(iter);
						String ord = tempLyric.get(0).toString();
						long verdi = Long.parseLong(tempLyric.get(1).toString());

						//Hvis ordet finnes i ordene i listen, så legges den nye verdien til den eksisterende verdien
						if(lyricSummaryMap.containsKey(ord)){
							lyricSummaryMap.put(ord, (lyricSummaryMap.get(ord) + verdi));
						}
						//Hvis ikke ordet finnes, så legges til ordet og dens verdi
						else{
							lyricSummaryMap.put(ord,verdi);
						}

					}
					BasicDBObject keySum = (BasicDBObject) dbc.get(6);
					BasicDBList keySummary = (BasicDBList) keySum.get("key");
					for(int iter = 0 ; iter < keySummary.size() ; iter++ ){
						DBObject keyObj = (DBObject) keySummary.get(iter);
						int verdi = Integer.parseInt(keyObj.get(iter+"").toString());
						keys[iter]+=verdi;
						
					}
				}
			}
			danceability = danceability/antallLister;
			duration =  duration/antallLister;
			energy =  energy/antallLister;
			loudness =  loudness/antallLister;
			mode = mode/antallLister;
			tempo =  tempo/antallLister;
			BasicDBObject document = new BasicDBObject();
			document.put("year", i);
			document.put("songs", songs);
			document.put("charts", antallLister);
			document.put("danceability", danceability);
			document.put("duration", duration);
			document.put("energy", energy);
			document.put("loudness", loudness);
			document.put("mode", mode);
			document.put("tempo", tempo);

			for (Map.Entry<String, Long> entry : lyricSummaryMap.entrySet()) {
				String ord = entry.getKey();
				Long value = entry.getValue();
				JSONArray tempArray = new JSONArray();

				tempArray.add(ord);
				tempArray.add(value);
				lyricSummaryArray.add(tempArray);

			}
			for(int iter = 0; iter < keys.length; iter++){
				int value = keys[iter];
				JSONObject tempKey = new JSONObject();
				tempKey.put(iter, value);
				jsonKeyArray.add(tempKey);
			}
			DBObject keyArrayObj = (DBObject) JSON.parse(jsonKeyArray.toJSONString());
			DBObject lyricSummaryObj = (DBObject) JSON.parse(lyricSummaryArray.toJSONString());
			document.put("lyricSummary", lyricSummaryObj);
			document.put("key", keyArrayObj);
			System.out.println(document);
		summaryYear.insert(document);


		}
	}

	public void generateDecades(){
		for(int dec=1960 ; dec < 2020; dec=dec+10){
			HashMap<String, Long> lyricSummaryMap = new HashMap<String, Long>();
			JSONArray lyricSummaryArray = new JSONArray();
			JSONArray jsonKeyArray = new JSONArray();
			int[] keys = new int[12];
			double danceability =  0.0f;
			double duration =  0.0f;
			double energy =  0.0f;
			double loudness =  0.0f;
			double mode = 0;
			double tempo =  0.0f;
			int years = 0;
			int songs = 0;
			int charts = 0;
			int max =10;
			if(dec==2010){
				max =5;
			}
			for(int year=0 ; year <max ; year++){
				int actualyear = dec +year;
				DBObject chart = findChartSummaryYear(actualyear);

				danceability += Double.parseDouble(chart.get("danceability").toString());
				duration +=Double.parseDouble(chart.get("duration").toString());
				energy +=Double.parseDouble(chart.get("energy").toString());
				loudness +=Double.parseDouble(chart.get("loudness").toString());
				mode +=Double.parseDouble(chart.get("mode").toString());
				tempo +=Double.parseDouble(chart.get("tempo").toString());
				songs +=Integer.parseInt(chart.get("songs").toString());
				charts +=Integer.parseInt(chart.get("charts").toString());
				BasicDBList lyricSummary = (BasicDBList) chart.get("lyricSummary");

				for(int iter = 0 ; iter < lyricSummary.size() ; iter++ ){

					BasicDBList tempLyric = (BasicDBList) lyricSummary.get(iter);
					String ord = tempLyric.get(0).toString();
					long verdi = Long.parseLong(tempLyric.get(1).toString());

					//Hvis ordet finnes i ordene i listen, så legges den nye verdien til den eksisterende verdien
					if(lyricSummaryMap.containsKey(ord)){
						lyricSummaryMap.put(ord, (lyricSummaryMap.get(ord) + verdi));
					}
					//Hvis ikke ordet finnes, så legges til ordet og dens verdi
					else{
						lyricSummaryMap.put(ord,verdi);
					}

				}
				
				BasicDBList keySummary = (BasicDBList) chart.get("key");
				for(int iter = 0 ; iter < keySummary.size() ; iter++ ){
					DBObject keyObj = (DBObject) keySummary.get(iter);
					int verdi = Integer.parseInt(keyObj.get(iter+"").toString());
					keys[iter]+=verdi;
					
				}

				years++;
			}
			danceability = danceability/years;
			duration =  duration/years;
			energy =  energy/years;
			loudness =  loudness/years;
			mode = mode/years;
			tempo =  tempo/years;
			BasicDBObject document = new BasicDBObject();
			document.put("year",dec);
			document.put("songs",songs);
			document.put("charts",charts);
			document.put("antallaar", years);
			document.put("danceability", danceability);
			document.put("duration", duration);
			document.put("energy", energy);
			document.put("loudness", loudness);
			document.put("mode", mode);
			document.put("tempo", tempo);

			for (Map.Entry<String, Long> entry : lyricSummaryMap.entrySet()) {
				String ord = entry.getKey();
				Long value = entry.getValue();
				JSONArray tempArray = new JSONArray();

				tempArray.add(ord);
				tempArray.add(value);
				lyricSummaryArray.add(tempArray);

			}
			
			for(int iter = 0; iter < keys.length; iter++){
				int value = keys[iter];
				JSONObject tempKey = new JSONObject();
				tempKey.put(iter, value);
				jsonKeyArray.add(tempKey);
			}
			DBObject keyArrayObj = (DBObject) JSON.parse(jsonKeyArray.toJSONString());
			DBObject dbObject = (DBObject) JSON.parse(lyricSummaryArray.toJSONString());
			document.put("lyricSummary", dbObject);
			document.put("key", keyArrayObj);
			System.out.println(document);
					summaryDecade.insert(document);

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
	public DBObject getSongInfo(String artist, String title){
		DBObject hit = null;
		BasicDBObject fields = new BasicDBObject();
		fields.put("soundSummary", 1);
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("artist", artist);
		whereQuery.put("title", title);
		DBObject cursor = songs.findOne(whereQuery, fields);
		if(cursor!=null){
			hit = cursor;
			System.out.println(artist + " " + title + " finnes");
		}

		return hit;
	}
	public boolean findChart(String year, String week){
		boolean hit = true;

		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("year", year);
		whereQuery.put("week", week);
		DBObject cursor = charts.findOne(whereQuery);
		if(cursor==null){
			hit = false;
			System.out.println(year + " " + week + " finnes ikke");
		}

		return hit;
	}
	public DBObject findChartSummaryYear(int year){
		DBObject hit = null;

		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("year", year);
		DBObject cursor = summaryYear.findOne(whereQuery);
		if(cursor!=null){
			hit = cursor;
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
