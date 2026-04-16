package com.jplay.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.jplay.Main;
import com.jplay.Playable;
import com.jplay.loaders.PlayableLoader;


public class Menu extends JMenuBar {

		public interface MenuListener {
				void onPlayableUpdate();
		}

		MenuListener listener;
		
		PlayableLoader loader = Main.loader;
		public Menu() {
				JMenu menu = new JMenu("File");

				JMenuItem item = new JMenuItem("Load folder");
				item.addActionListener(e->{
								JFileChooser chooser = new JFileChooser();
								chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								chooser.setAcceptAllFileFilterUsed(false);
								int result = chooser.showOpenDialog(null);
        
								if (result == JFileChooser.APPROVE_OPTION) {
										File selectedFolder = chooser.getSelectedFile();
										System.out.println("Selected folder: " + selectedFolder.getAbsolutePath());
										JplayGui.progressBar.setValue(0);
										var players = Main.getPlayablesInFolder(selectedFolder.getAbsolutePath());
										var size = 100/(players.size()-1);
										JplayGui.progressBar.setString("Loading...");
										JplayGui.progressBar.setStringPainted(true);
										JplayGui.progressBar.setVisible(true);
										new Thread(() -> {
														for(Playable player : players) {
																player.pathExists = 1;
																loader.registerPlayable(player);
																if(listener != null)
																		listener.onPlayableUpdate();

																JplayGui.progressBar.setValue(JplayGui.progressBar.getValue()+size);
														}
														JplayGui.progressBar.setString("");
														JplayGui.progressBar.setVisible(false);
										}).start();
										
								} else {
										System.out.println("No folder selected");
								}
						});
				menu.add(item);

				add(menu);
		}

		public void setMenuListener(MenuListener listener) { this.listener = listener;  }
		
}
