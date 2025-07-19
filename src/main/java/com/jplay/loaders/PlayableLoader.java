package com.jplay.loaders;
import java.util.List;

import com.jplay.Playable;

public interface PlayableLoader {


	// register movie or show in database
	public void registerPlayable(Playable playable);
	// remove movie or show from database
	public void removePlayable  (Playable playable);

	// return playable
	public Playable getPlayable(String search, int season, int episode);
	// retuns last watched or next of last is almost done
	public Playable getLatestPlayable(String search);

	public List<Playable> getAllEpisodes(String title);
	public List<String> getAllTitles();
	public List<Playable> getAllEntries();
}
