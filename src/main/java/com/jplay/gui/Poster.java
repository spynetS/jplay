package com.jplay.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jplay.gui.TitlePicker.TitlePickerListener;


import com.jplay.Playable;

public class Poster extends JPanel {

		private TitlePickerListener listener;
		JLabel label = new JLabel();
		
		public Poster(Playable p, boolean showTitle){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.CENTER_ALIGNMENT);
				
        JLabel titleLabel = new JLabel("<html><div style='text-align: center; width: 150px;'>" + ((p != null) ? p.title : "") + "</div></html>");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

				if(p != null)
						setImage(p);

				label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
								if(listener != null)
										listener.onTitlePressed(p);
						}
        });
				
				this.add(label);
        this.add(Box.createVerticalStrut(5));
				if (showTitle)
						this.add(titleLabel);

        this.setMaximumSize(new Dimension(150, 240));
		}

		public void setImage(Playable p) {
				try {
            URL imageUrl = new URL(p.poster);
            ImageIcon imageIcon = new ImageIcon(imageUrl);
            Image scaledImage = imageIcon.getImage().getScaledInstance(150, 225, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
        } catch (MalformedURLException e) {
            System.err.println("Poster url wrong! " + p.poster);
            label.setText(p.title);
        }
		}


		public void setTitlePickerListener(TitlePickerListener listener) {
				this.listener = listener;
		}
		
}
