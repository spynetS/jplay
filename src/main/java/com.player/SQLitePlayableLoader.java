package com.player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class SQLitePlayableLoader implements PlayableLoader {
    private static final String DB_URL = "jdbc:sqlite:playables.db";

    public SQLitePlayableLoader() {
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
												  episode INTEGER
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
		System.out.println(playable);
		String sql = """
			INSERT INTO playables (title, path, length, lastPos, season, episode)
			VALUES (?, ?, ?, ?, ?, ?)
			ON CONFLICT(path) DO UPDATE SET
            title = excluded.title,
            length = excluded.length,
            season = excluded.season,
            episode = excluded.episode%s
			""".formatted(playable.lastPos == -1.0 ? "" : ",\n lastPos = excluded.lastPos");

			try (Connection conn = DriverManager.getConnection(DB_URL);
				 PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setString(1, playable.title);
				pstmt.setString(2, playable.path);
				pstmt.setDouble(3, playable.length);
				pstmt.setDouble(4, playable.lastPos); // still needed for binding
				pstmt.setInt(5, playable.season);
				pstmt.setInt(6, playable.episode);

				pstmt.executeUpdate();

			} catch (SQLException e) {
				e.printStackTrace();
			}
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
		if(season == -1 || episode == -1){
			return this.getLatestPlayable(search);
		}
        String sql = """
            SELECT title, path, length, lastPos, season, episode
            FROM playables
            WHERE title = ? AND season = ? AND episode = ?
			""";

			try (Connection conn = DriverManager.getConnection(DB_URL);
				 PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setString(1, search);
				pstmt.setInt(2, season);
				pstmt.setInt(3, episode);

				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						Playable playable = new Playable();
						playable.title = rs.getString("title");
						playable.path = rs.getString("path");
						playable.length = rs.getDouble("length");
						playable.lastPos = rs.getDouble("lastPos");
						playable.season = rs.getInt("season");
						playable.episode = rs.getInt("episode");
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
            SELECT title, path, length, lastPos, season, episode
            FROM playables
            WHERE title = ?
            ORDER BY season, episode
            LIMIT 1
			""";

			try (Connection conn = DriverManager.getConnection(DB_URL);
				 PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setString(1, title);
			
				try (ResultSet rs = pstmt.executeQuery()) {
					if (rs.next()) {
						Playable playable = new Playable();
						playable.title = rs.getString("title");
						playable.path = rs.getString("path");
						playable.length = rs.getDouble("length");
						playable.lastPos = rs.getDouble("lastPos");
						playable.season = rs.getInt("season");
						playable.episode = rs.getInt("episode");
						System.out.println(playable);

						// Check if lastPos close to length (e.g. > 90% watched)
						if (playable.length > 0 && playable.lastPos / playable.length > 0.9) {
							// Try to load next episode
							Playable nextEp = getPlayable(title, playable.season, playable.episode + 1);
							if (nextEp != null) return nextEp;
						}
						return playable;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

        return null;
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
				Playable p = new Playable();
				p.title = rs.getString("title");
				p.path = rs.getString("path");
				p.length = rs.getDouble("length");
				p.lastPos = rs.getDouble("lastPos");
				p.season = rs.getInt("season");
				p.episode = rs.getInt("episode");
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
