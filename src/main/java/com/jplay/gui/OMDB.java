package com.jplay.gui;

import java.net.*;
import java.io.*;
import com.google.gson.Gson;
import java.util.Map;

public class OMDB {

	String endpoint = "https://www.omdbapi.com";
	String key = "e5f1fd02";

	private String getEndPoint(String search){
		return endpoint+"/?"+search+"&apikey="+this.key;
	}

	public SeriesInfo searchIMDB(String imdb) throws IOException{
		URL url = new URL(this.getEndPoint("i=tt0306414"));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
		Gson gson = new Gson();
		System.out.println(response);
		SeriesInfo info = gson.fromJson(response.toString(), SeriesInfo.class);
		return info;
	}

}
