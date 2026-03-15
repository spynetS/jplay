package com.jplay.gui;

import java.util.List;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jplay.Playable;
import com.jplay.loaders.SQLitePlayableLoader;

public class TitlePicker extends JPanel {

		public interface TitlePickerListener {
        void onTitlePressed(Playable playable);
    }
    private TitlePickerListener listener;
		
		public TitlePicker() {
				this.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		}
		
		public void update(){
				SQLitePlayableLoader loader = new SQLitePlayableLoader();
				List<Playable> playables = loader.getAllEntries(true);

				ArrayList<String> added = new ArrayList<>();

				for(Playable p : playables){
						if(added.contains(p.title) || p.pathExists != 1) continue;
						added.add(p.title);
						Poster poster = new Poster(p,true);
						// when poster is pressed title was pressed
						poster.setTitlePickerListener((Playable playable) -> {
										listener.onTitlePressed(playable);
								});
						add(poster);
				}
		}

		public void setTitlePickerListener(TitlePickerListener listener) {
				this.listener = listener;
		}
}
