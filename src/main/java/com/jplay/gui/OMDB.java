package com.jplay.gui;

import java.net.*;
import java.io.*;
import com.google.gson.*;
import java.util.Map;

public class OMDB {

	String endpoint = "https://www.omdbapi.com";
	String key = "e5f1fd02";

	private String getEndPoint(String search){
		return endpoint+"/?"+search+"&apikey="+this.key;
	}

	private StringBuilder request(String endpoint) throws IOException{
		URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

		return response;
	}


	public String getIMDB(String title) throws IOException{
		String json = request(this.getEndPoint("s="+title.replace(" ","%20"))).toString();
		JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray searchResults = root.getAsJsonArray("Search");

        if (searchResults != null && searchResults.size() > 0) {
            JsonObject firstResult = searchResults.get(0).getAsJsonObject();
            String imdbID = firstResult.get("imdbID").getAsString();
            return imdbID;
        } else {
            System.out.println("No results found.");
        }
		return null;
	}


	public SeriesInfo getInfo(String title) throws IOException{
		StringBuilder response = request(this.getEndPoint("i="+getIMDB(title)));
		Gson gson = new Gson();
		System.out.println(response);
		SeriesInfo info = gson.fromJson(response.toString(), SeriesInfo.class);
		return info;
	}

}
