package com.jplay.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jplay.Main;
import com.jplay.Playable;
import com.jplay.loaders.SQLitePlayableLoader;

public class DetailPage extends JPanel {

		
		private Playable playable;
		JPanel mediaDetails = new JPanel();
		Poster poster = new Poster(null, false);
		JLabel title = new JLabel();
		JLabel description = new JLabel();

		EpisodePanel episode_panel;
		
		public DetailPage (CardLayout cardLayout) {
				setLayout(new BorderLayout());
				
				JButton back = new JButton("Back");
        back.addActionListener(e -> cardLayout.show(this.getParent(),"grid"));

				mediaDetails.setLayout(new BorderLayout());
				mediaDetails.add(poster,BorderLayout.WEST);

				JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

				title.setFont(new Font("Serif", Font.PLAIN, 24));
				panel.add(title);
				panel.add(description);
				mediaDetails.add(panel, BorderLayout.CENTER);
				mediaDetails.add(back,BorderLayout.EAST);

				
				episode_panel = new EpisodePanel();
				JPanel play_panel = new JPanel();
				

				JButton play = new JButton("Play latest");
				play.addActionListener(e -> {
								SQLitePlayableLoader loader = new SQLitePlayableLoader();
								Playable p = loader.getPlayable(playable.title,-1,-1);
								p.play(Main.player);
								loader.registerPlayable(p);
						});

				play_panel.add(play);

				add(mediaDetails,BorderLayout.NORTH);
				add(episode_panel,BorderLayout.CENTER);
				add(play_panel,BorderLayout.SOUTH);
		}

		public void update(Playable playable) {

				this.playable = playable;
				
				System.out.println(playable.title);
				episode_panel.setPlayable(playable);
				
				poster.setImage(playable);
				title.setText(playable.title);
				description.setText(playable.plot);
		}
		
}
