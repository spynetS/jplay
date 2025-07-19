package com.jplay.gui;

import com.jplay.*;
import com.jplay.loaders.SQLitePlayableLoader;

import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

public class PlayableList extends JPanel {


	PlayablePanel playablePanel;

	public PlayableList(PlayablePanel playablePanel){
		this.playablePanel = playablePanel;


	}

	public void updateList(){
		SQLitePlayableLoader loader = new SQLitePlayableLoader();
		List<Playable> playables = loader.getAllEntries();


		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		ArrayList<String> added = new ArrayList<>();
		for(Playable p : playables){
			if(added.contains(p.title)) continue;
			added.add(p.title);
			JButton btn = new JButton(p.title + " " + p.season);
			btn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					playablePanel.updatePlayable(p);
				}
				});
			this.add(btn);
		}

	}

}
