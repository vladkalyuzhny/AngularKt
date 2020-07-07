#!/bin/bash

rm -rf build/js/packages
# bash gradlew :lib:publishToMavenLocal
bash gradlew :compiler:install
bash gradlew build
