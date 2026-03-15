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
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setBorder(BorderFactory.createEmptyBorder(5,5,5,5)); // padding

            // Episode number
            JLabel number = new JLabel(String.valueOf(episode.episode));
            number.setPreferredSize(new Dimension(30, 20)); // fixed width for alignment

						int _seen = (int)(100*(episode.lastPos/episode.length));
						JLabel seen = new JLabel(String.valueOf(_seen)+"%");
            seen.setPreferredSize(new Dimension(50, 20)); // fixed width for alignment

            // Episode title
            JLabel title = new JLabel(playable.title);
            title.setPreferredSize(new Dimension(200, 20));

						JButton playButton = new JButton("Play");
						playButton.addActionListener((ActionEvent e) -> {
										episode.play(Main.player);
										loader.registerPlayable(episode);
										this.setPlayable(playable);
										validate();
										repaint();
								});
						
            row.add(number);
            row.add(Box.createHorizontalStrut(10)); // spacing
            row.add(seen);
            row.add(Box.createHorizontalStrut(10)); // spacing
            row.add(title);
            row.add(Box.createHorizontalGlue()); // push play button to the right

						if(episode.pathExists == 1) 
								row.add(playButton);
						else
								row.add(new JLabel("There is no file to play"));
						

            episodeListPanel.add(row);
            episodeListPanel.add(Box.createVerticalStrut(5)); // space between episodes
        }
				remove(scrollPane);
				scrollPane = new JScrollPane(episodeListPanel);
				add(scrollPane);				
		}
		
}
