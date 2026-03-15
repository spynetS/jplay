package com.jplay.converters;


import com.jplay.player.*;

import picocli.CommandLine;

public class PlayerConverter implements CommandLine.ITypeConverter<Player> {

    @Override
    public Player convert(String value) throws Exception {
				switch(value){
				case "mpv":
						return new MPV();
				case "vlc":
						return new VLC();
				}
				return null;
		}
}
