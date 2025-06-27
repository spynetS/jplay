# JPlay


JPlay is a program that stores where you last watched your local
movies and shows. When you play a video file with it, it regesters
that file in the database. When you close the video it stores at
what time you stopped so next time you play the file it begins where
you stopped last time. 

If the file is part of a show (parsed from file name) and 90% was 
watched. The next episode will start playing instead.

## Install
To install use the install script. It will build the project to a jar file
and then place it in `/usr/share/java/`. It will then create a shell
script running `java -jar` on the jar file.
```bash
./install.sh
```

## Uninstall
To uninstall just run the
```bash
./uninstall.sh
```
or just remove the files
`/usr/share/java/jplay`
`/usr/;ocal/bin/jplay`

## How to use
It's cli where you can start a file
```bash
jplay ./The.Wire.S01E01.1080p.BluRay.x265-RARBG.mp4
```
or a folder with episodes
```bash
jplay ./Breaking_bad
jplay "Breaking Bad"
jplay -s 3 -e 2 "Breaking Bad" 

jplay list
jplay list --title="Breaking Bad"
```

## Dependencies
- [MPV](https://mpv.io/)
- ffprobe ([ffmpeg](https://ffmpeg.org/))

