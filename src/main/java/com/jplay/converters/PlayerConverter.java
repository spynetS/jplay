package com.jplay.converters;


import com.jplay.player.MPV;
import com.jplay.player.Player;

import picocli.CommandLine;

public class PlayerConverter implements CommandLine.ITypeConverter<Player> {

    @Override
    public Player convert(String value) throws Exception {
				switch(value){
				case "mpv":
						return new MPV();
				}
				return null;
		}
}
