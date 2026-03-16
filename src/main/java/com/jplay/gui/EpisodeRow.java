package com.jplay.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jplay.Main;
import com.jplay.Playable;
import com.jplay.loaders.SQLitePlayableLoader;

public class EpisodeRow extends JPanel {

		public EpisodeRow(Playable episode) {
				SQLitePlayableLoader loader = new SQLitePlayableLoader();
				
				this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
				this.setAlignmentX(Component.LEFT_ALIGNMENT);
				this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5)); // padding

				// Episode number
				JLabel number = new JLabel(String.valueOf(episode.episode));
				number.setPreferredSize(new Dimension(30, 20)); // fixed width for alignment

				int _seen = (int)(100*(episode.lastPos/episode.length));
				JLabel seen = new JLabel(String.valueOf(_seen)+"%");
				seen.setPreferredSize(new Dimension(50, 20)); // fixed width for alignment

				// Episode title
				JLabel title = new JLabel(episode.title);
				title.setPreferredSize(new Dimension(200, 20));

				JButton playButton = new JButton("Play");
				playButton.addActionListener((ActionEvent e) -> {
								episode.play(Main.player);
								loader.registerPlayable(episode);
								//								this.setPlayable(episode);
								validate();
								repaint();
						});
						
				this.add(number);
				this.add(Box.createHorizontalStrut(10)); // spacing
				this.add(seen);
				this.add(Box.createHorizontalStrut(10)); // spacing
				this.add(title);
				this.add(Box.createHorizontalGlue()); // push play button to the right

				if(episode.pathExists == 1) 
						this.add(playButton);
				else
						this.add(new JLabel("There is no file to play"));
						

		}
		
}
