package com.jplay.gui;

import java.awt.*;
import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

public class JplayGui extends JFrame {
    public JplayGui() {
        // Modern Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        setTitle("JPlay");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout(10, 10)); // spacing

        // Top Header
        JLabel headerLabel = new JLabel("🎵 JPlay Media Browser");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        add(headerLabel, BorderLayout.NORTH);


        // Main Content Area
        PlayablePanel centerPanel = new PlayablePanel();
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(centerPanel, BorderLayout.CENTER);

        // Left Sidebar
        JPanel leftPanel = new PlayableList(centerPanel);
        leftPanel.setBackground(new Color(245, 245, 245));
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        add(leftPanel, BorderLayout.WEST);


        // Padding around the whole frame
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}
