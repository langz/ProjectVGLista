import com.echonest.api.v4.Artist;
import com.echonest.api.v4.ArtistParams;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Params;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.SongParams;

import java.io.IOException;
import java.util.List;

public class SearchSongData {
	public static SearchSongData searchsong;
	private EchoNestAPI en;

	public SearchSongData() throws EchoNestException {
		en = new EchoNestAPI("9VREGE9HWOOMXPTBA");
		en.setTraceSends(true);
		en.setTraceRecvs(false);
	}

	public void dumpSong(Song song) throws EchoNestException {
		//        System.out.printf("%s\n", song.getTitle());
		//        System.out.printf("   artist: %s\n", song.getArtistName());
		//        System.out.printf("   dur   : %.3f\n", song.getDuration());
		//        System.out.printf("   BPM   : %.3f\n", song.getTempo());
		//        System.out.printf("   Mode  : %d\n", song.getMode());
		//        System.out.printf("   S hot : %.3f\n", song.getSongHotttnesss());
		//        System.out.printf("   A hot : %.3f\n", song.getArtistHotttnesss());
		//        System.out.printf("   A fam : %.3f\n", song.getArtistFamiliarity());
		//        System.out.printf("   A loc : %s\n", song.getArtistLocation());
		//        System.out.println(song.getSongHotttnesss());
		//        System.out.println("ID: " +  song.getID());
	}

	public Song searchSongsByArtistAndTitle(String artist, String title, int results)
			throws EchoNestException, InterruptedException {
		Thread.sleep(1000);
		Song songreturn = null;
		SongParams p = new SongParams();
		p.setArtist(artist);
		p.setTitle(title);
		p.includeAudioSummary();
		p.includeArtistHotttnesss();
		p.includeSongHotttnesss();
		p.includeArtistFamiliarity();
		p.includeArtistLocation();
		p.sortBy("song_hotttnesss", false);

		try {
			List<Song> songs = en.searchSongs(p);
			if(songs.size()!=0){
				songreturn = songs.get(0);
			}
		} catch (EchoNestException e) {
			System.out.println("Finner ikke i echonest søk");
		}

		return songreturn;
	}

	public void searchSongsByTitle(String title, int results)
			throws EchoNestException {
		Params p = new Params();
		p.add("title", title);
		p.add("results", results);
		List<Song> songs = en.searchSongs(p);
		for (Song song : songs) {
			dumpSong(song);
			System.out.println();
		}
	}
	public void searchSongsByArtist(String artist, int results)
			throws EchoNestException {
		SongParams p = new SongParams();
		p.setArtist(artist);
		p.includeAudioSummary();
		p.includeArtistHotttnesss();
		p.includeSongHotttnesss();
		p.includeArtistFamiliarity();
		p.includeArtistLocation();
		p.sortBy("song_hotttnesss", false);


		List<Song> songs = en.searchSongs(p);
		for (Song song : songs) {
			dumpSong(song);
			System.out.println();
		}
	}

	public void stats() {
		en.showStats();
	}
	public static SearchSongData getInstance(){
		if (searchsong == null){
			try {
				searchsong = new SearchSongData();
			} catch (EchoNestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return searchsong;
		}
		else return searchsong;
	}
}