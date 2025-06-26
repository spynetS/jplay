package com.player;

import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.player.FileLoader;
import com.player.MPV;
import com.player.Playable;
import com.player.PlayableLoader;
import com.player.SQLitePlayableLoader;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "player", mixinStandardHelpOptions = true, version = "player 1.0",
         description = "Movie/show player and manager", subcommands = {
    Main.ListCommand.class
})
public class Main implements Runnable {
    static String[] extensions = {".mp4", ".mkv", ".avi", ".mov", ".flv", ".wmv", ".webm"};

    @Parameters(index = "0", description = "The path to scan/play", arity = "0..1")
    File inputPath;

        @Option(names = {"--season", "-s"}, description = "Season number")
    private Integer season = 1;


    @Option(names = {"--episode", "-e"}, description = "Episode number")
    private Integer episode = -1;

    @Option(names = {"--title", "-t"}, description = "Title to play")
    private String title;

    public static void main(String[] args) {
        CommandLine.run(new Main(), args);
    }


    @Override
    public void run() {
        SQLitePlayableLoader loader = new SQLitePlayableLoader();
        if (inputPath != null && inputPath.exists()) {
            if(inputPath.isDirectory()){
                // registar all episodes in the foldern
                List<Playable> players = getPlayablesInFolder(inputPath.getAbsolutePath());
                for(Playable player : players){
                    loader.registerPlayable(player);
                }
                // play the lastest of the title
                Playable p = loader.getPlayable(title != null ? title : players.getFirst().title,season,episode);
                p.play();
                // register again to save the new lastPos
                loader.registerPlayable(p);
            }
            else if(inputPath.isFile()){
                try {
                    // load the file
                    Playable p = loadPlayable(inputPath);
                    loader.registerPlayable(p);
                    // get it from the database (this is if it allready exists so we get the right lastpos)
                    p = loader.getPlayable(p.title,season,episode);
                    p.play();
                    // save the new lastpos
                    loader.registerPlayable(p);

                } catch (Exception e) {
                    // TODO: handle exception
                }

            }

        }
        else if (inputPath != null) {

            Playable p = loader.getPlayable(inputPath.getName(),season,episode);
            p.play();
            loader.registerPlayable(p);

        }
        else if (title != null){
            Playable p = loader.getPlayable(title,season,episode);
            p.play();
            loader.registerPlayable(p);
        }

    }
    public static List<Playable> getPlayablesInFolder(String folderPath) {
        List<Playable> playables = new ArrayList<>();

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Invalid folder path: " + folderPath);
            return playables;
        }

        // Supported video extensions (add more if you want)
        // Recursive helper
        scanFolder(folder, playables);

        return playables;
    }

    private static void scanFolder(File folder, List<Playable> playables) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                scanFolder(file, playables);  // Recurse into subfolders
            } else {
                try{
                    playables.add(loadPlayable(file));
                }catch(IOException e){}
            }
        }
    }

    private static Playable loadPlayable(File file) throws IOException{
        String name = file.getName().toLowerCase();

        for (String ext : extensions) {
            if (name.endsWith(ext)) {
                Guessit g = new Guessit(file.getName(),file.getAbsolutePath());
                Playable p = new Playable();
                p.title = g.getTitle();
                p.path = file.getAbsolutePath();
                p.length = g.getLength();     // You can set length later if you want
                p.lastPos = -1;
                p.season = g.getSeason();     // Default for movies, change if you want
                p.episode = g.getEpisode();
                return p;
            }
        }
        throw new IOException();
    }

    @Command(name = "list", description = "List all registered playables")
    static class ListCommand implements Runnable {


        @Option(names = {"--title"}, description = "Only list episodes for this title")
        String title;

        @Override
        public void run() {
            PlayableLoader loader = new SQLitePlayableLoader();

            if (title != null) {
                List<Playable> episodes = loader.getAllEpisodes(title);
                if (episodes.isEmpty()) {
                    System.out.println("No episodes found for: " + title);
                } else {
                    episodes.forEach(p -> System.out.printf(
                        "%s - S%02dE%02d [%s] lastPos=%.2f\n",
                        p.title, p.season, p.episode, p.path, p.lastPos));
                }
            } else {
                List<String> allTitles = loader.getAllTitles();
                if (allTitles.isEmpty()) {
                    System.out.println("No titles found.");
                } else {
                    allTitles.forEach(System.out::println);
                }
            }
        }
    }

}
