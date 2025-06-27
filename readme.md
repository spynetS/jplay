# JPlay


JPlay is a program that stores where you last watched your local
movies and shows. When you play a video file with it, it regesters
that file in the database. When you close the video it stores at
what time you stopped so next time you play the file it begins where
you stopped last time. 

If the file is part of a show (parsed from file name) and 90% was 
watched. The next episode will start playing instead.

## Install
To build
```bash
mvn clean package
```
this will create a jar file at `target/jplay.jar`
it can be run with
```bash
java -jar jplay.jar
```
#### Example 
Move the jar file to `usr/local/bin`
then create a bash script which will run that jar file
```bash
sudo ln -s $PWD/target/jplay.jar /usr/local/bin/jplay.jar
echo -e '#!/bin/bash\njava -jar /usr/local/bin/jplay.jar "$@"' | sudo tee /usr/local/bin/jplay > /dev/null
sudo chmod 755 /usr/local/bin/jplay
```


## How to use
It's cli where you can start a file
```bash
jplay ./he.Wire.S01E01.1080p.BluRay.x265-RARBG.mp4
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

