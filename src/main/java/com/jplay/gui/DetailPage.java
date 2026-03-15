package com.jplay.gui;

import java.awt.CardLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jplay.Playable;

public class DetailPage extends JPanel {

		
		private Playable playable;
		JPanel mediaDetails = new JPanel();
		Poster poster = new Poster(null, false);
		JLabel title = new JLabel();
		JLabel description = new JLabel();
		
		public DetailPage (CardLayout cardLayout) {
				JButton back = new JButton("Back");
        back.addActionListener(e -> cardLayout.show(this.getParent(),"grid"));
        

				mediaDetails.add(poster);
				JPanel panel = new JPanel();
				title.setFont(new Font("Serif", Font.PLAIN, 24));
				panel.add(title);
				mediaDetails.add(panel);
								
				mediaDetails.add(back);

				add(mediaDetails);
		}

		public void update(Playable playable) {
				System.out.println(playable.title);
				poster.setImage(playable);
				title.setText(playable.title);
				description.setText(playable.plot);
		}
		
}
