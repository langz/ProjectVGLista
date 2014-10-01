import java.io.IOException;

import org.jmusixmatch.MusixMatchException;

import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;

public class Main {

	public static void main(String[] args) throws IOException, EchoNestException, MusixMatchException, InterruptedException {
		//REAL,
		for(int i=2014; i<2015; i++){
			int x = 53;
			if(i==2014){
				x=35;
			}
			else{

			}
			for(int v=1; v<=x ;v++){
				if((i==1975 && v==1) || (i==1985 && v==1) || (i==1986 && v==1) || (i==1987 && v==1)){

				}
				else{
					Thread.sleep(1000);
					String url = "http://lista.vg.no/liste/topp-20-single/1/dato/"+ i +"/uke/" + v;
					CrawlerVG crawler = CrawlerVG.getInstance();
					System.out.println("Prøver å gå til: " + url);
					crawler.getList(url);
				}
			}
		}



		//		
		//    	SearchSong sse = new SearchSong();
		//    	Song answer = sse.searchSongsByArtistAndTitle("The Black Eyed Peas", "Don't stop the Party", 5);
		//System.out.println(answer.getArtistName() + " - " + answer.getTitle() + " - " + answer.getSongHotttnesss());
		//Song answer2 = sse.searchSongsByArtistAndTitle("The Black Eyed Peas", "Don't stop the Party", 5);
		//System.out.println(answer2.getArtistName() + " - " + answer2.getTitle() + " - " + answer2.getSongHotttnesss());
		//    	SearchLyric sl = new SearchLyric();
		//    	String trackName = "Så kom våren til Tarina";
		//    	String artistName = "Bjørg og Per Gunnar";
		//    	sl.fuzzySearch(trackName, artistName);
	}
}
