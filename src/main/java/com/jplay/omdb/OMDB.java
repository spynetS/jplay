package com.jplay.omdb;

import java.net.*;
import java.io.*;
import com.google.gson.*;
import com.jplay.Playable;

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


	public Playable getInfo(String title) throws IOException{
		StringBuilder response = request(this.getEndPoint("i="+getIMDB(title)));
		Gson gson = new Gson();
		System.out.println(response);
		Playable info = gson.fromJson(response.toString(), Playable.class);
		return info;
	}
	public void fillInfo(Playable playable) throws IOException {
		String imdbQuery = "i=" + getIMDB(playable.title);
		StringBuilder response = request(this.getEndPoint(imdbQuery));
		Gson gson = new Gson();

		System.out.println(response);

		Playable info = gson.fromJson(response.toString(), Playable.class);

		// Populate the input playable with fetched metadata
		    playable.imdbID = info.imdbID;
			playable.year = info.year;
			playable.rated = info.rated;
			playable.released = info.released;
			playable.runtime = info.runtime;
			playable.genre = info.genre;
			playable.director = info.director;
			playable.writer = info.writer;
			playable.actors = info.actors;
			playable.plot = info.plot;
			playable.language = info.language;
			playable.country = info.country;
			playable.awards = info.awards;
			playable.poster = info.poster;
			playable.metascore = info.metascore;
			playable.imdbrating = info.imdbrating;
			playable.imdbvotes = info.imdbvotes;
			playable.type = info.type;
			playable.totalseasons = info.totalseasons;
	}


}
