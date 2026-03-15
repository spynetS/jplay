package com.jplay.player;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class VLC implements Player {
    private static final String VLC_HOST = "127.0.0.1";
    private static final int VLC_PORT = 9999; // RC interface port

    @Override
    public Process start(String videoPath, double startSeconds) throws IOException {
        // Format start time
        startSeconds = startSeconds < 0 ? 0 : startSeconds;

        // Construct command to start VLC with RC interface
        List<String> command = Arrays.asList(
                "vlc",
                videoPath,
                "--extraintf", "rc",
                "--rc-host", VLC_HOST + ":" + VLC_PORT,
                "--start-time", String.valueOf((int) startSeconds),
                "--no-video-title-show",
                "--quiet"
        );

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        // Log VLC output in a background thread
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[vlc] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        return process;
    }

    /**
     * Send a command to VLC RC interface
     */
    private String sendVLCCommand(String command) {
        try (Socket socket = new Socket(VLC_HOST, VLC_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(command);
            // VLC RC interface often echoes a response; read a line or two
            return in.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Get current playback position in seconds
     */
    public double getVLCPosition() {
        String response = sendVLCCommand("get_time");
        if (response != null) {
            try {
                return Double.parseDouble(response.trim());
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    @Override
    public double waitForExit(Process process) {
        double lastPosition = 0;

        while (process.isAlive()) {
            try {
                Thread.sleep(1000);
                double pos = getVLCPosition();
                lastPosition = pos;
            } catch (Exception ignored) {}
        }

        return lastPosition;
    }
}
