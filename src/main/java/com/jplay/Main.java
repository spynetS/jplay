package com.jplay;
import com.jplay.gui.*;

import java.io.IOException;
import java.nio.file.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.jplay.MPV;
import com.jplay.Playable;
import com.jplay.loaders.PlayableLoader;
import com.jplay.loaders.SQLitePlayableLoader;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import com.jplay.MyVersionProvider;

@Command(name = "jplay", mixinStandardHelpOptions = true,versionProvider = MyVersionProvider.class,
         description = "Movie/show jplay and manager", subcommands = {
    Main.ListCommand.class,
    Main.GUICommand.class,
})
public class Main implements Runnable {
    static String[] extensions = {".mp4", ".mkv", ".avi", ".mov", ".flv", ".wmv", ".webm"};

    @Parameters(index = "0", description = "The path to scan/play", arity = "0..1")
    File inputPath;

    @Option(names = {"--home"}, description = "The default scan path")
    File defaultPath = new File(System.getProperty("user.home"), "Movies");

    @Option(names = {"--season", "-s"}, description = "Season number")
    private Integer season = -1;


    @Option(names = {"--episode", "-e"}, description = "Episode number")
    private Integer episode = -1;

    @Option(names = {"--title", "-t"}, description = "Title to play")
    private String title;

    public static void main(String[] args) {

        CommandLine.run(new Main(), args);
    }

    public void scanDefault(){
        SQLitePlayableLoader loader = new SQLitePlayableLoader();
        if(defaultPath != null && defaultPath.isDirectory()){
            List<Playable> players = getPlayablesInFolder(defaultPath.getAbsolutePath());
            for(Playable player : players){
                // log new found fotage
                // System.out.println("Found: "+player.title);
                loader.registerPlayable(player);
            }
        }
    }

    @Override
    public void run() {
        SQLitePlayableLoader loader = new SQLitePlayableLoader();
        scanDefault();
        if (inputPath != null && inputPath.exists()) {
            if(inputPath.isDirectory()){
                System.out.println("Playing folder");
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
                    System.out.println("Playing file");
                    // load the file
                    Playable p = loadPlayable(inputPath);
                    loader.registerPlayable(p);
                    // get it from the database (this is if it allready exists so we get the right lastpos)
                    p = loader.getPlayable(p.title,p.season,p.episode);
                    p.play();
                    // save the new lastpos
                    loader.registerPlayable(p);

                } catch (Exception e) {
                    // TODO: handle exception
                }

            }

        }
        else if (inputPath != null) {
            System.out.println("Playing title");
            Playable p = loader.getPlayable(inputPath.getName(),season,episode);
            p.play();
            loader.registerPlayable(p);

        }
        else if (title != null){
            System.out.println("Playing title (--title)");
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

    public static void scanFolder(File folder, List<Playable> playables) {
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
                p.path = file.getCanonicalPath();
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

        @Option(names = "-p", description = "Show path to the video file")
        boolean showPath;

        @Override
        public void run() {
            PlayableLoader loader = new SQLitePlayableLoader();
            if (title != null) {
                List<Playable> episodes = loader.getAllEpisodes(title);
                if (episodes.isEmpty()) {
                    System.out.println("No episodes found for: " + title);
                } else {

                    System.out.println(episodes.get(0).plot);
                    System.out.println("-----------------------");
                    if(!showPath){
                        episodes.forEach(p -> System.out.printf(
                                                                "%s - S%02dE%02d seen=%.2f%%%n",
                                                                p.title, p.season, p.episode, (p.lastPos / p.length * 100)
                                                                ));
                    }
                    else{
                        episodes.forEach(p -> System.out.printf(
                                            "%s - S%02dE%02d [%s] %.2f%\n",
                                            p.title, p.season, p.episode, p.path,
                                            (p.lastPos / p.length * 100)
                                            ));

                    }

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

    @Command(name = "gui", description = "Starts a Graphical User Interface for JPLAY")
    static class GUICommand implements Runnable {

        @Override
        public void run() {
            new Main().scanDefault();
            SwingUtilities.invokeLater(() -> {
                    new JplayGui().setVisible(true);

            });
        }
    }
}
