package com.jplay;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

import com.google.gson.annotations.SerializedName;

// this can be an episode in a show or a movie
public class Playable implements Serializable {
	@SerializedName("Title")
	public String title;
	public String path;

    public String imdbID;

    @SerializedName("Year")
    public String year;

    @SerializedName("Rated")
    public String rated;

    @SerializedName("Released")
    public String released;

    @SerializedName("Runtime")
    public String runtime;

    @SerializedName("Genre")
    public String genre;

    @SerializedName("Director")
    public String director;

    @SerializedName("Writer")
    public String writer;

    @SerializedName("Actors")
    public String actors;

    @SerializedName("Plot")
    public String plot;

    @SerializedName("Language")
    public String language;

    @SerializedName("Country")
    public String country;

    @SerializedName("Awards")
    public String awards;

    @SerializedName("Poster")
    public String poster;

    @SerializedName("Metascore")
    public String metascore;

    @SerializedName("imdbRating")
    public String imdbrating;

    @SerializedName("imdbVotes")
    public String imdbvotes;

    @SerializedName("Type")
    public String type;

    @SerializedName("totalSeasons")
    public String totalseasons;


	public double length;
	public double lastPos;
	// for a movie here is 1 1
	public int season;
	public int episode;

	public MPV mpv = null;

	public Playable(){
		this.mpv = new MPV();
	}

	public void play(){
		try{
			var process = mpv.startMPV(this.path, this.lastPos);
			this.lastPos =  mpv.waitForMPVExit(process);
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	public String toString(){
		return "{\ntitle: "+title+"\npath: "+path+"\nseason: "+season+"\nepisode: "+episode+"\nlastpos: "+lastPos+"\n}";
	}
}
