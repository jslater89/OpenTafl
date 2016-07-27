#!/bin/bash

./gradlew clean && ./gradlew build && ./linux-test.sh --silent && ./package-gradle.sh
