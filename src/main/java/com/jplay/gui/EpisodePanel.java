package com.jplay.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jplay.Main;
import com.jplay.Playable;
import com.jplay.loaders.SQLitePlayableLoader;


public class EpisodePanel extends JPanel {

		JScrollPane scrollPane = new JScrollPane();
		
		public EpisodePanel() {
				setLayout(new BorderLayout());
				
				JLabel etitle = new JLabel("Episodes");
				etitle.setFont(new Font("Serif", Font.PLAIN, 24));
				add(etitle, BorderLayout.NORTH);

				add(scrollPane, BorderLayout.CENTER);
		}

		public void setPlayable(Playable playable) {
				JPanel episodeListPanel = new JPanel();
        episodeListPanel.setLayout(new BoxLayout(episodeListPanel, BoxLayout.Y_AXIS));

				SQLitePlayableLoader loader = new SQLitePlayableLoader();

				
        for (Playable episode : loader.getAllEpisodes(playable.title)) {

						EpisodeRow row = new EpisodeRow(episode);
						
            episodeListPanel.add(row);
            episodeListPanel.add(Box.createVerticalStrut(5)); // space between episodes
        }
				remove(scrollPane);
				scrollPane = new JScrollPane(episodeListPanel);
				add(scrollPane);				
		}
		
}
