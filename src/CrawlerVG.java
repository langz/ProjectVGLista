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


public class CrawlerVG {
	public static CrawlerVG crawler;
	public SearchSongData searchsong = SearchSongData.getInstance();
	public SearchLyric searchlyric = SearchLyric.getInstance();
	public IO io = IO.getInstance();
	public MongoDB mongodb = MongoDB.getInstance();

	public void getList(String listeURL) throws IOException, EchoNestException, SocketTimeoutException, NullPointerException{
		JSONObject jsonliste = new JSONObject();
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
		jsonliste.put("year", year);
		Element selectlist = navigation.select("select.listNavigatorWeek").first();
		Element selectedWeek = selectlist.select("option[selected]").first();
		String week = selectedWeek.text();
		jsonliste.put("week", week);

		Element tbody = doc.select("tbody").first();
		Elements topList = tbody.select("tr");
		JSONArray jsonlisteArray = new JSONArray();
		int i=1;
		for(Element listeElement:topList){
			JSONObject jsonListItem = new JSONObject();
			JSONObject jsonSong = new JSONObject();
			Element tdLeft = listeElement.select("td").first();

			String link = tdLeft.select("a").first().attr("href");
			
			String artist = "Null";
			String title = "Null";
			if(link.length()< 5){
				
			}
			else{
				Document doc2 = Jsoup.connect("http://lista.vg.no/"+link).get();
				Element bio = doc2.select("div.bio").first();
				title = bio.select("h1").first().text();
				artist = bio.select("h2").first().select("a").first().text();
			}

			Document doc2 = Jsoup.connect("http://lista.vg.no/"+link).get();


			
			if(!mongodb.findSong(artist, title)){

				jsonSong.put("artist", artist);
				jsonSong.put("title", title);
				JSONArray soundsummary = new JSONArray();
				Song echoNestSong = null;
				try {
					echoNestSong = searchsong.searchSongsByArtistAndTitle(artist, title, 5);
				} catch (EchoNestException | InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println(title + " - " + artist + " har ingen treff på Lyd");
				}
				if(echoNestSong==null){
					jsonSong.put("soundSummary", soundsummary);
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

					soundsummary.add(danceability);
					soundsummary.add(duration);
					soundsummary.add(energy);
					soundsummary.add(key);
					soundsummary.add(loudness);
					soundsummary.add(mode);
					soundsummary.add(tempo);
					soundsummary.add(timesignature);
					jsonSong.put("soundSummary", soundsummary);
				}

				Lyrics lyric = null;
				try {
					lyric = searchlyric.fuzzySearch(title, artist);
				} catch (MusixMatchException e) {
					// TODO Auto-generated catch block
					System.out.println(title + " - " + artist + " har ingen treff på lyric");
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
				
			}
			else{
				System.out.println("FINNES FØR API KALL; WAHTTTUP");
			}

			jsonListItem.put("title", title);
			jsonListItem.put("artist", artist);
			jsonListItem.put("position", i);
			i++;
			jsonlisteArray.add(jsonListItem);
			jsonliste.put("list", jsonlisteArray);


		}
//		io.saveList(jsonliste);
		mongodb.insertChart(jsonliste.toJSONString());
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
