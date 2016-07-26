#!/bin/bash

./gradlew build && ./linux-test.sh --silent && ./package-gradle.sh
