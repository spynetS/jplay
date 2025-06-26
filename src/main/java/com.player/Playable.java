package com.player;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

// this can be an episode in a show or a movie
public class Playable implements Serializable {
	public String title;
	public String path;

	public double length;
	public double lastPos;
	// for a movie here is 1 1
	public int season;
	public int episode;

	public MPV mpv = null;

	public Playable(){
		this.mpv = new MPV();
	}

	public void play(){
		try{
			System.out.println(this.lastPos);
			var process = mpv.startMPV(this.path, this.lastPos);
			this.lastPos =  mpv.waitForMPVExit(process);
		}catch(IOException e){
			e.printStackTrace();
		}

	}

	public String toString(){
		return "{\ntitle: "+title+"\npath: "+path+"\nseason: "+season+"\nepisode: "+episode+"\nlastpos: "+lastPos+"\n}";
	}
}
