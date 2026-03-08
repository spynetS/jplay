package com.jplay.player;

import java.io.IOException;

public interface Player {

		public Process start(String videoPath, double startSeconds) throws IOException;
		public double waitForExit(Process process);

}
