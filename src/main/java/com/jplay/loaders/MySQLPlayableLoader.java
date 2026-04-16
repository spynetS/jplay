package com.jplay.loaders;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jplay.omdb.OMDB;
import com.jplay.omdb.OMDB.InvalidApiKeyException;
import com.jplay.*;

import java.io.FileInputStream;

public class MySQLPlayableLoader implements PlayableLoader {

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        try {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream(System.getProperty("user.home") + "/.config/jplay/config.properties"));
            DB_URL      = appProps.getProperty("mysql.url",      "jdbc:mysql://localhost:3306/jplay");
            DB_USER     = appProps.getProperty("mysql.user",     "root");
            DB_PASSWORD = appProps.getProperty("mysql.password", "");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load MySQL config", e);
        }
    }

    private OMDB omdb = null;

    public MySQLPlayableLoader() {
        try {
            Properties appProps = new Properties();
            appProps.load(new FileInputStream(System.getProperty("user.home") + "/.config/jplay/config.properties"));
            String apikey = appProps.getProperty("apikey");
            if (apikey != null) {
                omdb = new OMDB(apikey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        createTableIfNotExists();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void createTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS playables (
                    title        TEXT,
                    path         VARCHAR(1024) NOT NULL,
                    length       DOUBLE,
                    lastPos      DOUBLE,
                    season       INT,
                    episode      INT,
                    imdbID       VARCHAR(20),
                    year         VARCHAR(20),
                    rated        VARCHAR(20),
                    released     VARCHAR(50),
                    runtime      VARCHAR(50),
                    genre        TEXT,
                    director     TEXT,
                    writer       TEXT,
                    actors       TEXT,
                    plot         TEXT,
                    language     TEXT,
                    country      TEXT,
                    awards       TEXT,
                    poster       TEXT,
                    metascore    VARCHAR(20),
                    imdbRating   VARCHAR(20),
                    imdbVotes    VARCHAR(20),
                    type         VARCHAR(50),
                    totalSeasons VARCHAR(20),
                    pathExists   INT,
                    PRIMARY KEY (path(768))
                ) CHARACTER SET utf8mb4;
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerPlayable(Playable playable) {
        try (Connection conn = getConnection()) {
            if (omdb != null) playable = fillMissingMetadata(conn, playable);

            // MySQL uses INSERT ... ON DUPLICATE KEY UPDATE instead of SQLite's ON CONFLICT
            String lastPosClause = playable.lastPos == -1.0 ? "" : ",\n lastPos = VALUES(lastPos)";

            String sql = """
                    INSERT INTO playables (
                        title, path, length, season, episode,
                        imdbID, year, rated, released, runtime, genre, director,
                        writer, actors, plot, language, country, awards, poster,
                        metascore, imdbRating, imdbVotes, type, totalSeasons, lastPos, pathExists
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        title        = VALUES(title),
                        length       = VALUES(length),
                        season       = VALUES(season),
                        episode      = VALUES(episode),
                        imdbID       = VALUES(imdbID),
                        year         = VALUES(year),
                        rated        = VALUES(rated),
                        released     = VALUES(released),
                        runtime      = VALUES(runtime),
                        genre        = VALUES(genre),
                        director     = VALUES(director),
                        writer       = VALUES(writer),
                        actors       = VALUES(actors),
                        plot         = VALUES(plot),
                        language     = VALUES(language),
                        country      = VALUES(country),
                        awards       = VALUES(awards),
                        poster       = VALUES(poster),
                        metascore    = VALUES(metascore),
                        imdbRating   = VALUES(imdbRating),
                        imdbVotes    = VALUES(imdbVotes),
                        type         = VALUES(type),
                        totalSeasons = VALUES(totalSeasons)%s,
                        pathExists   = VALUES(pathExists)
                    """.formatted(lastPosClause);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int i = 1;
                pstmt.setString(i++, playable.title);
                pstmt.setString(i++, playable.path);
                pstmt.setDouble(i++, playable.length);
                pstmt.setInt(i++, playable.season);
                pstmt.setInt(i++, playable.episode);
                pstmt.setString(i++, playable.imdbID);
                pstmt.setString(i++, playable.year);
                pstmt.setString(i++, playable.rated);
                pstmt.setString(i++, playable.released);
                pstmt.setString(i++, playable.runtime);
                pstmt.setString(i++, playable.genre);
                pstmt.setString(i++, playable.director);
                pstmt.setString(i++, playable.writer);
                pstmt.setString(i++, playable.actors);
                pstmt.setString(i++, playable.plot);
                pstmt.setString(i++, playable.language);
                pstmt.setString(i++, playable.country);
                pstmt.setString(i++, playable.awards);
                pstmt.setString(i++, playable.poster);
                pstmt.setString(i++, playable.metascore);
                pstmt.setString(i++, playable.imdbrating);
                pstmt.setString(i++, playable.imdbvotes);
                pstmt.setString(i++, playable.type);
                pstmt.setString(i++, playable.totalseasons);
                pstmt.setDouble(i++, playable.lastPos);
                pstmt.setInt(i++, playable.pathExists);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Playable fillMissingMetadata(Connection conn, Playable playable) {
        if (playable.imdbID != null && !playable.imdbID.isBlank()) return playable;

        String sql = "SELECT * FROM playables WHERE title LIKE ? AND season = ? AND episode = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + playable.title + "%");
            pstmt.setInt(2, playable.season);
            pstmt.setInt(3, playable.episode);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getString("imdbID") != null) {
                playable.imdbID       = rs.getString("imdbID");
                playable.year         = rs.getString("year");
                playable.rated        = rs.getString("rated");
                playable.released     = rs.getString("released");
                playable.runtime      = rs.getString("runtime");
                playable.genre        = rs.getString("genre");
                playable.director     = rs.getString("director");
                playable.writer       = rs.getString("writer");
                playable.actors       = rs.getString("actors");
                playable.plot         = rs.getString("plot");
                playable.language     = rs.getString("language");
                playable.country      = rs.getString("country");
                playable.awards       = rs.getString("awards");
                playable.poster       = rs.getString("poster");
                playable.metascore    = rs.getString("metascore");
                playable.imdbrating   = rs.getString("imdbRating");
                playable.imdbvotes    = rs.getString("imdbVotes");
                playable.type         = rs.getString("type");
                playable.totalseasons = rs.getString("totalSeasons");
            } else {
                try {
                    omdb.fillInfo(playable);
                } catch (InvalidApiKeyException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return playable;
    }

    @Override
    public void removePlayable(Playable playable) {
        String sql = "DELETE FROM playables WHERE title LIKE ? AND season = ? AND episode = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + playable.title + "%");
            pstmt.setInt(2, playable.season);
            pstmt.setInt(3, playable.episode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Playable getPlayable(int index, int season, int episode) {
        String title = this.getAllTitles(true).get(index);
        return this.getPlayable(title, season, episode);
    }

    @Override
    public Playable getPlayable(String search, int season, int episode) {
        if (season == -1 && episode == -1) {
            return this.getLatestPlayable(search);
        }

        String sql;
        if (season == -1) {
            sql = "SELECT * FROM playables WHERE title LIKE ? AND episode = ? ORDER BY season ASC LIMIT 1";
        } else if (episode == -1) {
            sql = "SELECT * FROM playables WHERE title LIKE ? AND season = ? ORDER BY episode ASC LIMIT 1";
        } else {
            sql = "SELECT * FROM playables WHERE title LIKE ? AND season = ? AND episode = ? LIMIT 1";
        }

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + search + "%");
            if (season == -1) {
                pstmt.setInt(2, episode);
            } else if (episode == -1) {
                pstmt.setInt(2, season);
            } else {
                pstmt.setInt(2, season);
                pstmt.setInt(3, episode);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Playable getLatestPlayable(String title) {
        String sql = "SELECT * FROM playables WHERE title LIKE ? ORDER BY season, episode";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + title + "%");

            List<Playable> episodes = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) episodes.add(map(rs));
            }

            if (episodes.isEmpty()) return null;

            int latestWatchedIndex = -1;
            for (int i = 0; i < episodes.size(); i++) {
                Playable ep = episodes.get(i);
                if (ep.lastPos > ep.length * 0.1f) latestWatchedIndex = i;
            }

            if (latestWatchedIndex == -1) return episodes.get(0);

            Playable latestWatched = episodes.get(latestWatchedIndex);
            double progress = (double) latestWatched.lastPos / latestWatched.length;

            if (progress > 0.9) {
                return (latestWatchedIndex + 1 < episodes.size())
                        ? episodes.get(latestWatchedIndex + 1)
                        : null;
            } else {
                return latestWatched;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Playable map(ResultSet rs) {
        Playable p = new Playable();
        try {
            p.title      = rs.getString("title");
            p.path       = rs.getString("path");
            p.length     = rs.getDouble("length");
            p.lastPos    = rs.getDouble("lastPos");
            p.season     = rs.getInt("season");
            p.episode    = rs.getInt("episode");
            p.pathExists = rs.getInt("pathExists");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            p.imdbID       = rs.getString("imdbID");
            p.year         = rs.getString("year");
            p.rated        = rs.getString("rated");
            p.released     = rs.getString("released");
            p.runtime      = rs.getString("runtime");
            p.genre        = rs.getString("genre");
            p.director     = rs.getString("director");
            p.writer       = rs.getString("writer");
            p.actors       = rs.getString("actors");
            p.plot         = rs.getString("plot");
            p.language     = rs.getString("language");
            p.country      = rs.getString("country");
            p.awards       = rs.getString("awards");
            p.poster       = rs.getString("poster");
            p.metascore    = rs.getString("metascore");
            p.imdbrating   = rs.getString("imdbRating");
            p.imdbvotes    = rs.getString("imdbVotes");
            p.type         = rs.getString("type");
            p.totalseasons = rs.getString("totalSeasons");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }

    public List<Playable> getAllEntries(boolean exists) {
        List<Playable> list = new ArrayList<>();
        String sql = exists
                ? "SELECT * FROM playables WHERE pathExists = 1 ORDER BY season, episode"
                : "SELECT * FROM playables ORDER BY season, episode";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Playable> getAllEpisodes(String title) {
        List<Playable> list = new ArrayList<>();
        String sql = "SELECT * FROM playables WHERE title LIKE ? ORDER BY season, episode";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + title + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<String> getAllTitles(boolean exists) {
        List<String> titles = new ArrayList<>();
        String sql = exists
                ? "SELECT DISTINCT title FROM playables WHERE pathExists = 1 ORDER BY title"
                : "SELECT DISTINCT title FROM playables ORDER BY title";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) titles.add(rs.getString("title"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return titles;
    }
}
