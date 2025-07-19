package com.jplay.loaders;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.jplay.omdb.OMDB;
import com.jplay.omdb.SeriesInfo;
import com.jplay.omdb.OMDB.InvalidApiKeyException;
import com.jplay.*;

import java.io.File;
import java.io.FileInputStream;

public class SQLitePlayableLoader implements PlayableLoader {
	private static final String DB_PATH = System.getProperty("user.home") + "/.config/jplay/jplay.db";
	private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
	private OMDB omdb = null;

	public SQLitePlayableLoader() {
		try {
			// Ensure parent directories exist
			File dbFile = new File(DB_PATH);
			File parentDir = dbFile.getParentFile();
			if (!parentDir.exists()) {
				boolean created = parentDir.mkdirs();  // creates directories if not existing
				if (created) {
					System.out.println("Created directories: " + parentDir.getAbsolutePath());
				} else {
					System.out.println("Failed to create directories: " + parentDir.getAbsolutePath());
				}
			}
		}
		catch(Exception e){}

		try{

			//retrive apikey
			Properties appProps = new Properties();
			appProps.load(new FileInputStream("/home/spy/.config/jplay/config.properties"));
			String apikey = appProps.getProperty("apikey");
			if (apikey != null) {
				omdb = new OMDB(apikey);
			}


		}catch(Exception e){
			e.printStackTrace();
		}

		createTableIfNotExists();
	}

