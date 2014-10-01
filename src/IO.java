import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.json.simple.JSONObject;


public class IO {
	public static IO io;

	public void saveList(JSONObject obj){
		try {
			new File("c:\\Master/Lists/" + obj.get("year")).mkdirs();
			File file = new File("c:\\Master/Lists/" + obj.get("year") + "/" + obj.get("week") + ".json");
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF8"));
			out.write(obj.toJSONString());
			out.flush();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.print("Ferdig med " + obj.get("year") + " - " + obj.get("week"));
	}

	public void saveSong(JSONObject obj){

		if(!checkFileExists(obj.get("artist").toString(), obj.get("title").toString())){


			try {
				new File("c:\\Master/Songs/").mkdirs();
				File file = new File("c:\\Master/Songs/"  + obj.get("artist") +"-" +obj.get("title") + ".json");
				Writer out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file), "UTF8"));
				out.write(obj.toJSONString());
				out.flush();
				out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.print("Ferdig med " + obj.get("artist") + " - " + obj.get("title"));
		}
		else{
			System.out.println("fantes fra før" + "c:\\Master/Songs/"  + obj.get("artist") +"-" +obj.get("title") + ".json");

		}
	}
	public boolean checkFileExists(String artist, String title){
		boolean boolsk = false;
		File checkFile = new File("c:\\Master/Songs/"  + artist +"-" +title + ".json");
		if(checkFile.exists()){
			boolsk = true;
		}
		else{
			boolsk = false;
		}
		return boolsk;
	}
	public static IO getInstance(){
		if (io == null){
			io = new IO();
			return io;
		}
		else return io;
	}
}
