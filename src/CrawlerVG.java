import java.io.IOException;
import java.lang.reflect.Array;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.google.gson.JsonArray;
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
	public MusixMatchBOW mmBow = new MusixMatchBOW();

	public void getList(String listeURL) throws IOException, EchoNestException, SocketTimeoutException, NullPointerException{
		//JsonObject som holder på all informasjon om den aktuelle listen.
		JSONObject jsonChart = new JSONObject();
		//integer som holder på antall sanger i listen som har lyd-teknisk-data
		int average =0;
		//doubles som holder på forskjellige verdier for alle sangene med lyd-teknisk data i den aktuelle listen
		double danceabilityAverage = 0.0f;
		double durationAverage = 0.0f;
		double energyAverage = 0.0f;
		double loudnessAverage = 0.0f;
		double modeAverage = 0;
		double tempoAverage = 0.0f;
		JSONArray jsonKeyArray = new JSONArray();
		//HashMap som holder på alle ordene og antall ganger de har blitt brukt i listen.
		HashMap<String, Long> lyricSummaryMap = new HashMap<String, Long>();
		//lyricSummaryArray skal etterhvert bli populert med all data ifra lyricSummaryMap som er over.
		JSONArray lyricSummaryArray = new JSONArray();
		int[] keys = new int[12];

		//Kobler til en VG-Lista liste
		Document doc = Jsoup.connect(listeURL).timeout(10*1000).get(); 
		//Velger ut flere elementer ifra htmlen til listen som trengs senere
		Element table = doc.select("table.chart").first();
		Element topinfo = table.select("th.topinfo").first();
		Element navigation = topinfo.select("li.navigation").first();
		Element selectlistYear = navigation.select("select.listNavigatorYear").first();
		Element selectedYear = selectlistYear.select("option[selected]").first();

		if(selectedYear==null){
			System.out.println("ÅR ER NULL?");
		}
		else{

			//Henter året for den aktuelle listen
			String year = selectedYear.text();
			Element selectlist = navigation.select("select.listNavigatorWeek").first();
			Element selectedWeek = selectlist.select("option[selected]").first();
			//Henter uken for den aktuelle listen
			String week = selectedWeek.text();
			//Hvis ikke sangen allerede finnes i databasen, så crawler vi VG-Lista!
			if(!mongodb.findChart(year, week)){


				//Setter uke og år for listen.
				jsonChart.put("week", week);
				jsonChart.put("year", year);
				Element tbody = doc.select("tbody").first();
				//Velger ut alle TR, tablerows i HTML-siden, hver table-row er en sang i listen
				Elements topList = tbody.select("tr");
				//Oppretter et JSONArray som skal holde på litt informasjon om alle sangene i listen.
				JSONArray jsonListArray = new JSONArray();
				//Starter alltid å iterere fra topp og nedover i listen, derfor er alltid 1-plass først.
				int i=1;
				for(Element listElement:topList){
					//Oppretter JSONObjects, et for elementet som skal lagres i listen og et omfattende song-objekt
					JSONObject jsonListItem = new JSONObject();
					JSONObject jsonSong = new JSONObject();
					//Velger Hver TR har en td med class="left", den inneholder linken.
					Element tdLeft = listElement.select("td").first();
					//link er linken for et element i VG-lista til sub-siden for den aktuelle 
					String link = tdLeft.select("a").first().attr("href");
					//Oppretter artist og title-String, som skal hold epå verdiene for artisten og tittelen på den aktuelle sangen
					String artistString = "";
					String titleString = "";
					if(link.length()< 5){
						//Ikke gjør noe hvis lengden på linken er mindre enn 5, ettersom det ikke finnes noen sang og artist som er mindre enn 5 bokstaver
						//Er nødt til å være her, ettersom det finnes en død-link, som peker til ingensteder på VG-Lista sine sider
					}
					else{
						//Crawler VG-Lista for den aktuelle sangen i listen
						Document doc2 = Jsoup.connect("http://lista.vg.no/"+link).timeout(10*1000).get(); 
						//Velger elementet bio, som holder på informasjon om sangen og artisten
						Element bioElement = doc2.select("div.bio").first();
						try {
							//Prøver å sette titleString til å være et html-element som skal være tittelen på sangen
							titleString = bioElement.select("h1").first().text();

						} catch (NullPointerException e) {
							titleString="";

							// TODO: handle exception
						}
						try {
							//Prøver å sette artistString til å være et html-element som skal være artisten på sangen
							artistString = bioElement.select("h2").first().select("a").first().text();
						} catch (NullPointerException e) {
							// TODO: handle exception
							artistString="";
						}


					}
					//Setter artist og title på den aktuelle sangen
					jsonSong.put("artist", artistString);
					jsonSong.put("title", titleString);
					//Oppretter et JSONArray hvor lydtekniske-data fra EchoNest skal lagres.
					JSONArray jsonSoundSummaryArray = new JSONArray();
					//Oppretter et EchoNest Song object
					Song echoNestSong = null;
					try {
						//Prøver å hente Lyd-teknisk data fra EchoNest APIet
						echoNestSong = searchsong.searchSongsByArtistAndTitle(artistString, titleString, 5);
					} catch (EchoNestException | InterruptedException e) {
						//Har ingen treff på lyd-teknisk data
					}
					if(echoNestSong==null){
						//Hvis sangen fremdeles er null etter try-blokka, så settes det inn et tomt array i soundSummary i den aktuelle sangen
						jsonSong.put("soundSummary", jsonSoundSummaryArray);
					}
					//Hvis echoNestSong har fått en verdi etter try-blokka
					else{
						//Henter JSON version av echoNestSong og setter jsonSoundSummaryArray til å være lik det.
						jsonSoundSummaryArray = getSoundSummary(echoNestSong);

						//Plusser på de nye verdiene for feltene til den totale verdien for feltene i listen
						//Vil senere bli tatt gjennomsnittet av.
						danceabilityAverage += echoNestSong.getDanceability();
						durationAverage += echoNestSong.getDuration();
						energyAverage += echoNestSong.getEnergy();
						loudnessAverage += echoNestSong.getLoudness();
						modeAverage += echoNestSong.getMode();
						tempoAverage += echoNestSong.getTempo();
						keys[echoNestSong.getKey()]++;
						//Plusser på average, som er det tallet alle verdier over skal deles på etterhvert.
						//Average inkrementerer kun x-ganger for de sangene som har lyd-teknisk data.
						average ++;
						jsonSong.put("soundSummary", jsonSoundSummaryArray);
					}
					//Hent BOW av sangen fra MM-apiet
					JSONArray bow = mmBow.getBOW(artistString, titleString);
					//Put BOW resultatene inn i JsonSong som er den akutelle sangen det itereres gjennom
					jsonSong.put("bow", bow);

					//Hvis BOW != 0 så itereres det gjennom ord og verdi i BOW, som legges til i lyricSummaryMap, som er et hashmap som holder på alle ord og verdier for listen
					if(bow.size()!=0){
						for(int iter = 0 ; iter < bow.size() ; iter++ ){

							JSONArray temp = (JSONArray) bow.get(iter);
							String ord = (String) temp.get(0);
							long verdi = (Long) temp.get(1);

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

					//Hvis ikke sangen finnes, lagre den i mongodb Sang collection
					if(!mongodb.findSong(artistString, titleString)){
						mongodb.insertSong(jsonSong.toJSONString());
					}




					//Oppretter sangen som et jsonListItem object
					jsonListItem.put("title", titleString);
					jsonListItem.put("artist", artistString);
					jsonListItem.put("position", i);
					//i representerer posisjonen i listen for den aktuelle sangen
					i++;
					//Legger til JsonListItemet (Sangen) til listen, jsonListArray
					jsonListArray.add(jsonListItem);




				}

				//Itererer gjennom hashmappet som holder på alle ordene og antallganger det er representert og legger de til i lyricSummaryArrayet
				for (Map.Entry<String, Long> entry : lyricSummaryMap.entrySet()) {
					String ord = entry.getKey();
					Long value = entry.getValue();
					JSONArray tempArray = new JSONArray();

					tempArray.add(ord);
					tempArray.add(value);
					lyricSummaryArray.add(tempArray);

				}
				//Oppretter soundsammendrag arrayet
				JSONArray jsonSoundSummaryArray = new JSONArray();
				//Oppretter nødvendige objects til å holde gjennomsnittsverdiene for feltene
				JSONObject energy = new JSONObject();
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

				//Itererer gjennom alle keys i listen og legger det til i et jsonarray
				for(int iter = 0; iter < keys.length; iter++){
					int value = keys[iter];
					JSONObject tempKey = new JSONObject();
					tempKey.put(iter, value);
					jsonKeyArray.add(tempKey);
				}

				JSONObject jsonKey = new JSONObject();
				//Legger til Keys for listen
				jsonKey.put("key", (jsonKeyArray));
				//Legger til Jsonobjects som omhandler gjennomsnitt av alle feltene
				jsonSoundSummaryArray.add(danceability);
				jsonSoundSummaryArray.add(duration);
				jsonSoundSummaryArray.add(energy);
				jsonSoundSummaryArray.add(loudness);
				jsonSoundSummaryArray.add(mode);
				jsonSoundSummaryArray.add(tempo);
				jsonSoundSummaryArray.add(jsonKey);



				//Legger til soundSummary, lyricSummary og listen over sanger til listen
				jsonChart.put("soundSummary",jsonSoundSummaryArray );
				jsonChart.put("lyricSummary", lyricSummaryArray);
				jsonChart.put("list", jsonListArray);
				//Lagrer listen i mongodb
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
	public JSONArray getSoundSummary(Song echoNestSong) throws EchoNestException{
		JSONArray jsonSoundSummaryArray = new JSONArray();
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

		jsonSoundSummaryArray.add(danceability);
		jsonSoundSummaryArray.add(duration);
		jsonSoundSummaryArray.add(energy);
		jsonSoundSummaryArray.add(key);
		jsonSoundSummaryArray.add(loudness);
		jsonSoundSummaryArray.add(mode);
		jsonSoundSummaryArray.add(tempo);
		jsonSoundSummaryArray.add(timesignature);
		return jsonSoundSummaryArray;
	}

}
