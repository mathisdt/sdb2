![license](https://img.shields.io/github/license/mathisdt/sdb2.svg?style=flat) [![Build](https://github.com/mathisdt/sdb2/actions/workflows/build.yaml/badge.svg)](https://github.com/mathisdt/sdb2/actions) [![last released](https://img.shields.io/github/release-date/mathisdt/sdb2.svg?label=last%20released&style=flat)](https://github.com/mathisdt/sdb2/releases)

# Song Database

The Song Database is a program to show lyrics on a digital projector for worship in a congregation.
It can also display calendar events or a slide show.

## Features:

- runs on nearly all systems because it is a Java application
- presentation control window shown separately from actual presentation(s) (which means your graphics
  card has to have two or three outputs)
  - you have a control monitor to display the database and the presentation control while the seperate
    projection screen shows only the content which the congregation (or the worship team, respectively)
    wants to see - no confusion, very professional
- show or hide the song title for the audience
- guitar chords can be included in the database, but will not be shown when presenting songs to the
  congregation - but they are useful when printing song sheets and for displaying the lyrics including
  the chords to the worship team on a separate monitor
- english user interface
- fonts and colors are customizable
- you can add remarks to every song
- easy song search/filtering - just type a part of the title or the text
- show the song beginnings in the song list (additionally to the song title) - you can find songs faster
  in spontaneous worship sessions
- image slide show possible for announcements, e.g. before or after the service

## Why use this software and not Powerpoint?

- the database format is not binary, but XML (essentially a text file) - you can easily modify it manually
  if you wish, synchronize it using any cloud service or even put it into a Git repository (producing nice diffs)
- the days on which a song is presented are counted automatically (in a separate statistics file)
  so you have the data ready when the collecting society wants to know which songs you used
  - you can export the statistics to an Excel file
- the song texts are displayed continually, not on individual slides, so everyone sees how the song continues
  after the current part
  - animated transition when displaying another part of a song
  - fade effect when displaying another song (with configurable fade duration)

## Getting started

- download the latest [SDB2 release](https://github.com/mathisdt/sdb2/releases/latest)
  (use the file named `sdb2-...` which corresponds to your system), unpack it
  and look for an executable in the `bin` subdirectory
  - if you're on MacOS you have to install Java 17 or later by yourself and use `sdb2-without-jre.zip`

## Contributing

The program has evolved since 2005 at [Koinonia Calvary Chapel Hannover](https://koinonia.church).
If you find a bug or want a new feature, you are welcome to [file an issue](https://github.com/mathisdt/sdb2/issues)
or even fix things yourself and create a pull request!
