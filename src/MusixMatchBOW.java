import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class MusixMatchBOW {



	public MusixMatchBOW(){

	}

	public JSONArray getBOW(String artist, String song) throws MalformedURLException, IOException{

		String artistString = artist;
		String songString = song;
		//Fjerner opphold, æ, ø, å og &
		artistString = artistString.replaceAll("\\s+","").replaceAll("œ","\u00e6").replaceAll("å","\u00e5").replaceAll("ø","\u00e8").replaceAll("&","%20");
		songString = songString.replaceAll("\\s+","").replaceAll("œ","\u00e6").replaceAll("å","\u00e5").replaceAll("ø","\u00e8").replaceAll("&","%20");
		//Setter strengene til LowerCase
		artistString = artistString.toLowerCase();
		songString = songString.toLowerCase();
		//Oppretter et JSONArray som skal inneholde alle ord og deres forekomster.
		JSONArray bow = new JSONArray();
		//URLen til APIet
		String urlString = "http://ec2-54-77-161-182.eu-west-1.compute.amazonaws.com/research/v1.0/bow.get?q=" + artistString + songString;

		InputStream input = null;
		//Prøver å koble til, hvis det ikke går og IOException blir kastet, så catches det.
		try {
			input = new URL(urlString).openStream();
		} catch (IOException e) {

		}
		//Hvis nettstedet ikke er null
		if(input!=null){

			Reader reader = new InputStreamReader(input, "UTF-8");

			JSONParser jsonParser = new JSONParser();
			try {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
				if(jsonObject.get("Error").equals("none")){
					//Setter bow til å være responsen fra APIet
					bow =(JSONArray) jsonObject.get("Bag_of_words");
				}
				else{
					//Bytter rekkefølge på song og artist-String
					String urlString2 = "http://ec2-54-77-161-182.eu-west-1.compute.amazonaws.com/research/v1.0/bow.get?q=" + songString + artistString;

					InputStream input2 = new URL(urlString2).openStream();

					Reader reader2 = new InputStreamReader(input2, "UTF-8");

					JSONParser jsonParser2 = new JSONParser();
					JSONObject jsonObject2 = (JSONObject) jsonParser2.parse(reader2);
					if(jsonObject2.get("Error").equals("none")){
						//Setter bow til å være responsen fra APIet
						bow =(JSONArray) jsonObject2.get("Bag_of_words");
					}
					else{
						//Setter bow til å være et tomt array
						bow = new JSONArray();
					}
					reader2.close();
					input2.close();
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reader.close();
			input.close();
		}
		//Returnerer ordene og deres forekomster i sangen.
		return bow;
	}

}
