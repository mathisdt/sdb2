# Song Database

The Song Database is a program to show lyrics on a digital projector for worship in a congregation. 

## Features:

- runs on nearly all systems because it is a Java application
- presentation control window shown separately from actual presentation (which means your graphics cards has 
  to have two outputs, or you need two graphics cards)
  - you have a control monitor to display the database and the presentation control while the seperate 
    projection screen shows only the content which the congregation wants to see - no confusion, very professional
- show or hide the song title for the audience
- guitar chords can be included in the database, but will not be shown when presenting songs - they are 
  useful when printing song sheets for the band
- english user interface
- fonts and colors are customizable
- you can add remarks to every song
- easy song search - just type a part of the title or the text
- show the song beginnings in the song list (additionally to the song title) - you can find songs faster 
  in spontaneous worship sessions
- filter on title or text parts
- image slide show possible for announcements, e.g. before or after the service

## Why use this software and not Powerpoint?

- the database format is not binary, but XML (essentially a text file) - you can easily modify it by hand,
  synchronize it using any cloud service or even put it into a Git repository (producing nice diffs)
- the days on which a song is presented are counted automatically (in a separate statistics file)
- the song texts are displayed continually, not on individual slides, so everyone sees how the song continues
  after the current part
  - animation when displaying another part of a song

The program has evolved through more than 10 years at my church. If you find a bug or want a new feature, 
you are welcome to fix things yourself - or file an issue in the [GitHub project](https://github.com/mathisdt/sdb2/)
(or at least drop me a line)!
