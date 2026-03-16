package com.jplay.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.io.*;
import java.util.List;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.jplay.loaders.SQLitePlayableLoader;
import com.jplay.Playable;
import com.jplay.Main;

public class JplayGui extends JFrame {

    private JTextArea consoleArea = new JTextArea();
		//    private PlayablePanel centerPanel;
		private TitlePicker centerPanel;

		private CardLayout cardLayout = new CardLayout();
		private JPanel mainPanel = new JPanel(cardLayout);
		
    private SQLitePlayableLoader loader = new SQLitePlayableLoader();
    private File defaultPath = new File("/home/spy/Movies");

		public static JProgressBar progressBar = new JProgressBar();

    public JplayGui() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
						UIManager.put("Button.hoverBackground", new Color(70, 70, 90));
						UIManager.put("Button.hoverForeground", Color.WHITE);
						UIManager.put("Button.arc", 20); // rounded corners
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }

        setTitle("🎵 JPlay");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

				DetailPage detailsPanel = new DetailPage(cardLayout);

				
				centerPanel = new TitlePicker();
				centerPanel.setTitlePickerListener((Playable playable) -> {
								detailsPanel.update(playable);
								cardLayout.show(mainPanel, "details");
						});


				centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
				detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
				
				mainPanel.add(centerPanel,"grid");
        mainPanel.add(detailsPanel,"details");

				add(mainPanel);
				Menu menu = new Menu();
				menu.setMenuListener(() -> {
								System.out.println("asd");
								centerPanel.update();
								validate();
								repaint();

						});
				setJMenuBar(menu);
				this.add(progressBar,BorderLayout.SOUTH);

        // Load files and run registerPlayable
        loadPlayables();
				
        //getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
												System.out.println("added " + player.title + " " + player.episode);
												player.pathExists = 1;
												loader.registerPlayable(player);
										}
										centerPanel.update();
										validate();
										repaint();

										for (Playable player : loader.getAllEntries(true)) {
												System.out.println("removed " + player.title + " " + player.episode);
												player.pathExists = 0;
												loader.registerPlayable(player);
										}
										for (Playable player : players) {
												System.out.println("added " + player.title + " " + player.episode);
												player.pathExists = 1;
												loader.registerPlayable(player);
										}
										centerPanel.update();
										validate();
										repaint();

            }).start();
        } else {
            System.out.println("✖ Invalid default path: " + defaultPath);
        }
    }
}
