import java.io.IOException;
import java.lang.reflect.Array;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


public class CrawlerVG {
	public static CrawlerVG crawler;
	public SearchSongData searchsong = SearchSongData.getInstance();
	public SearchLyric searchlyric = SearchLyric.getInstance();
	public IO io = IO.getInstance();
	public MongoDB mongodb = MongoDB.getInstance();

	public void getList(String listeURL) throws IOException, EchoNestException, SocketTimeoutException, NullPointerException{
		JSONObject jsonChart = new JSONObject();
		int average =0;
		double danceabilityAverage = 0.0f;
		double durationAverage = 0.0f;
		double energyAverage = 0.0f;
		double loudnessAverage = 0.0f;
		double modeAverage = 0;
		double tempoAverage = 0.0f;
		JSONArray jsonKeyArray = new JSONArray();

		int[] keys = new int[12];

		Document doc = Jsoup.connect(listeURL).get();

		Element table = doc.select("table.chart").first();
		Element topinfo = table.select("th.topinfo").first();
		Element navigation = topinfo.select("li.navigation").first();
		Element selectlistYear = navigation.select("select.listNavigatorYear").first();
		Element selectedYear = selectlistYear.select("option[selected]").first();

		if(selectedYear==null){
			System.out.println("NULL");
		}
		else{


			String year = selectedYear.text();
			Element selectlist = navigation.select("select.listNavigatorWeek").first();
			Element selectedWeek = selectlist.select("option[selected]").first();
			String week = selectedWeek.text();

			if(!mongodb.findChart(year, week)){



				jsonChart.put("week", week);
				jsonChart.put("year", year);
				Element tbody = doc.select("tbody").first();
				Elements topList = tbody.select("tr");
				JSONArray jsonListArray = new JSONArray();
				int i=1;
				for(Element listElement:topList){
					JSONObject jsonListItem = new JSONObject();
					JSONObject jsonSong = new JSONObject();
					Element tdLeft = listElement.select("td").first();

					String link = tdLeft.select("a").first().attr("href");

					String artistString = "Null";
					String titleString = "Null";
					if(link.length()< 5){

					}
					else{
						Document doc2 = Jsoup.connect("http://lista.vg.no/"+link).get();
						Element bioElement = doc2.select("div.bio").first();
						titleString = bioElement.select("h1").first().text();
						artistString = bioElement.select("h2").first().select("a").first().text();
					}




				

						jsonSong.put("artist", artistString);
						jsonSong.put("title", titleString);
						JSONArray jsonSoundSummaryArray = new JSONArray();
						Song echoNestSong = null;
						try {
							echoNestSong = searchsong.searchSongsByArtistAndTitle(artistString, titleString, 5);
						} catch (EchoNestException | InterruptedException e) {
							// TODO Auto-generated catch block
							System.out.println(titleString + " - " + artistString + " har ingen treff på Lyd");
						}
						if(echoNestSong==null){
							System.out.println(echoNestSong);
							jsonSong.put("soundSummary", jsonSoundSummaryArray);
						}
						else{


							JSONObject energy = new JSONObject();
							JSONObject key = new JSONObject();
							JSONObject loudness = new JSONObject();
							JSONObject danceability = new JSONObject();
							JSONObject duration = new JSONObject();
							JSONObject tempo = new JSONObject();
							JSONObject mode = new JSONObject();
							JSONObject timesignature = new JSONObject();

							energy.put("energy", echoNestSong.getEnergy());
							key.put("key", echoNestSong.getKey());
							loudness.put("loudness", echoNestSong.getLoudness());
							danceability.put("danceability", echoNestSong.getDanceability());
							duration.put("duration", echoNestSong.getDuration());
							tempo.put("tempo", echoNestSong.getTempo());
							mode.put("mode", echoNestSong.getMode());
							timesignature.put("timesignature", echoNestSong.getTimeSignature());


							danceabilityAverage += echoNestSong.getDanceability();
							durationAverage += echoNestSong.getDuration();
							energyAverage += echoNestSong.getEnergy();
							loudnessAverage += echoNestSong.getLoudness();
							modeAverage += echoNestSong.getMode();
							tempoAverage += echoNestSong.getTempo();
							keys[echoNestSong.getKey()]++;

							average ++;


							jsonSoundSummaryArray.add(danceability);
							jsonSoundSummaryArray.add(duration);
							jsonSoundSummaryArray.add(energy);
							jsonSoundSummaryArray.add(key);
							jsonSoundSummaryArray.add(loudness);
							jsonSoundSummaryArray.add(mode);
							jsonSoundSummaryArray.add(tempo);
							jsonSoundSummaryArray.add(timesignature);
							jsonSong.put("soundSummary", jsonSoundSummaryArray);
						}

						Lyrics lyric = null;
						try {
							lyric = searchlyric.fuzzySearch(titleString, artistString);
						} catch (MusixMatchException e) {
							// TODO Auto-generated catch block
							System.out.println(titleString + " - " + artistString + " har ingen treff på lyric");
						}
						if(lyric==null){
							jsonSong.put("lyricLanguage", "");
							jsonSong.put("lyric", "");
						}
						else{

							jsonSong.put("lyricLanguage", lyric.getLyricsLang());
							String textLyric = lyric.getLyricsBody();
							textLyric = textLyric.replace("\n", " ").replace("\r", " ")
									.replace("******* This Lyrics is NOT for Commercial use *******", "")
									.replace("...", "");
							jsonSong.put("lyric", textLyric);
						}

						//				io.saveSong(jsonSong);
						mongodb.insertSong(jsonSong.toJSONString());

					


					jsonListItem.put("title", titleString);
					jsonListItem.put("artist", artistString);
					jsonListItem.put("position", i);
					i++;
					jsonListArray.add(jsonListItem);
					jsonChart.put("list", jsonListArray);


				}

				JSONArray jsonSoundSummaryArray = new JSONArray();
				JSONObject energy = new JSONObject();
				JSONObject key = new JSONObject();
				JSONObject loudness = new JSONObject();
				JSONObject danceability = new JSONObject();
				JSONObject duration = new JSONObject();
				JSONObject tempo = new JSONObject();
				JSONObject mode = new JSONObject();

				energy.put("energy", (energyAverage / average));
				loudness.put("loudness", (loudnessAverage / average));
				danceability.put("danceability", (danceabilityAverage / average));
				duration.put("duration", (durationAverage / average));
				tempo.put("tempo", (tempoAverage / average));
				mode.put("mode", (modeAverage /average));


				for(int iter = 0; iter < keys.length; iter++){
					int value = keys[iter];
					JSONObject tempKey = new JSONObject();
					tempKey.put(iter, value);
					jsonKeyArray.add(tempKey);
				}

				JSONObject jsonKey = new JSONObject();

				jsonKey.put("key", (jsonKeyArray));
				
				jsonSoundSummaryArray.add(danceability);
				jsonSoundSummaryArray.add(duration);
				jsonSoundSummaryArray.add(energy);
				jsonSoundSummaryArray.add(loudness);
				jsonSoundSummaryArray.add(mode);
				jsonSoundSummaryArray.add(tempo);
				jsonSoundSummaryArray.add(jsonKey);


System.out.println(average + " ER ANTALLET");

				jsonChart.put("soundSummary",jsonSoundSummaryArray );
				//		io.saveList(jsonliste);
				mongodb.insertChart(jsonChart.toJSONString());
			}
		}
	}
	public static CrawlerVG getInstance(){
		if (crawler == null){
			crawler = new CrawlerVG();
			return crawler;
		}
		else return crawler;
	}

}
