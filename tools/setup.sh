#!/bin/bash

bash gradlew wrapper --gradle-version 5.6 --distribution-type all
bash gradlew :lib:publishToMavenLocal
bash gradlew :compiler:install
