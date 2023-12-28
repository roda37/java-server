#!/bin/bash
javac Webserver.java
jar cfm program.jar manifest.mf Webserver.class Webserver\$LeHandler.class ./lib/mariadb-java-client-3.3.2.jar
java -jar program.jar
