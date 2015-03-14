@echo off
REM this is an example of how a start script could look like if an unpacked JDK (jre1.8.0_31-windows) would be used to start the Song Database using a custom songs file (sdb-songs\songs.xml)
jre1.8.0_31-windows\bin\javaw.exe -jar sdb2-2.0.0.556\bin\sdb2.jar sdb-songs\songs.xml
