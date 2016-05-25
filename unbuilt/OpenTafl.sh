#!/bin/bash

java -server -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+TieredCompilation -Xms256m -Xmx2048m -jar OpenTafl.jar

#if [ `which gnome-terminal` != "" ]; then
#    gnome-terminal --geometry 80x30 -e "java -server -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+TieredCompilation -Xms1024m -Xmx2048m -jar OpenTafl.jar"
#elif [ `which konsole` != "" ]; then
#    konsole --geometry 80x30 -e "java -server -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+TieredCompilation -Xms1024m -Xmx2048m -jar OpenTafl.jar"
#elif [ `which xterm` != "" ]; then
#    xterm --geometry 80x30 -e "java -server -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+TieredCompilation -Xms1024m -Xmx2048m -jar OpenTafl.jar"
    # If it isn't working and you know to look this far,
    # then I don't need to tell you want to do.
#fi
