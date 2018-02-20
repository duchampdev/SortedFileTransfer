#!/bin/bash
sudo rm -r runtimeImage &&
./gradlew assemble &&
$JAVA_HOME/bin/jlink \
 --module-path $JAVA_HOME/jmods:build/libs/SortedFileTransfer-1.2.jar \
 --add-modules SortedFileTransfer \
 --launcher SortedFileTransfer=SortedFileTransfer/duchampdev.sft.Main \
 --output runtimeImage