	private void createTableIfNotExists() {
		String sql = """
			CREATE TABLE IF NOT EXISTS playables (
												  title TEXT,
												  path TEXT UNIQUE,
												  length REAL,
												  lastPos REAL,
												  season INTEGER,
												  episode INTEGER,
												  imdbID TEXT,
												  year TEXT,
												  rated TEXT,
												  released TEXT,
												  runtime TEXT,
												  genre TEXT,
												  director TEXT,
												  writer TEXT,
												  actors TEXT,
												  plot TEXT,
												  language TEXT,
												  country TEXT,
												  awards TEXT,
												  poster TEXT,
												  metascore TEXT,
												  imdbRating TEXT,
												  imdbVotes TEXT,
												  type TEXT,
												  totalSeasons TEXT
												  );
		""";

			try (Connection conn = DriverManager.getConnection(DB_URL);
				 Statement stmt = conn.createStatement()) {
				stmt.execute(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}
	
	
	@Override
	public void registerPlayable(Playable playable) {
		try (Connection conn = DriverManager.getConnection(DB_URL)) {
			if(omdb != null) playable = fillMissingMetadata(conn, playable); // fetch info if needed

			String sql = """
				INSERT INTO playables (
									   title, path, length, season, episode,
									   imdbID, year, rated, released, runtime, genre, director,
									   writer, actors, plot, language, country, awards, poster,
									   metascore, imdbRating, imdbVotes, type, totalSeasons, lastPos
									   )
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				ON CONFLICT(path) DO UPDATE SET
                title = excluded.title,
                length = excluded.length,
                season = excluded.season,
                episode = excluded.episode,
                imdbID = excluded.imdbID,
                year = excluded.year,
                rated = excluded.rated,
                released = excluded.released,
                runtime = excluded.runtime,
                genre = excluded.genre,
                director = excluded.director,
                writer = excluded.writer,
                actors = excluded.actors,
                plot = excluded.plot,
                language = excluded.language,
                country = excluded.country,
                awards = excluded.awards,
                poster = excluded.poster,
                metascore = excluded.metascore,
                imdbRating = excluded.imdbRating,
                imdbVotes = excluded.imdbVotes,
                type = excluded.type,
                totalSeasons = excluded.totalSeasons%s
				"""
				.formatted(playable.lastPos == -1.0 ? "" : ",\n lastPos = excluded.lastPos");


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

					pstmt.executeUpdate();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private Playable fillMissingMetadata(Connection conn, Playable playable) {
		if (playable.imdbID != null && !playable.imdbID.isBlank()) return playable;

		String sql = "SELECT * FROM playables WHERE title = ? AND season = ? AND episode = ? LIMIT 1";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, playable.title);
			pstmt.setInt(2, playable.season);
			pstmt.setInt(3, playable.episode);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next() && rs.getString("imdbID") != null) {
				
				playable.imdbID = rs.getString("imdbID");
				playable.year = rs.getString("year");
				playable.rated = rs.getString("rated");
				playable.released = rs.getString("released");
				playable.runtime = rs.getString("runtime");
				playable.genre = rs.getString("genre");
				playable.director = rs.getString("director");
				playable.writer = rs.getString("writer");
				playable.actors = rs.getString("actors");
				playable.plot = rs.getString("plot");
				playable.language = rs.getString("language");
				playable.country = rs.getString("country");
				playable.awards = rs.getString("awards");
				playable.poster = rs.getString("poster");
				playable.metascore = rs.getString("metascore");
				playable.imdbrating = rs.getString("imdbRating");
				playable.imdbvotes = rs.getString("imdbVotes");
				playable.type = rs.getString("type");
				playable.totalseasons = rs.getString("totalSeasons");
			} else {
				try{
					omdb.fillInfo(playable);
				}
				catch(InvalidApiKeyException e){
					System.out.println(e.getMessage());
				}


				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return playable;
	}


	@Override
	public void removePlayable(Playable playable) {
		String sql = "DELETE FROM playables WHERE title = ? AND season = ? AND episode = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, playable.title);
			pstmt.setInt(2, playable.season);
			pstmt.setInt(3, playable.episode);
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Playable getPlayable(String search, int season, int episode) {
		// Case: latest
		if (season == -1 && episode == -1) {
			return this.getLatestPlayable(search);
		}

		String sql;
		if (season == -1) {
			// Episode specified, any season
			sql = """
				SELECT *
				FROM playables
				WHERE title = ? AND episode = ?
				ORDER BY season ASC
				LIMIT 1
				""";
				} else if (episode == -1) {
			// Season specified, any episode
			sql = """
				SELECT *
				FROM playables
				WHERE title = ? AND season = ?
				ORDER BY episode ASC
				LIMIT 1
				""";
				} else {
			// Both specified
			sql = """
				SELECT *
				FROM playables
				WHERE title = ? AND season = ? AND episode = ?
				""";
				}

		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, search);
			if (season == -1) {
				pstmt.setInt(2, episode);
			} else if (episode == -1) {
				pstmt.setInt(2, season);
			} else {
				pstmt.setInt(2, season);
				pstmt.setInt(3, episode);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Playable playable = map(rs);
					return playable;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}


	// Example getLatestPlayable implementation:
	// Returns last watched episode or next episode if last is almost done (lastPos near length)
	@Override
	public Playable getLatestPlayable(String title) {
		String sql = """
			SELECT *
			FROM playables
			WHERE title = ?
			ORDER BY season, episode
			""";

			try (Connection conn = DriverManager.getConnection(DB_URL);
				 PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setString(1, title);

				try (ResultSet rs = pstmt.executeQuery()) {
					while (rs.next()) {
						Playable playable = map(rs);

						// Check if lastPos close to length (e.g. > 90% watched)
						if (playable.length > 0 && playable.lastPos / playable.length > 0.9) {
							continue;
						}
						else{
							return playable;
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		return null;
	}

	private Playable map(ResultSet rs) {
		Playable p = new Playable();
		try{
			p.title = rs.getString("title");
			p.path = rs.getString("path");
			p.length = rs.getDouble("length");
			p.lastPos = rs.getDouble("lastPos");
			p.season = rs.getInt("season");
			p.episode = rs.getInt("episode");
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			p.imdbID = rs.getString("imdbID");
			p.year = rs.getString("year");
			p.rated = rs.getString("rated");
			p.released = rs.getString("released");
			p.runtime = rs.getString("runtime");
			p.genre = rs.getString("genre");
			p.director = rs.getString("director");
			p.writer = rs.getString("writer");
			p.actors = rs.getString("actors");
			p.plot = rs.getString("plot");
			p.language = rs.getString("language");
			p.country = rs.getString("country");
			p.awards = rs.getString("awards");
			p.poster = rs.getString("poster");
			p.metascore = rs.getString("metascore");
			p.imdbrating = rs.getString("imdbRating");
			p.imdbvotes = rs.getString("imdbVotes");
			p.type = rs.getString("type");
			p.totalseasons = rs.getString("totalSeasons");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return p;
	}

	@Override
	public List<Playable> getAllEpisodes(String title) {
		List<Playable> list = new ArrayList<>();
		String sql = "SELECT * FROM playables WHERE title = ? ORDER BY season, episode";

		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, title);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				Playable p = map(rs);
				list.add(p);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public List<String> getAllTitles() {
		List<String> titles = new ArrayList<>();
		String sql = "SELECT DISTINCT title FROM playables ORDER BY title";

		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				titles.add(rs.getString("title"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return titles;
	}
}
