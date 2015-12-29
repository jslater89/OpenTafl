#!/bin/bash

CLASSPATH=out/production/TaflEngine:lib/*

java -ea -cp $CLASSPATH com/manywords/softworks/tafl/OpenTafl --test
