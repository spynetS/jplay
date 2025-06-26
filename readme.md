# JPlay


JPlay is a program that stores where you last watched your local
movies and shows. When you play a video file with it, it regesters
that file in the database. When you close the video it stores at
what time you stopped so next time you play the file it begins where
you stopped last time. 

If the file is part of a show (parsed from file name) and 90% was 
watched. The next episode will start playing instead.

```bash

jplay ./Breaking_bad
jplay "Breaking Bad"
jplay -s 3 -e 2 "Breaking Bad" 

jplay list
jplay list --title="Breaking Bad"

```
