package com.jplay.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import com.jplay.Playable;
import com.jplay.loaders.PlayableLoader;
import com.jplay.loaders.SQLitePlayableLoader;
import com.jplay.omdb.OMDB;


public class PlayablePanel extends JPanel {

	PlayableLoader loader = new SQLitePlayableLoader();
	Playable selectedPlayable;
    private OMDB omdb = null;

    private JLabel title = new JLabel();
    private JTextArea plot = new JTextArea();
    private JLabel imageLabel = new JLabel();

    private DefaultListModel<Playable> episodeListModel = new DefaultListModel<>();
    private JList<Playable> episodeList = new JList<>(episodeListModel);
    private JButton playButton = new JButton("Play Episode");
	private JButton playLatest = new JButton("Play Latest");

    private JLabel lastWatchedLabel = new JLabel("Last watched: None");

	JFrame frame;

    public PlayablePanel(JFrame frame) {
		this.frame = frame;

        // init omdb
        try{
			//retrive apikey
			Properties appProps = new Properties();
			appProps.load(new FileInputStream("/home/spy/.config/jplay/config.properties"));
			String apikey = appProps.getProperty("apikey");
			if (apikey != null) {
				omdb = new OMDB(apikey);
			}


		}catch(Exception e){
			e.printStackTrace();
		}


		episodeList.setCellRenderer(new ListCellRenderer<Playable>() {
				@Override
				public Component getListCellRendererComponent(
															  JList<? extends Playable> list,
															  Playable value,
															  int index,
															  boolean isSelected,
															  boolean cellHasFocus
															  ) {
					JLabel label = new JLabel();

					// Customize the display text
					label.setText(value.episode + "(Last at " + value.lastPos + ")");

					// Optional styling
					label.setOpaque(true);
					label.setFont(new Font("SansSerif", Font.PLAIN, 14));
					label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

					if (isSelected) {
						label.setBackground(list.getSelectionBackground());
						label.setForeground(list.getSelectionForeground());
					} else {
						label.setBackground(list.getBackground());
						label.setForeground(list.getForeground());
					}

					return label;
				}
			});


        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left: Poster
        imageLabel.setPreferredSize(new Dimension(300, 450));
        add(imageLabel, BorderLayout.WEST);

        // Center: Title and Plot
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        add(infoPanel, BorderLayout.CENTER);

        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        infoPanel.add(title, BorderLayout.NORTH);

        plot.setLineWrap(true);
        plot.setWrapStyleWord(true);
        plot.setEditable(false);
        plot.setOpaque(false);
        plot.setFont(plot.getFont().deriveFont(14f));
        JScrollPane plotScroll = new JScrollPane(plot);
        plotScroll.setBorder(null);
        plotScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoPanel.add(plotScroll, BorderLayout.CENTER);

        // Right: Episode list and Play section
        JPanel episodesPanel = new JPanel(new BorderLayout(5, 5));
        episodesPanel.setPreferredSize(new Dimension(250, 450));
        add(episodesPanel, BorderLayout.EAST);

        episodeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(episodeList);
        episodesPanel.add(listScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(playButton, BorderLayout.EAST);
		bottomPanel.add(playLatest, BorderLayout.WEST);
        episodesPanel.add(bottomPanel, BorderLayout.SOUTH);

        lastWatchedLabel.setFont(lastWatchedLabel.getFont().deriveFont(Font.ITALIC, 12f));
        episodesPanel.add(lastWatchedLabel, BorderLayout.NORTH);

        // Play button behavior
        playButton.addActionListener(e -> {
				Playable selected = episodeList.getSelectedValue();
				if (selected != null) {
					lastWatchedLabel.setText("Last watched: " + selected.episode + " at " + selected.lastPos);
					selected.play();
					loader.registerPlayable(selected);
				}

			});
		playLatest.addActionListener(e -> {
				Playable selected = episodeList.getSelectedValue();
				if (selected != null) {
					lastWatchedLabel.setText("Last watched: " + selected.episode + " at " + selected.lastPos);
					Playable p = loader.getPlayable(selectedPlayable.title,-1,-1);
					p.play();
					loader.registerPlayable(p);
				}
			});
    }

    private void setImage(String url) {
        try {
            URL imageUrl = new URL(url);
            ImageIcon imageIcon = new ImageIcon(imageUrl);
            Image scaledImage = imageIcon.getImage().getScaledInstance(300, 450, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updatePlayable(Playable currentPlayable) {
        title.setText(currentPlayable.title + " " + currentPlayable.season);
		selectedPlayable = currentPlayable;



            plot.setText(currentPlayable.plot);
            setImage(currentPlayable.poster);

            // Clear and add new Playables (replace with real data)
            episodeListModel.clear();
            for (Playable episode : loader.getAllEpisodes(currentPlayable.title)) {
                episodeListModel.addElement(episode);
            }
            episodeList.setSelectedIndex(0);
            lastWatchedLabel.setText("Last watched: None");

    }
}
