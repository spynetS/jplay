package com.jplay.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import com.formdev.flatlaf.FlatLightLaf;
import com.jplay.loaders.SQLitePlayableLoader;
import com.jplay.Playable;
import com.jplay.Main;

public class JplayGui extends JFrame {

    private JTextArea consoleArea = new JTextArea();
    private JScrollPane consoleScroll;
    private PlayablePanel centerPanel;
    private SQLitePlayableLoader loader = new SQLitePlayableLoader();
    private File defaultPath = new File("/home/spy/Movies");
    private PlayableList leftPanel;

    public JplayGui() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        setTitle("🎵 JPlay");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel headerLabel = new JLabel("🎵 JPlay Media Browser");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        add(headerLabel, BorderLayout.NORTH);

        // Center Panel
        centerPanel = new PlayablePanel(this);
        JScrollPane centerScroll = new JScrollPane(centerPanel);
        centerScroll.setBorder(null);
        add(centerScroll, BorderLayout.CENTER);

        // Sidebar
        leftPanel = new PlayableList(centerPanel);
        leftPanel.setPreferredSize(new Dimension(220, 0));
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
        add(leftPanel, BorderLayout.WEST);

        // Console Panel
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleScroll = new JScrollPane(consoleArea);
        consoleScroll.setPreferredSize(new Dimension(1000, 150));
        add(consoleScroll, BorderLayout.SOUTH);

        // Redirect stdout to console
        redirectSystemOut();

        // Load files and run registerPlayable
        loadPlayables();

        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void redirectSystemOut() {
        PrintStream ps = new PrintStream(new OutputStream() {
            public void write(int b) {
                consoleArea.append(String.valueOf((char) b));
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            }
        });
        System.setOut(ps);
        System.setErr(ps);
    }

    private void loadPlayables() {
        if (defaultPath != null && defaultPath.isDirectory()) {
            new Thread(() -> {
                List<Playable> players = Main.getPlayablesInFolder(defaultPath.getAbsolutePath());
                for (Playable player : players) {
                    loader.registerPlayable(player);
                }
                System.out.println("✔ Done loading " + players.size() + " playables.");
                leftPanel.updateList();
                validate();
                repaint();

            }).start();
        } else {
            System.out.println("✖ Invalid default path: " + defaultPath);
        }
    }
}
