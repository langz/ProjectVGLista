import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;

import com.google.gson.JsonSyntaxException;


public class SearchLyric {
	public static SearchLyric searchlyric;
	String apiKey = "0b67420005ecf2332ae8a5b142993fa5";
	MusixMatch musixMatch = new MusixMatch(apiKey);

	public SearchLyric(){

	}

	public Lyrics fuzzySearch(String trackName, String artistName) throws MusixMatchException{
		System.out.println("Søket er på : " + trackName + " " + artistName);
		Lyrics lyric=null;
		// Track Search [ Fuzzy ]
		Track track = null;
		try {
			track = musixMatch.getMatchingTrack(trackName, artistName);
			System.out.println(track.getTrack().getArtistName());
		} catch (MusixMatchException e) {
			System.out.println("Ingen treff på lyric, inne i SearchLyric.fuzzy");
		}

		if(track!=null){
			TrackData data = track.getTrack();
			lyric = lyricSearch(data);
		}
		else{

		}

		return lyric;

	}
	public Lyrics lyricSearch(TrackData data) throws MusixMatchException{
		int trackID = data.getTrackId();
		Lyrics lyrics = null;
		try{
			lyrics = musixMatch.getLyrics(trackID);
		}
		catch (JsonSyntaxException | IllegalStateException e){
			System.out.println("STEMMER, DENNA ER ILLEGAL!");
		}
		//		System.out.println("Lyrics ID       : "     + lyrics.getLyricsId());
		//		System.out.println("Lyrics Language : "     + lyrics.getLyricsLang());
		//		System.out.println("Lyrics Body     : "     + lyrics.getLyricsBody());
		//		System.out.println("Script-Tracking-URL : " + lyrics.getScriptTrackingURL());
		//		System.out.println("Pixel-Tracking-URL : "  + lyrics.getPixelTrackingURL());
		//		System.out.println("Lyrics Copyright : "    + lyrics.getLyricsCopyright());

		return lyrics;
	}
	public static SearchLyric getInstance(){
		if (searchlyric == null){
			searchlyric = new SearchLyric();
			return searchlyric;
		}
		else return searchlyric;
	}	
}
