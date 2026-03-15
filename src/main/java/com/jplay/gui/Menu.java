package com.jplay.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.jplay.Main;
import com.jplay.Playable;
import com.jplay.loaders.SQLitePlayableLoader;

public class Menu extends JMenuBar {

		public interface MenuListener {
				void onPlayableUpdate();
		}

		MenuListener listener;
		
		SQLitePlayableLoader loader = new SQLitePlayableLoader();
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
										for(Playable player : Main.getPlayablesInFolder(selectedFolder.getAbsolutePath())) {
												player.pathExists = 1;
												loader.registerPlayable(player);
												if(listener != null)
														listener.onPlayableUpdate();


										}
								} else {
										System.out.println("No folder selected");
								}
						});
				menu.add(item);

				add(menu);
		}

		public void setMenuListener(MenuListener listener) { this.listener = listener;  }
		
}
