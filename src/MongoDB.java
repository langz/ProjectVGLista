import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	public DBCollection summaryArtist;
	public DBCollection summaryArtistTopUnik;
	public DBCollection summaryArtistTopAntall;
	public DBCollection summaryArtistTopDuration;
	public DBCollection summaryArtistTopLoudness;
	public DBCollection summaryArtistTopDanceability;
	public DBCollection summaryArtistTopMode;
	public DBCollection summaryArtistTopTempo;
	public DBCollection summaryArtistTopEnergy;
	public DBCollection summaryArtistTopTimesignature;
	public DBCollection summarySongTopUnik;
	public DBCollection summarySongTopAntall;
	public DBCollection summarySongTopTempo;
	public DBCollection summarySongTopDuration;
	public DBCollection summarySongTopDanceability;
	public DBCollection summarySongTopLoudness;
	public DBCollection summarySongTopMode;
	public DBCollection summarySongTopEnergy;
	public DBCollection summarySongTopTimesignature;
	public DBCollection summaryYear;
	public DBCollection summaryDecade;
	public DBCollection summary;
	public HashSet<String> m_Words;
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
		summaryArtist = db.getCollection("summaryArtist");
		summaryArtistTopUnik = db.getCollection("summaryArtistTopUnik");
		summaryArtistTopAntall = db.getCollection("summaryArtistTopAntall");
		summaryArtistTopDuration = db.getCollection("summaryArtistTopDuration");
		summaryArtistTopLoudness = db.getCollection("summaryArtistTopLoudness");
		summaryArtistTopMode = db.getCollection("summaryArtistTopMode");
		summaryArtistTopEnergy = db.getCollection("summaryArtistTopEnergy");
		summaryArtistTopTimesignature = db.getCollection("summaryArtistTopTimesignature");
		summaryArtistTopDanceability = db.getCollection("summaryArtistTopDanceability");
		summaryArtistTopTempo = db.getCollection("summaryArtistTopTempo");

		summarySongTopDanceability = db.getCollection("summarySongTopDanceability");
		summarySongTopUnik = db.getCollection("summarySongTopUnik");
		summarySongTopAntall = db.getCollection("summarySongTopAntall");
		summarySongTopDuration = db.getCollection("summarySongTopDuration");
		summarySongTopLoudness = db.getCollection("summarySongTopLoudness");
		summarySongTopMode = db.getCollection("summarySongTopMode");
		summarySongTopEnergy = db.getCollection("summarySongTopEnergy");
		summarySongTopTimesignature = db.getCollection("summarySongTopTimesignature");
		summarySongTopTempo = db.getCollection("summarySongTopTempo");
		summaryDecade = db.getCollection("summaryDecade");
		summaryYear = db.getCollection("summaryYear");
		summary = db.getCollection("summary");
	}

	public void funkerDette(){
		for(int i=1960; i<2015; i++){
			HashMap<String, Long> hitLastingPower = new HashMap<String, Long>();
			int antallListermedSanger = 0;
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

					antallListermedSanger++;

					BasicDBList sanger = (BasicDBList) temp.get("list");
					for(int a=0;a<sanger.size();a++){
						BasicDBObject sang =  (BasicDBObject) sanger.get(a);
						if(hitLastingPower.containsKey(sang.get("title").toString()+sang.get("artist").toString())){
							hitLastingPower.put(sang.get("title").toString()+sang.get("artist").toString(), 
									hitLastingPower.get(sang.get("title").toString()+sang.get("artist").toString())+1);
						}
						else{
							hitLastingPower.put(sang.get("title").toString()+sang.get("artist").toString(),(long) 1);
						}
					}


					BasicDBList dbc = (BasicDBList) temp.get("soundSummary");

					if(dbc.size()!=0){
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
					}
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
			Long totalHitLasting = (long) 0;
			for (Map.Entry<String, Long> s : hitLastingPower.entrySet()) {
				totalHitLasting += s.getValue();
			}


			document.put("hitlasting", totalHitLasting/hitLastingPower.size());
			System.out.println(totalHitLasting + " / " + hitLastingPower.size());

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
			int timesignature =  0;
			int years = 0;
			int songs = 0;
			int charts = 0;
			int max =10;
			Long hitlasting = (long) 0;
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
				timesignature +=Integer.parseInt(chart.get("timesignature").toString());
				songs +=Integer.parseInt(chart.get("songs").toString());
				charts +=Integer.parseInt(chart.get("charts").toString());
				hitlasting +=Long.parseLong(chart.get("charts").toString());
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
			timesignature =  timesignature/years;
			mode = mode/years;
			tempo =  tempo/years;
			hitlasting =  hitlasting/years;
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
			document.put("timesignature", timesignature);
			document.put("hitlasting", hitlasting);

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
		System.out.println("ferdig");
	}
	public void updateCharts(){
		DBCursor cursor = charts.find();
		while(cursor.hasNext()) {

			DBObject chart = cursor.next();
			int antall = 0;
			int timeTotal = 0;
			BasicDBList listen = (BasicDBList) chart.get("list");
			for(int ai = 0 ; ai<listen.size(); ai++){
				BasicDBObject sang = (BasicDBObject) listen.get(ai);
				String title = (String)sang.get("title");
				String artist = (String)sang.get("artist");
				BasicDBObject andQuery = new BasicDBObject();
				List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
				obj.add(new BasicDBObject("title", title));
				obj.add(new BasicDBObject("artist", artist));
				andQuery.put("$and", obj);
			 
				DBCursor cursor2 = songs.find(andQuery);
				while (cursor2.hasNext()) {
					DBObject sangen = cursor2.next();
					BasicDBList soundSummary = (BasicDBList) sangen.get("soundSummary");
					if(soundSummary.size() == 0){
						
					}
					else{
						
						DBObject timesig = (DBObject)soundSummary.get(7);
						int timesignature = (int) timesig.get("timesignature");
						antall ++;
						timeTotal+=timesignature;
					}
				}
				
			}
			System.out.println("Delt på " + antall);
			BasicDBList ss = (BasicDBList) chart.get("soundSummary");
			BasicDBObject timesign = new BasicDBObject();
			timesign.put("timesignature", timeTotal/antall);
			ss.add(timesign);
			System.out.println(ss);
			chart.put("soundSummary", ss);
			
		
			charts.save(chart);
		}
		System.out.println("ferdig");
		
	}
	public void updateChartsYear(){
		DBCursor cursor = summaryYear.find();
		while(cursor.hasNext()) {

			DBObject chart = cursor.next();
			int antall = 0;
			int timeTotal = 0;
			int aaar = (int) chart.get("year");
			BasicDBObject whereQuery = new BasicDBObject();
			whereQuery.put("year", ""+ aaar+ "");
			DBCursor cursor2 = charts.find(whereQuery);
			while(cursor2.hasNext()) {
			    DBObject charten = cursor2.next();
			    
				BasicDBList soundSummary = (BasicDBList) charten.get("soundSummary");
				if(soundSummary.size() == 0){
					
				}
				else{
					DBObject timesig = (DBObject)soundSummary.get(7);
					int timesignature = (int) timesig.get("timesignature");
					antall ++;
					timeTotal+=timesignature;
				}

			}
			 
				
			
		
	

			chart.put("timesignature", timeTotal/antall);
			
		
			summaryYear.save(chart);
		}
		System.out.println("ferdig");
		
	}
	public void generateTotal(){

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
		int decades = 0;
		int songs = 0;
		int songsUnique=0; 
		int antallaar = 0;
		int charts = 0;


		DBCursor cursor = summaryDecade.find();
		while(cursor.hasNext()) {

			DBObject dec = cursor.next();


			decades++;
			danceability += Double.parseDouble(dec.get("danceability").toString());
			duration +=Double.parseDouble(dec.get("duration").toString());
			energy +=Double.parseDouble(dec.get("energy").toString());
			loudness +=Double.parseDouble(dec.get("loudness").toString());
			mode +=Double.parseDouble(dec.get("mode").toString());
			tempo +=Double.parseDouble(dec.get("tempo").toString());
			songs +=Integer.parseInt(dec.get("songs").toString());
			songsUnique +=Integer.parseInt(dec.get("songsUnique").toString());
			antallaar +=Integer.parseInt(dec.get("antallaar").toString());
			charts +=Integer.parseInt(dec.get("charts").toString());
			BasicDBList lyricSummary = (BasicDBList) dec.get("lyricSummary");

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

			BasicDBList keySummary = (BasicDBList) dec.get("key");
			for(int iter = 0 ; iter < keySummary.size() ; iter++ ){
				DBObject keyObj = (DBObject) keySummary.get(iter);
				int verdi = Integer.parseInt(keyObj.get(iter+"").toString());
				keys[iter]+=verdi;

			}


		}
		danceability = danceability/decades;
		duration =  duration/decades;
		energy =  energy/decades;
		loudness =  loudness/decades;
		mode = mode/decades;
		tempo =  tempo/decades;
		BasicDBObject document = new BasicDBObject();
		document.put("songs",songs);
		document.put("charts",charts);
		document.put("antallaar", antallaar);
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
		summary.insert(document);


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

	public void generateTop10ArtistAntallListet(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("antall", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			summaryArtistTopAntall.insert(obj);
			System.out.println(obj);
			i++;	
		}
	}
	public void generateTop10ArtistAntallUnike(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("antallunikesanger", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopUnik.insert(obj);
			i++;	
		}
	}
	public void generateTop10ArtistDanceability(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("danceability", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopDanceability.insert(obj);
			i++;	
		}
	}
	public void generateTop10ArtistDuration(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("duration", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopDuration.insert(obj);
			i++;	
		}
	}
	public void generateTop10ArtistLoudness(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("loudness", 1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopLoudness.insert(obj);
			i++;	
		}
	}
	public void generateTop10ArtistMode(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("mode", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopMode.insert(obj);
			i++;	
		}
	}
	public void generateTop10ArtistEnergy(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("energy", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopEnergy.insert(obj);
			i++;	
		}
	}
	public void generateTop10ArtistTempo(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("tempo", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopTempo.insert(obj);
			i++;	
		}
	}
	public void generateTop10ArtistTimesignature(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("timesignature", -1);
		DBCursor cursor = summaryArtist.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summaryArtistTopTimesignature.insert(obj);
			i++;	
		}
	}
	//HER STARTER SONGS

	public void generateTop10SongsAntallListet(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("antall", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			summaryArtistTopAntall.insert(obj);
			System.out.println(obj);
			i++;	
		}
	}
	public void generateTop10SongsDanceability(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("soundSummary.0.danceability", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summarySongTopDanceability.insert(obj);
			i++;	
		}
	}
	public void generateTop10SongsDuration(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("soundSummary.1.duration", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summarySongTopDuration.insert(obj);
			i++;	
		}
	}
	public void generateTop10SongsLoudness(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("soundSummary.4.loudness", 1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			BasicDBList lol = (BasicDBList) obj.get("soundSummary");
			if(lol.size()!=0){
		summarySongTopLoudness.insert(obj);

			i++;	
			}
		}
	}
	public void generateTop10SongsMode(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("soundSummary.5.mode", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summarySongTopMode.insert(obj);
			i++;	
		}
	}
	public void generateTop10SongsEnergy(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("soundSummary.2.energy", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summarySongTopEnergy.insert(obj);
			i++;	
		}
	}
	public void generateTop10SongsTempo(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("soundSummary.6.tempo", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summarySongTopTempo.insert(obj);
			i++;	
		}
	}
	public void generateTop10SongsAntall(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("antall", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summarySongTopAntall.insert(obj);
			i++;	
		}
	}
	public void generateTop10SongsTimesignature(){
		BasicDBObject orderBy = new BasicDBObject();
		orderBy.put("soundSummary.7.timesignature", -1);
		DBCursor cursor = songs.find().sort(orderBy);
		int i = 0;
		while(cursor.hasNext() && i<20) {
			DBObject obj = cursor.next();
			System.out.println(obj);
			summarySongTopTimesignature.insert(obj);
			i++;	
		}
	}
	public void generateSongAntallBestPlass(){
		DBCursor cursor = songs.find();
		int match = 0;
		int teller = 0;
		while(cursor.hasNext()) {
			teller ++;
			DBObject obj = cursor.next();

			String artistnavn = (String) obj.get("artist");
			String sangnavn = (String) obj.get("title");
			BasicDBObject mainQ = new BasicDBObject();
			BasicDBObject andQuery = new BasicDBObject();
			BasicDBObject whereQ = new BasicDBObject();

			whereQ.put("artist", artistnavn);
			whereQ.put("title", sangnavn);

			andQuery.put("$elemMatch", whereQ);

			mainQ.put("list", andQuery);

			int antallListet = 0;
			int bestpos = 20;
			DBCursor cursor2 = charts.find(mainQ);
			antallListet = cursor2.size();
BasicDBList yearsListed = new BasicDBList();
HashSet<String> years = new HashSet<>();
			while(cursor2.hasNext()) {
				

				
				DBObject obj2 = cursor2.next();
				
				String aartext = (String) obj2.get("year");
				years.add(aartext);
				
				
				BasicDBList list = (BasicDBList) obj2.get("list");


				for(int o = 0 ; o < list.size() ; o++){
					BasicDBObject obj3 = (BasicDBObject) list.get(o);
					int currPos = (int) obj3.get("position");
					if(obj3.get("artist").equals(artistnavn) && obj3.get("title").equals(sangnavn) && currPos < bestpos){
						bestpos = currPos;

					}
				}

			}
			
			for (String s : years) {
				yearsListed.add(s);
			}
			obj.put("years", yearsListed);
			obj.put("antall",antallListet );
			obj.put("bestPos", bestpos);
			BasicDBList bow = (BasicDBList) obj.get("bow");	
			BasicDBList newBow = new BasicDBList();
			if(bow.size()!=0){
				int lengden = bow.size();
				int hits = 0;
				for(int a = 0 ; a < lengden ; a ++){

					BasicDBList bowElem = (BasicDBList) bow.get(a);

					String ord = (String) bowElem.get(0);
					int verdi = (int) bowElem.get(1);
					String trimLow = ord.trim().toLowerCase();

					if(Stopwords.isStopword(trimLow)){
						System.out.println("stoppord");
						hits++;
					}
					else{
						BasicDBList bowElemIns = new BasicDBList();
						int bowElemInsVerdi = verdi;

						for(int as = 0 ; as < lengden ; as ++){

							BasicDBList bowElemNew = (BasicDBList) bow.get(as);

							String ordNew = (String) bowElemNew.get(0);
							int verdiNew = (int) bowElemNew.get(1);
							String trimLowNew = ordNew.trim().toLowerCase();
							if(trimLow.equals(trimLowNew) && as!=a){
								verdi+=verdiNew;
								System.out.println(bow);
								bow.remove(as);
								System.out.println(bow);
								lengden = lengden -1;
								hits++;
								System.out.println("har hitt på: " + trimLow + "=" + trimLowNew);
							}

						}
						bowElemIns.add(trimLow);
						bowElemIns.add(verdi);
						newBow.add(bowElemIns);


					}

				}
	

			}
			obj.put("bow", newBow);
	
			//		try {
			//		    Thread.sleep(1500);                 //1000 milliseconds is one second.
			//		} catch(InterruptedException ex) {
			//		    Thread.currentThread().interrupt();
			//		}
		songs.save(obj);;
			
		}
		System.out.println("Ferdi");
	}

	public void stopWordForSummaryArtist(){
		int match = 0;
		int tell = 0;
		DBCursor cursorDoc = summaryArtist.find();
		while (cursorDoc.hasNext()) {
			tell++;
			DBObject obj = cursorDoc.next();

			BasicDBList bow = (BasicDBList) obj.get("bow");	

			if(bow.size()!=0){
				System.out.println(bow);
				for(int a = 0 ; a < bow.size() ; a ++){
					BasicDBList bowElem = (BasicDBList) bow.get(a);

					String ord = (String) bowElem.get(0);


					if(Stopwords.isStopword(ord)){
						match++;
						System.out.println("match :" + ord);
						System.out.println(bow.remove(a));;


					}
					else{

					}

				}

			}
			BasicDBList songsyears = (BasicDBList) obj.get("sanger");
			HashSet<String> songsyearset = new HashSet<String>();
			for(int a = 0 ; a < songsyears.size(); a++){
				DBObject objS = (DBObject) songsyears.get(a);
				String artistnavn = (String) objS.get("artist");
				String sangnavn = (String) objS.get("title");
				BasicDBObject mainQ = new BasicDBObject();
				BasicDBObject andQuery = new BasicDBObject();
				BasicDBObject whereQ = new BasicDBObject();

				whereQ.put("artist", artistnavn);
				whereQ.put("title", sangnavn);

				andQuery.put("$elemMatch", whereQ);

				mainQ.put("list", andQuery);

				DBCursor cursor2 = charts.find(mainQ);
				while(cursor2.hasNext()){
					DBObject objektet = cursor2.next();
					String aaret = (String) objektet.get("year");
					songsyearset.add(aaret);
				}
			}
			BasicDBList yearsListed = new BasicDBList();
			for (String s : songsyearset) {
				yearsListed.add(s);
			}
			obj.put("years", yearsListed);
			System.out.println(tell +"/"+ cursorDoc.size());
			obj.put("bow", bow);
			summaryArtist.save(obj);
		}
		System.out.println("antall match " + match);
	}
	public void generateArtistData(){
		HashSet<String> artister = new HashSet<>();
		int i=0;
		DBCursor cursorDoc = songs.find();
		while (cursorDoc.hasNext()) {
			i++;
			String artistnavn = (String) cursorDoc.next().get("artist");
			artister.add(artistnavn);
			System.out.println( artistnavn +" " +i +"/" +cursorDoc.size());

		}
		int teller = 0;
		for(String artistnavnet : artister){
			teller ++;
			System.out.println( artistnavnet +" " +teller +"/" +artister.size());
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
			double timesignature = 0;

			BasicDBObject whereQuery = new BasicDBObject();
			whereQuery.put("artist", artistnavnet);
			DBCursor songsDoc = songs.find(whereQuery);
			HashSet<String> unikeSanger = new HashSet<>();

			BasicDBObject artistObject = new BasicDBObject();
			artistObject.put("artist", artistnavnet);
			BasicDBList artistsanger = new BasicDBList();
			int antall =0;


			while (songsDoc.hasNext()) {
				DBObject sang = songsDoc.next();
				String tittelen = (String) sang.get("title");
				if(unikeSanger.add(tittelen)){


					BasicDBObject mainQ = new BasicDBObject();
					BasicDBObject andQuery = new BasicDBObject();
					BasicDBObject whereQ = new BasicDBObject();

					whereQ.put("artist", artistnavnet);
					whereQ.put("title", tittelen);

					andQuery.put("$elemMatch", whereQ);

					mainQ.put("list", andQuery);


					DBCursor numberofSongs = charts.find(mainQ);
					int antallganger = numberofSongs.size();
					antall+= antallganger;
					sang.put("antall", antallganger);
					artistsanger.add(sang);
				}
			}
			artistObject.put("antall", antall);

			int sangerMedSoundSummary = 0;

			for(int sangid = 0; sangid<artistsanger.size(); sangid++){

				BasicDBObject sangObj = (BasicDBObject) artistsanger.get(sangid);
				BasicDBList dbc = (BasicDBList) sangObj.get("soundSummary");

				if(dbc.size()!=0){
					sangerMedSoundSummary+=1;


					BasicDBObject danceabilityObj =  (BasicDBObject) dbc.get(0);
					danceability += Double.parseDouble(danceabilityObj.get("danceability").toString());

					BasicDBObject durationObj =  (BasicDBObject) dbc.get(1);
					duration += Double.parseDouble(durationObj.get("duration").toString());

					BasicDBObject energyObj =  (BasicDBObject) dbc.get(2);
					energy += Double.parseDouble(energyObj.get("energy").toString());

					BasicDBObject loudnessObj =  (BasicDBObject) dbc.get(4);
					loudness += Double.parseDouble(loudnessObj.get("loudness").toString());

					BasicDBObject modeObj =  (BasicDBObject) dbc.get(5);
					mode += Double.parseDouble(modeObj.get("mode").toString());

					BasicDBObject tempoObj =  (BasicDBObject) dbc.get(6);
					tempo += Double.parseDouble(tempoObj.get("tempo").toString());

					BasicDBObject singObj =  (BasicDBObject) dbc.get(7);
					timesignature += Double.parseDouble(singObj.get("timesignature").toString());

					BasicDBObject keyObj =  (BasicDBObject) dbc.get(3);
					int keyvalue = Integer.parseInt(keyObj.get("key").toString());
					keys[keyvalue]++;




				}

				BasicDBList dbl = (BasicDBList) sangObj.get("bow");
				if(dbl.size()!=0){

					for(int iter = 0 ; iter < dbl.size() ; iter++ ){

						BasicDBList tempLyric = (BasicDBList) dbl.get(iter);
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

				}

			}
			if(danceability != 0){

				danceability = danceability/sangerMedSoundSummary;
			}
			if(duration != 0){
				duration =  duration/sangerMedSoundSummary;

			}
			if(energy != 0){

				energy =  energy/sangerMedSoundSummary;
			}
			if(loudness != 0){
				loudness =  loudness/sangerMedSoundSummary;

			}
			if(mode != 0){

				mode = mode/sangerMedSoundSummary;
			}
			if(tempo != 0){

				tempo =  tempo/sangerMedSoundSummary;
			}
			if(timesignature != 0){

				timesignature =  timesignature/sangerMedSoundSummary;
			}

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
			artistObject.put("antallunikesanger", artistsanger.size());
			DBObject dbObject = (DBObject) JSON.parse(lyricSummaryArray.toJSONString());
			DBObject keyArrayObj = (DBObject) JSON.parse(jsonKeyArray.toJSONString());
			artistObject.put("danceability", danceability);
			artistObject.put("duration", duration);
			artistObject.put("energy", energy);
			artistObject.put("key", keyArrayObj);
			artistObject.put("loudness", loudness);
			artistObject.put("mode", mode);
			artistObject.put("tempo", tempo);
			artistObject.put("timesignature", timesignature);
			artistObject.put("bow", dbObject);
			artistObject.put("sanger",artistsanger );


			summaryArtist.insert(artistObject);

		}




	}

	public static MongoDB getInstance(){
		if (mongodb == null){
			mongodb = new MongoDB();
			return mongodb;
		}
		else return mongodb;
	}

}
