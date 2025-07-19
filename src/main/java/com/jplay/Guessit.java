package com.jplay;

import java.io.*;
import java.util.regex.*;

public class Guessit {
    private String title;
    private int season = 1;
    private int episode = 1;
    private double length = -1;

    public Guessit(String filename, String fullPath) {
        parse(filename);
        this.length = probeLength(fullPath);
    }

    private void parse(String filename) {
        int lastSlash = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (lastSlash != -1) {
            filename = filename.substring(lastSlash + 1);
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex != -1) {
            filename = filename.substring(0, dotIndex);
        }

        Pattern[] patterns = new Pattern[]{
            Pattern.compile("(.+?)\\.s(\\d{1,2})e(\\d{1,3})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(.+?)\\.(\\d{1,2})x(\\d{1,3})"),
            Pattern.compile("(.+?)\\.season[\\s._-]?(\\d{1,2})[\\s._-]?episode[\\s._-]?(\\d{1,3})"),
            Pattern.compile("(.+?)\\.(\\d)(\\d{2})"),
            Pattern.compile("(.+?)[\\s-]+s(\\d{1,2})e(\\d{1,3})", Pattern.CASE_INSENSITIVE)
        };

        for (Pattern p : patterns) {
            Matcher m = p.matcher(filename);
            if (m.find()) {
                this.title = cleanTitle(m.group(1));
                this.season = tryParseInt(m.group(2), 1);
                this.episode = tryParseInt(m.group(3), 1);
                return;
            }
        }

        this.title = cleanTitle(filename);
    }

    private String cleanTitle(String rawTitle) {
        return rawTitle.replaceAll("[._-]+", " ").trim();
    }

    private int tryParseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private double probeLength(String fullPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                fullPath
            );
            Process proc = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = reader.readLine();
            proc.waitFor();
            if (line != null) {
                return Double.parseDouble(line.trim());
            }
        } catch (Exception e) {
            System.err.println("Error probing duration: " + e.getMessage());
        }
        return -1;
    }

    // Getters
    public String getTitle() { return title; }
    public int getSeason() { return season; }
    public int getEpisode() { return episode; }
    public double getLength() { return length; }


}
