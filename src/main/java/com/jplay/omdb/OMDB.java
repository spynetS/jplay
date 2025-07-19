package com.jplay.omdb;

import java.net.*;
import java.io.*;
import com.google.gson.*;
import com.jplay.Playable;

import java.util.Map;

public class OMDB {

	String endpoint = "https://www.omdbapi.com";
	String key = "";

	public OMDB(String apikey){
		this.key = apikey;
	}

	private String getEndPoint(String search){
		return endpoint+"/?"+search+"&apikey="+this.key;
	}

	public StringBuilder request(String endpoint) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
    conn.setRequestMethod("GET");
    conn.connect();

    int status = conn.getResponseCode();
    InputStream stream;

    // Choose stream based on success or failure
    if (status >= 200 && status < 300) {
        stream = conn.getInputStream();
    } else {
        stream = conn.getErrorStream();
    }

    // Read the response
    StringBuilder response = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
    }

    // Handle API-level error (e.g. invalid API key)
    if (status == 401 || response.toString().contains("Invalid API key")) {
        throw new InvalidApiKeyException("Invalid or missing API key");
    }

    // You can also throw for other known OMDb errors:
    if (response.toString().contains("\"Response\":\"False\"")) {
        // Optional: parse the exact error message
        String errorMsg = extractErrorMessage(response.toString());
        throw new IOException("OMDb API error: " + errorMsg);
    }

    return response;
}

// Optional helper to extract "Error" field from OMDb JSON
private String extractErrorMessage(String json) {
    int index = json.indexOf("\"Error\":\"");
    if (index != -1) {
        int start = index + 9;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
    return "Unknown error";
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

	public void fillInfo(Playable playable) throws Exception {
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

	public class InvalidApiKeyException extends IOException {
		public InvalidApiKeyException(String message) {
			super(message);
		}
	}
}
