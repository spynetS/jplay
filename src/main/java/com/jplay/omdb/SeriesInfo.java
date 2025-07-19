package com.jplay.omdb;

import java.util.List;

public class SeriesInfo {
    public String Title;
    public String Year;
    public String Rated;
    public String Released;
    public String Runtime;
    public String Genre;
    public String Director;
    public String Writer;
    public String Actors;
    public String Plot;
    public String Language;
    public String Country;
    public String Awards;
    public String Poster;
    public List<Rating> Ratings;
    public String Metascore;
    public String imdbRating;
    public String imdbVotes;
    public String imdbID;
    public String Type;
    public String totalSeasons;
    public String Response;

    // Optional: toString() for quick debug print
    @Override
    public String toString() {
        return "SeriesInfo{" +
                "Title='" + Title + '\'' +
                ", Year='" + Year + '\'' +
                ", Rated='" + Rated + '\'' +
                ", Released='" + Released + '\'' +
                ", Runtime='" + Runtime + '\'' +
                ", Genre='" + Genre + '\'' +
                ", Director='" + Director + '\'' +
                ", Writer='" + Writer + '\'' +
                ", Actors='" + Actors + '\'' +
                ", Plot='" + Plot + '\'' +
                ", Language='" + Language + '\'' +
                ", Country='" + Country + '\'' +
                ", Awards='" + Awards + '\'' +
                ", Poster='" + Poster + '\'' +
                ", Ratings=" + Ratings +
                ", Metascore='" + Metascore + '\'' +
                ", imdbRating='" + imdbRating + '\'' +
                ", imdbVotes='" + imdbVotes + '\'' +
                ", imdbID='" + imdbID + '\'' +
                ", Type='" + Type + '\'' +
                ", totalSeasons='" + totalSeasons + '\'' +
                ", Response='" + Response + '\'' +
                '}';
    }

    // Nested Rating class
    public static class Rating {
        public String Source;
        public String Value;

        @Override
        public String toString() {
            return "Rating{" +
                    "Source='" + Source + '\'' +
                    ", Value='" + Value + '\'' +
                    '}';
        }
    }
}
