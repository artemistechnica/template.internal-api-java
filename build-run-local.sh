#!/bin/bash

./gradlew clean build
docker build --tag=template.internal-api-java:latest .
docker run -p8080:8080 template.internal-api-java:latest

