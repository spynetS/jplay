package com.jplay.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JplayGui extends JFrame {
	public JplayGui (){
                setTitle("JFrame with Left and Center Panels");
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setSize(800, 600);
                setLocationRelativeTo(null); // Center on screen

                // Set layout for the frame
                setLayout(new BorderLayout());


                // Center/right panel (main content area)
                PlayablePanel centerPanel = new PlayablePanel();
                centerPanel.setBackground(Color.WHITE);

                // Left panel (e.g., sidebar)
                JPanel leftPanel = new PlayableList(centerPanel);
                leftPanel.setBackground(Color.LIGHT_GRAY);
                leftPanel.setPreferredSize(new Dimension(200, 0)); // fixed width

                // Add panels to the frame
                add(leftPanel, BorderLayout.WEST);
                add(centerPanel, BorderLayout.CENTER);
        }
}
