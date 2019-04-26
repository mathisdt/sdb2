![license](https://img.shields.io/github/license/mathisdt/sdb2.svg?style=flat) [![Travis-CI Build](https://img.shields.io/travis/mathisdt/sdb2.svg?label=Travis-CI%20Build&style=flat)](https://travis-ci.org/mathisdt/sdb2/) [![last released](https://img.shields.io/github/release-date/mathisdt/sdb2.svg?label=last%20released&style=flat)](https://github.com/mathisdt/sdb2/releases)

# Song Database

The Song Database is a program to show lyrics on a digital projector for worship in a congregation.

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

## Getting started

- if you don't have it yet: download and install
  [Java 11 or later](https://adoptopenjdk.net/?variant=openjdk12&jvmVariant=hotspot)
  (use the "Install JRE" button if you're using Windows)
- download the lastest [SDB2 release](https://github.com/mathisdt/sdb2/releases/latest) and unpack it
  (use the file named `sdb2-...`)
- use the starter contained in the "bin" subdirectory which corresponds to your system

## Contributing

The program has evolved since 2005 at [Koinonia Calvary Chapel Hannover](https://koinonia.church).
If you find a bug or want a new feature, you are welcome to [file an issue](https://github.com/mathisdt/sdb2/issues)
or even fix things yourself and create a pull request!
