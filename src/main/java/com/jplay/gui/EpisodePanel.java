package com.jplay.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jplay.Main;
import com.jplay.Playable;
import com.jplay.loaders.SQLitePlayableLoader;


public class EpisodePanel extends JPanel {

		JScrollPane scrollPane = new JScrollPane();
		JPanel episodePanel = new JPanel(new CardLayout());

		JPanel seasonsPanel = new JPanel();
		JList<String> seasons = new JList<>();
		DefaultListModel<String> model = new DefaultListModel<>();
		ArrayList<Playable> playables = new ArrayList<>();
		
		public EpisodePanel() {
				setLayout(new BorderLayout());
				
				JLabel etitle = new JLabel("Episodes");
				etitle.setFont(new Font("Serif", Font.PLAIN, 24));
				add(etitle, BorderLayout.NORTH);

				seasons = new JList<String>(model);
				seasons.addListSelectionListener(new ListSelectionListener() {
								@Override
								public void valueChanged(ListSelectionEvent e) {
										setSeason(Integer.parseInt(seasons.getSelectedValue()));
								}
						});
				
        seasons.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				seasonsPanel.add(seasons);



				add(seasons,BorderLayout.WEST);
				add(scrollPane, BorderLayout.CENTER);

		}

		public void setSeason(int season) {
				JPanel episodeListPanel = new JPanel();
        episodeListPanel.setLayout(new BoxLayout(episodeListPanel, BoxLayout.Y_AXIS));

				SQLitePlayableLoader loader = new SQLitePlayableLoader();
				

        for (Playable episode : playables) {
						if (episode.season != season) continue;
						
						EpisodeRow row = new EpisodeRow(episode);
            episodeListPanel.add(row);
            episodeListPanel.add(Box.createVerticalStrut(5));


						if(!model.contains(String.valueOf(episode.season))) {
								model.addElement(String.valueOf(episode.season));
						}
        }
				
				remove(scrollPane);
				scrollPane = new JScrollPane(episodeListPanel);
				add(scrollPane);
				
				validate();
				repaint();
		}

		public void setPlayable(Playable playable) {
				SQLitePlayableLoader loader = new SQLitePlayableLoader();
				for(Playable episode : loader.getAllEpisodes(playable.title)) {
						playables.add(episode);
						if(!model.contains(String.valueOf(episode.season))) {
								model.addElement(String.valueOf(episode.season));
						}
				}
				setSeason(1);
		}
}
