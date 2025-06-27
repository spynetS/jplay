package com.jplay;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

public class MPV {
	private static final String MPV_SOCKET = "/tmp/mpvsocket";

	public Process startMPV(String videoPath, double startSeconds) throws IOException {
		Path socketPath = Paths.get(MPV_SOCKET);

		// Delete old socket if it exists
		if (Files.exists(socketPath)) {
			Files.delete(socketPath);
		}

		// Check if file exists
		File file = new File(videoPath);
		// Format start time
		startSeconds = startSeconds == -1 ? 0 : startSeconds;
		String formattedStart = formatSeconds(startSeconds);


		// Construct command
		List<String> command = Arrays.asList(
											 "mpv",
											 videoPath,
											 "--start=" + formattedStart,
											 "--input-ipc-server=" + MPV_SOCKET,
											 "--msg-level=all=no"
											 );
		// Start the process
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true); // Merge stdout + stderr

		Process process = builder.start();

		// Log MPV output in a background thread
		new Thread(() -> {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						System.out.println("[mpv] " + line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
		}).start();

		return process;
	}


	public String getMPVProperty(String propertyName) {
		try (SocketChannel socketChannel = SocketChannel.open(UnixDomainSocketAddress.of(MPV_SOCKET))) {
			String jsonCommand = "{\"command\": [\"get_property\", \"" + propertyName + "\"]}\n";
			socketChannel.write(ByteBuffer.wrap(jsonCommand.getBytes()));

			ByteBuffer buffer = ByteBuffer.allocate(2048);
			int bytesRead = socketChannel.read(buffer);
			if (bytesRead == -1) return null;

			String response = new String(buffer.array(), 0, bytesRead);
			return parseDataField(response);
		} catch (IOException e) {
			return null;
		}
	}

	public double waitForMPVExit(Process process) {
		double lastPosition = 0;

		while (process.isAlive()) {
			try {
				Thread.sleep(1000);
				String pos = getMPVProperty("time-pos");
				if (pos != null) {
					lastPosition = Double.parseDouble(pos);
				}
			} catch (Exception ignored) {
			}
		}

		return lastPosition;
	}

	private String formatSeconds(double seconds) {
		long hrs = (long) (seconds / 3600);
		long mins = (long) ((seconds % 3600) / 60);
		double secs = seconds % 60;
		return String.format("%02d:%02d:%05.2f", hrs, mins, secs);
	}

	private String parseDataField(String json) {
		int dataIndex = json.indexOf("\"data\"");
		if (dataIndex == -1) return null;

		int colon = json.indexOf(':', dataIndex);
		if (colon == -1) return null;

		// Trim trailing comma or brace
		int end = json.indexOf(',', colon);
		if (end == -1) end = json.indexOf('}', colon);
		if (end == -1) end = json.length();

		String value = json.substring(colon + 1, end).trim();
		// Remove quotes if it's a string
		if (value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1, value.length() - 1);
		}
		return value;
	}
}
