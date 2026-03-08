package com.jplay.converters;


import com.jplay.MPV;
import com.jplay.Player;

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
